package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.DefinizioneFlussoDTO;
import it.grandimolini.aia.model.TaskProcesso.TipoTask;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

/**
 * Interpreta un file BPMN 2.0 (generato da bpmn-js) ed estrae:
 * <ul>
 *   <li>La lista ordinata di step eseguibili ({@link #parseSteps}) — per l'anteprima</li>
 *   <li>Il grafo completo del processo ({@link #parseGraph}) — per l'engine runtime</li>
 * </ul>
 *
 * <p>Il grafo supporta:
 * <ul>
 *   <li>userTask / serviceTask — task normali</li>
 *   <li>exclusiveGateway (XOR) — biforcazione condizionale</li>
 *   <li>parallelGateway (AND) — fork/join parallelo</li>
 *   <li>callActivity — riferimento a sotto-processo</li>
 * </ul>
 */
@Service
public class BpmnParserService {

    // ════ Records pubblici ════════════════════════════════════════════════════

    /**
     * Configurazione di una chiamata HTTP associata a un serviceTask.
     * Corrisponde all'elemento {@code <aia:serviceCall>} negli extensionElements.
     */
    public record ServiceCallConfig(
            String url,       // es. "https://api.example.com/notifica"
            String method,    // GET | POST | PUT | PATCH | DELETE  (default POST)
            String body,      // JSON template; ${processoId} e ${documentoId} vengono sostituiti
            String headers    // coppie "Header: valore" separate da newline
    ) {}

    /** Tipo di nodo nel grafo BPMN */
    public enum NodeType {
        START,
        END,
        USER_TASK,
        SERVICE_TASK,
        XOR_GATEWAY,    // exclusiveGateway — biforcazione "prendi uno solo"
        AND_GATEWAY,    // parallelGateway  — fork/join "prendi tutti / aspetta tutti"
        CALL_ACTIVITY,  // sotto-processo esterno
        UNKNOWN
    }

    /** Nodo del grafo BPMN */
    public record BpmnNode(
            String id,
            String name,
            NodeType type,
            ServiceCallConfig serviceCall,
            AutoTaskConfig autoTask,
            String calledElement   // solo per CALL_ACTIVITY: nome/id della DefinizioneFlusso figlia
    ) {
        /** TipoTask JPA corrispondente (null per gateway/start/end) */
        public TipoTask toTipoTask() {
            return switch (type) {
                case USER_TASK     -> TipoTask.USER_TASK;
                case SERVICE_TASK  -> TipoTask.SERVICE_TASK;
                case CALL_ACTIVITY -> TipoTask.CALL_ACTIVITY;
                default            -> null;
            };
        }
    }

    /** Arco orientato del grafo BPMN */
    public record FlowEdge(String sourceId, String targetId, String condition) {}

    /**
     * Grafo completo del processo BPMN.
     *
     * @param nodes       id → nodo
     * @param outgoing    id → lista archi uscenti
     * @param incomingIds id → lista id sorgenti degli archi entranti
     * @param startId     id dello startEvent
     * @param endIds      id degli endEvent
     */
    public record BpmnGraph(
            Map<String, BpmnNode>   nodes,
            Map<String, List<FlowEdge>> outgoing,
            Map<String, List<String>>   incomingIds,
            String        startId,
            Set<String>   endIds
    ) {
        public BpmnNode node(String id)              { return nodes.getOrDefault(id, null); }
        public List<FlowEdge> out(String id)         { return outgoing.getOrDefault(id, List.of()); }
        public List<String>   in(String id)          { return incomingIds.getOrDefault(id, List.of()); }
        public boolean isEnd(String id)              { return endIds.contains(id); }
    }

    /** Step estratto dal BPMN XML, usato da BpmService per creare i TaskProcesso */
    public record BpmnStep(String id, String nome, TipoTask tipo, ServiceCallConfig serviceCall, AutoTaskConfig autoTask) {
        public BpmnStep(String id, String nome, TipoTask tipo) { this(id, nome, tipo, null, null); }
        public BpmnStep(String id, String nome, TipoTask tipo, ServiceCallConfig sc) { this(id, nome, tipo, sc, null); }
    }

    // ════ parseSteps — lista flat per anteprima (invariato) ═════════════════

    /**
     * Parsa il BPMN XML e ritorna la lista ordinata di step eseguibili.
     * Usato per l'endpoint /preview. Per i gateway XOR prende il primo ramo.
     *
     * @param bpmnXml stringa XML BPMN 2.0
     * @return lista di BpmnStep in ordine topologico
     */
    public List<BpmnStep> parseSteps(String bpmnXml) throws Exception {
        BpmnGraph graph = parseGraph(bpmnXml);
        return flattenGraph(graph);
    }

    /**
     * Appiattisce il grafo in una lista lineare (per anteprima).
     * Segue il primo ramo per i gateway XOR; per AND prende tutti i rami.
     */
    public List<BpmnStep> flattenGraph(BpmnGraph graph) {
        List<BpmnStep> result  = new ArrayList<>();
        Set<String>    visited = new LinkedHashSet<>();
        Queue<String>  queue   = new LinkedList<>();
        if (graph.startId() != null) queue.add(graph.startId());

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            BpmnNode node = graph.node(current);
            if (node != null && node.toTipoTask() != null) {
                result.add(new BpmnStep(node.id(), node.name(), node.toTipoTask(),
                        node.serviceCall(), node.autoTask()));
            }

            List<FlowEdge> out = graph.out(current);
            if (!out.isEmpty()) {
                // Per XOR: prende solo il primo ramo (preview non valuta condizioni)
                // Per AND: prende tutti i rami — ma li deduplicamo con visited
                queue.addAll(out.stream().map(FlowEdge::targetId).toList());
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                "Il BPMN non contiene userTask o serviceTask raggiungibili dallo startEvent");
        }
        return result;
    }

    // ════ parseGraph — grafo completo per l'engine runtime ══════════════════

    /**
     * Parsa il BPMN XML in un grafo navigabile a runtime.
     * Usato da BpmService per la valutazione dinamica di gateway e sub-processi.
     *
     * @param bpmnXml stringa XML BPMN 2.0
     * @return BpmnGraph completo
     */
    public BpmnGraph parseGraph(String bpmnXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(bpmnXml)));
        doc.getDocumentElement().normalize();

        Map<String, BpmnNode> nodes = new LinkedHashMap<>();

        // ── Nodi task ──────────────────────────────────────────────────────
        addTaskNodes(doc, "userTask",    NodeType.USER_TASK,    nodes);
        addTaskNodes(doc, "serviceTask", NodeType.SERVICE_TASK, nodes);
        addCallActivityNodes(doc, nodes);

        // ── Gateway ────────────────────────────────────────────────────────
        addGatewayNodes(doc, "exclusiveGateway", NodeType.XOR_GATEWAY, nodes);
        addGatewayNodes(doc, "parallelGateway",  NodeType.AND_GATEWAY, nodes);
        addGatewayNodes(doc, "inclusiveGateway", NodeType.XOR_GATEWAY, nodes); // trattato come XOR

        // ── Start / End ────────────────────────────────────────────────────
        String startId = null;
        Set<String> endIds = new LinkedHashSet<>();
        NodeList starts = doc.getElementsByTagNameNS("*", "startEvent");
        for (int i = 0; i < starts.getLength(); i++) {
            Element el = (Element) starts.item(i);
            String id = el.getAttribute("id");
            nodes.put(id, new BpmnNode(id, "Start", NodeType.START, null, null, null));
            if (i == 0) startId = id;
        }
        NodeList ends = doc.getElementsByTagNameNS("*", "endEvent");
        for (int i = 0; i < ends.getLength(); i++) {
            Element el = (Element) ends.item(i);
            String id = el.getAttribute("id");
            nodes.put(id, new BpmnNode(id, "End", NodeType.END, null, null, null));
            endIds.add(id);
        }

        // ── SequenceFlow con condizioni ────────────────────────────────────
        Map<String, List<FlowEdge>> outgoing    = new LinkedHashMap<>();
        Map<String, List<String>>   incomingIds = new LinkedHashMap<>();
        NodeList seqFlows = doc.getElementsByTagNameNS("*", "sequenceFlow");
        for (int i = 0; i < seqFlows.getLength(); i++) {
            Element sf  = (Element) seqFlows.item(i);
            String src  = sf.getAttribute("sourceRef");
            String tgt  = sf.getAttribute("targetRef");
            String cond = extractCondition(sf);

            outgoing.computeIfAbsent(src, k -> new ArrayList<>())
                    .add(new FlowEdge(src, tgt, cond));
            incomingIds.computeIfAbsent(tgt, k -> new ArrayList<>()).add(src);
        }

        if (startId == null) {
            throw new IllegalArgumentException("Nessun startEvent trovato nel BPMN XML");
        }
        return new BpmnGraph(nodes, outgoing, incomingIds, startId, endIds);
    }

    // ════ toPreview ══════════════════════════════════════════════════════════

    public List<DefinizioneFlussoDTO.StepPreview> toPreview(List<BpmnStep> steps) {
        List<DefinizioneFlussoDTO.StepPreview> previews = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            BpmnStep s = steps.get(i);
            DefinizioneFlussoDTO.StepPreview p = new DefinizioneFlussoDTO.StepPreview();
            p.setId(s.id());
            p.setNome(s.nome());
            p.setTipoTask(s.tipo().name() + (s.autoTask() != null ? ":" + s.autoTask().tipo() :
                          s.serviceCall() != null ? ":API_CALL" : ""));
            p.setOrdine(i + 1);
            previews.add(p);
        }
        return previews;
    }

    // ════ Helpers privati ════════════════════════════════════════════════════

    private void addTaskNodes(Document doc, String localName, NodeType nodeType,
                              Map<String, BpmnNode> map) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el   = (Element) nodes.item(i);
            String id    = el.getAttribute("id");
            String name  = el.getAttribute("name");
            if (name == null || name.isBlank()) name = id;
            if (id != null && !id.isBlank()) {
                ServiceCallConfig sc     = nodeType == NodeType.SERVICE_TASK ? parseServiceCall(el) : null;
                AutoTaskConfig    autoTask = nodeType == NodeType.SERVICE_TASK ? parseAutoTaskConfig(el) : null;
                map.put(id, new BpmnNode(id, name.trim(), nodeType, sc, autoTask, null));
            }
        }
    }

    private void addCallActivityNodes(Document doc, Map<String, BpmnNode> map) {
        NodeList nodes = doc.getElementsByTagNameNS("*", "callActivity");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el          = (Element) nodes.item(i);
            String id           = el.getAttribute("id");
            String name         = el.getAttribute("name");
            String calledElement = el.getAttribute("calledElement"); // es. "RinnovoAIA"
            if (name == null || name.isBlank()) name = calledElement != null ? calledElement : id;
            if (id != null && !id.isBlank()) {
                map.put(id, new BpmnNode(id, name.trim(), NodeType.CALL_ACTIVITY, null, null, calledElement));
            }
        }
    }

    private void addGatewayNodes(Document doc, String localName, NodeType nodeType,
                                 Map<String, BpmnNode> map) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el  = (Element) nodes.item(i);
            String id   = el.getAttribute("id");
            String name = el.getAttribute("name");
            if (name == null || name.isBlank()) name = id;
            if (id != null && !id.isBlank()) {
                map.put(id, new BpmnNode(id, name.trim(), nodeType, null, null, null));
            }
        }
    }

    /**
     * Estrae il testo della condizione dal sequenceFlow.
     * Cerca prima {@code <conditionExpression>}, poi l'attributo {@code conditionExpression}.
     */
    private String extractCondition(Element seqFlow) {
        // Child element <conditionExpression>
        NodeList conds = seqFlow.getElementsByTagNameNS("*", "conditionExpression");
        if (conds.getLength() > 0) {
            String text = conds.item(0).getTextContent();
            if (text != null && !text.isBlank()) return text.trim();
        }
        // Attributo diretto (raro ma possibile in alcuni dialetti)
        String condAttr = seqFlow.getAttribute("conditionExpression");
        if (condAttr != null && !condAttr.isBlank()) return condAttr;
        return null;
    }

    /**
     * Cerca {@code <aia:serviceCall>} dentro gli extensionElements del task.
     */
    private ServiceCallConfig parseServiceCall(Element taskEl) {
        NodeList ext = taskEl.getElementsByTagNameNS("*", "serviceCall");
        if (ext.getLength() == 0) return null;
        Element sc = (Element) ext.item(0);
        String url = sc.getAttribute("url");
        if (url == null || url.isBlank()) return null;
        String method  = sc.hasAttribute("method")  ? sc.getAttribute("method").toUpperCase() : "POST";
        String body    = sc.hasAttribute("body")    ? sc.getAttribute("body")    : null;
        String headers = sc.hasAttribute("headers") ? sc.getAttribute("headers") : null;
        return new ServiceCallConfig(url, method, body, headers);
    }

    /**
     * Cerca AutoTaskConfig all'interno degli extensionElements,
     * cercando un elemento documentation che contiene JSON.
     */
    private AutoTaskConfig parseAutoTaskConfig(Element taskEl) {
        NodeList extList = taskEl.getElementsByTagNameNS("*", "extensionElements");
        for (int i = 0; i < extList.getLength(); i++) {
            Element ext = (Element) extList.item(i);
            NodeList docs = ext.getElementsByTagNameNS("*", "documentation");
            for (int j = 0; j < docs.getLength(); j++) {
                String text = docs.item(j).getTextContent();
                if (text != null && text.trim().startsWith("{")) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper om =
                            new com.fasterxml.jackson.databind.ObjectMapper();
                        AutoTaskConfig cfg = om.readValue(text.trim(), AutoTaskConfig.class);
                        if (cfg.tipo() != null) return cfg;
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }
}
