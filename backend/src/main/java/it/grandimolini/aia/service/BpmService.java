package it.grandimolini.aia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.grandimolini.aia.dto.AvviaProcessoRequest;
import it.grandimolini.aia.dto.CompletaTaskRequest;
import it.grandimolini.aia.exception.BadRequestException;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.*;
import it.grandimolini.aia.model.ProcessoDocumento.StatoProcesso;
import it.grandimolini.aia.model.ProcessoDocumento.TipoProcesso;
import it.grandimolini.aia.model.TaskProcesso.StatoTask;
import it.grandimolini.aia.model.TaskProcesso.TipoTask;
import it.grandimolini.aia.repository.*;
import it.grandimolini.aia.service.BpmnParserService.*;
import it.grandimolini.aia.repository.ScadenzaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servizio BPM: gestisce l'avvio, la progressione e il completamento
 * dei processi documentali. Implementa la macchina a stati che rispecchia
 * il diagramma BPMN "Lavorazione Documento AIA".
 */
@Service
public class BpmService {

    private static final Logger log = LoggerFactory.getLogger(BpmService.class);
    private static final AtomicLong sequencer = new AtomicLong(1000);

    @Autowired private ProcessoDocumentoRepository processoRepo;
    @Autowired private TaskProcessoRepository taskRepo;
    @Autowired private DocumentoRepository documentoRepo;
    @Autowired private StabilimentoRepository stabilimentoRepo;
    @Autowired private DefinizioneFlussoRepository definizioneFlussoRepo;
    @Autowired private BpmnParserService bpmnParser;
    @Autowired private ScadenzaRepository scadenzaRepository;
    @Autowired(required = false) private EmailService emailService;

    /** Segreto condiviso con LocalhostInternalAuthFilter per chiamate self-API senza JWT. */
    @org.springframework.beans.factory.annotation.Value("${app.internal.secret}")
    private String internalSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    // ─── Definizioni delle sequenze di task per tipo processo ───────────────
    record StepDef(String id, String nome, TipoTask tipo, ServiceCallConfig serviceCall, AutoTaskConfig autoTask) {
        static StepDef from(BpmnStep s) {
            return new StepDef(s.id(), s.nome(), s.tipo(), s.serviceCall(), s.autoTask());
        }
        /** Costruttore di comodo per step hardcoded (senza API call) */
        StepDef(String id, String nome, TipoTask tipo) { this(id, nome, tipo, null, null); }
    }

    private static final List<StepDef> STEPS_LAVORAZIONE_DOCUMENTO = List.of(
        new StepDef("Task_Ricezione",           "Ricezione e protocollazione documento",  TipoTask.USER_TASK),
        new StepDef("Task_RevisioneMetadati",    "Revisione metadati e classificazione",   TipoTask.USER_TASK),
        new StepDef("Task_EstrazioneEntita",     "Estrazione entità (scadenze/prescrizioni)", TipoTask.USER_TASK),
        new StepDef("Task_Approvazione",         "Approvazione e conferma entità",         TipoTask.USER_TASK),
        new StepDef("Task_Archiviazione",        "Archiviazione documento",                TipoTask.SERVICE_TASK)
    );

    private static final List<StepDef> STEPS_RINNOVO_AIA = List.of(
        new StepDef("Task_AvvioRinnovo",         "Avvio procedura rinnovo AIA",            TipoTask.USER_TASK),
        new StepDef("Task_RaccoltaDoc",          "Raccolta documentazione per rinnovo",    TipoTask.USER_TASK),
        new StepDef("Task_RevioneDoc",           "Revisione documenti raccolti",           TipoTask.USER_TASK),
        new StepDef("Task_InvioEnti",            "Invio istanza agli enti competenti",     TipoTask.USER_TASK),
        new StepDef("Task_AttesaEsito",          "Attesa esito istruttoria",               TipoTask.USER_TASK)
    );

    private static final List<StepDef> STEPS_NON_CONFORMITA = List.of(
        new StepDef("Task_RilevazioneNC",        "Rilevazione non conformità",             TipoTask.USER_TASK),
        new StepDef("Task_AnalisiCause",         "Analisi cause e responsabilità",         TipoTask.USER_TASK),
        new StepDef("Task_PianoAzioni",          "Definizione piano azioni correttive",    TipoTask.USER_TASK),
        new StepDef("Task_VerificaEfficacia",    "Verifica efficacia azioni",              TipoTask.USER_TASK),
        new StepDef("Task_ChiusuraNC",           "Chiusura non conformità",                TipoTask.USER_TASK)
    );

    private static final List<StepDef> STEPS_INTEGRAZIONE_ENTE = List.of(
        new StepDef("Task_RicezioneRichiesta",   "Ricezione richiesta integrazione",       TipoTask.USER_TASK),
        new StepDef("Task_AnalisiRichiesta",     "Analisi e valutazione richiesta",        TipoTask.USER_TASK),
        new StepDef("Task_PreparazioneRisposta", "Preparazione risposta integrativa",      TipoTask.USER_TASK),
        new StepDef("Task_InvioRisposta",        "Invio risposta all'ente",                TipoTask.USER_TASK)
    );

    // ─── Avvio processo ──────────────────────────────────────────────────────

    @Transactional
    public ProcessoDocumento avviaProcesso(AvviaProcessoRequest req, String avviatoDa) {
        // Carica documento
        Documento documento = documentoRepo.findById(req.getDocumentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", req.getDocumentoId()));

        // Verifica non esista già un processo attivo per questo documento
        List<ProcessoDocumento> esistenti = processoRepo.findByDocumentoId(documento.getId());
        boolean haProcessoAttivo = esistenti.stream()
                .anyMatch(p -> p.getStato() != StatoProcesso.COMPLETATO
                            && p.getStato() != StatoProcesso.ANNULLATO);
        if (haProcessoAttivo) {
            throw new BadRequestException("Esiste già un processo attivo per questo documento");
        }

        // Carica stabilimento
        Stabilimento stabilimento = null;
        if (req.getStabilimentoId() != null) {
            stabilimento = stabilimentoRepo.findById(req.getStabilimentoId())
                    .orElse(documento.getStabilimento());
        } else {
            stabilimento = documento.getStabilimento();
        }

        // ── Risolve tipo processo e step ─────────────────────────────────────
        DefinizioneFlusso definizioneFlusso = null;
        List<StepDef> steps;
        String codice;

        if (req.getDefinizioneFlussoId() != null) {
            // Modalità dinamica: step dal BPMN XML con graph traversal
            definizioneFlusso = definizioneFlussoRepo.findById(req.getDefinizioneFlussoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "DefinizioneFlusso", "id", req.getDefinizioneFlussoId()));
            if (!Boolean.TRUE.equals(definizioneFlusso.getAttiva())) {
                throw new BadRequestException("La definizione di flusso selezionata non è attiva");
            }
            codice = generaCodiceProcessoDinamico(definizioneFlusso.getNome());
            // Verifica che il BPMN sia parsabile prima di salvare il processo
            try {
                bpmnParser.parseGraph(definizioneFlusso.getBpmnXml());
            } catch (Exception e) {
                throw new BadRequestException("Errore nel parsing del BPMN: " + e.getMessage());
            }
            steps = List.of(); // non usato in modalità dinamica
        } else {
            // Modalità predefinita: tipo hardcoded
            if (req.getTipoProcesso() == null) {
                throw new BadRequestException(
                    "Specificare tipoProcesso oppure definizioneFlussoId");
            }
            steps = getStepPerTipo(req.getTipoProcesso());
            codice = generaCodiceProcesso(req.getTipoProcesso());
        }

        // Crea processo
        ProcessoDocumento processo = new ProcessoDocumento();
        processo.setCodiceProcesso(codice);
        processo.setTipoProcesso(req.getTipoProcesso());
        processo.setDefinizioneFlusso(definizioneFlusso);
        processo.setStato(StatoProcesso.AVVIATO);
        processo.setDocumento(documento);
        processo.setStabilimento(stabilimento);
        processo.setAvviatoDa(avviatoDa);
        processo.setAssegnatoA(req.getAssegnatoA() != null ? req.getAssegnatoA() : avviatoDa);
        processo.setNote(req.getNote());

        ProcessoDocumento saved = processoRepo.save(processo);

        if (definizioneFlusso != null) {
            // ── Modalità dinamica: traversal grafo dal nodo startEvent ──────
            try {
                BpmnGraph graph = bpmnParser.parseGraph(definizioneFlusso.getBpmnXml());
                avanzaOltre(saved, graph.startId(), null, graph);
            } catch (Exception e) {
                throw new BadRequestException("Errore avvio processo BPMN: " + e.getMessage());
            }
        } else {
            // ── Modalità hardcoded: crea tutti i task upfront ────────────────
            for (int i = 0; i < steps.size(); i++) {
                StepDef step = steps.get(i);
                TaskProcesso task = new TaskProcesso();
                task.setProcesso(saved);
                task.setTaskIdBpmn(step.id());
                task.setNomeTask(step.nome());
                task.setTipoTask(step.tipo());
                task.setStatoTask(i == 0 ? StatoTask.IN_CORSO : StatoTask.CREATO);
                task.setAssegnatoA(i == 0 ? saved.getAssegnatoA() : null);
                serializeTaskConfig(task, step);
                taskRepo.save(task);
            }
            // Se il primo task è un SERVICE_TASK, eseguilo subito
            if (!steps.isEmpty() && steps.get(0).tipo() == TipoTask.SERVICE_TASK) {
                List<TaskProcesso> creati = taskRepo.findByProcessoId(saved.getId());
                creati.stream().filter(t -> t.getStatoTask() == StatoTask.IN_CORSO).findFirst()
                      .ifPresent(t -> eseguiServiceTask(t, saved));
            }
        }

        // Aggiorna taskCorrente
        List<TaskProcesso> tuttiTask = taskRepo.findByProcessoId(saved.getId());
        tuttiTask.stream()
            .filter(t -> t.getStatoTask() == StatoTask.IN_CORSO)
            .findFirst()
            .ifPresent(t -> saved.setTaskCorrente(t.getNomeTask()));
        if (saved.getTaskCorrente() == null && !tuttiTask.isEmpty()) {
            saved.setTaskCorrente(tuttiTask.get(0).getNomeTask());
        }

        // Aggiorna stato documento
        documento.setStatoDocumento(Documento.StatoDocumento.IN_LAVORAZIONE);
        documento.setProcesso(saved);
        documentoRepo.save(documento);

        return processoRepo.findById(saved.getId()).orElse(saved);
    }

    // ─── Completamento task ───────────────────────────────────────────────────

    @Transactional
    public ProcessoDocumento completaTask(Long processoId, Long taskId,
                                          CompletaTaskRequest req, String utenteCorrente) {
        ProcessoDocumento processo = processoRepo.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", processoId));

        if (processo.getStato() == StatoProcesso.COMPLETATO
                || processo.getStato() == StatoProcesso.ANNULLATO) {
            throw new BadRequestException("Il processo è già terminato");
        }

        TaskProcesso task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (!task.getProcesso().getId().equals(processoId)) {
            throw new BadRequestException("Il task non appartiene a questo processo");
        }
        if (task.getStatoTask() != StatoTask.IN_CORSO && task.getStatoTask() != StatoTask.CREATO) {
            throw new BadRequestException("Il task non è in stato attivo");
        }

        // Completa il task corrente
        task.setStatoTask(StatoTask.COMPLETATO);
        task.setCompletatoDa(utenteCorrente);
        task.setCommento(req.getCommento());
        task.setEsito(req.getEsito());
        task.setFormDataJson(req.getFormDataJson());
        task.setDataCompletamento(LocalDateTime.now());
        taskRepo.save(task);

        if (processo.getDefinizioneFlusso() != null) {
            // ── Modalità dinamica BPMN: traversal grafo ──────────────────────
            try {
                BpmnGraph graph = bpmnParser.parseGraph(processo.getDefinizioneFlusso().getBpmnXml());

                if (task.getParallelGroupId() != null) {
                    // ── Task in ramo parallelo AND ───────────────────────────
                    List<TaskProcesso> gruppoTask = taskRepo.findByProcessoIdAndParallelGroupId(
                            processoId, task.getParallelGroupId());
                    boolean tuttiCompletati = gruppoTask.stream()
                            .allMatch(t -> t.getStatoTask() == StatoTask.COMPLETATO);
                    if (tuttiCompletati) {
                        // Tutti i rami paralleli hanno finito: avanza oltre il join gateway
                        String joinId = task.getJoinGatewayId();
                        if (joinId != null) {
                            avanzaOltre(processo, joinId, req, graph);
                        } else {
                            completaProcesso(processo);
                        }
                    }
                    // Altrimenti: altri rami ancora in esecuzione, non fare niente
                } else {
                    // ── Avanzamento sequenziale / gateway ───────────────────
                    avanzaOltre(processo, task.getTaskIdBpmn(), req, graph);
                }
            } catch (TimerScheduledException e) {
                log.info("Timer scheduled for task {}: {}", task.getNomeTask(), e.getMessage());
                processo.setTaskCorrente(task.getNomeTask() + " ⏳");
                processoRepo.save(processo);
                return processoRepo.findById(processoId).orElse(processo);
            } catch (Exception e) {
                log.error("Errore traversal BPMN per processo {}: {}", processoId, e.getMessage());
                throw new BadRequestException("Errore nel motore BPM: " + e.getMessage());
            }
        } else {
            // ── Modalità hardcoded: lista lineare dei task ───────────────────
            List<TaskProcesso> tutti = taskRepo.findByProcessoId(processoId);
            TaskProcesso prossimo = tutti.stream()
                    .filter(t -> t.getStatoTask() == StatoTask.CREATO)
                    .findFirst()
                    .orElse(null);

            if (prossimo != null) {
                prossimo.setStatoTask(StatoTask.IN_CORSO);
                prossimo.setAssegnatoA(processo.getAssegnatoA());
                taskRepo.save(prossimo);
                processo.setStato(StatoProcesso.IN_CORSO);
                processo.setTaskCorrente(prossimo.getNomeTask());
                aggiornaStatoDocumento(processo.getDocumento(), prossimo.getTaskIdBpmn());

                if (prossimo.getTipoTask() == TipoTask.SERVICE_TASK
                        && (prossimo.getAutoTaskConfig() != null || prossimo.getServiceCallConfig() != null)) {
                    try {
                        eseguiServiceTask(prossimo, processo);
                        prossimo.setStatoTask(StatoTask.COMPLETATO);
                        prossimo.setDataCompletamento(LocalDateTime.now());
                        prossimo.setCompletatoDa("sistema");
                        taskRepo.save(prossimo);

                        TaskProcesso dopoService = taskRepo.findByProcessoId(processoId).stream()
                                .filter(t -> t.getStatoTask() == StatoTask.CREATO)
                                .findFirst().orElse(null);
                        if (dopoService != null) {
                            dopoService.setStatoTask(StatoTask.IN_CORSO);
                            dopoService.setAssegnatoA(processo.getAssegnatoA());
                            taskRepo.save(dopoService);
                            processo.setTaskCorrente(dopoService.getNomeTask());
                        } else {
                            completaProcesso(processo);
                        }
                    } catch (TimerScheduledException e) {
                        log.info("Timer scheduled for task {}: {}", prossimo.getNomeTask(), e.getMessage());
                        processo.setTaskCorrente(prossimo.getNomeTask() + " ⏳");
                        processoRepo.save(processo);
                        return processoRepo.findById(processoId).orElse(processo);
                    }
                }
            } else {
                completaProcesso(processo);
            }
        }

        // Aggiorna taskCorrente con task attivo (se non già aggiornato)
        List<TaskProcesso> attiviOra = taskRepo.findByProcessoId(processoId);
        attiviOra.stream()
            .filter(t -> t.getStatoTask() == StatoTask.IN_CORSO)
            .findFirst()
            .ifPresent(t -> {
                processo.setTaskCorrente(t.getNomeTask());
                processo.setStato(StatoProcesso.IN_CORSO);
            });

        processoRepo.save(processo);
        return processoRepo.findById(processoId).orElse(processo);
    }

    // ─── Sospendi / Riprendi / Annulla ────────────────────────────────────────

    @Transactional
    public ProcessoDocumento sospendiProcesso(Long processoId, String motivo) {
        ProcessoDocumento processo = getProcessoById(processoId);
        if (processo.getStato() == StatoProcesso.COMPLETATO
                || processo.getStato() == StatoProcesso.ANNULLATO) {
            throw new BadRequestException("Il processo non può essere sospeso");
        }
        processo.setStato(StatoProcesso.SOSPESO);
        if (motivo != null) {
            processo.setNote((processo.getNote() != null ? processo.getNote() + "\n" : "") + "SOSPESO: " + motivo);
        }
        return processoRepo.save(processo);
    }

    @Transactional
    public ProcessoDocumento riprendiProcesso(Long processoId) {
        ProcessoDocumento processo = getProcessoById(processoId);
        if (processo.getStato() != StatoProcesso.SOSPESO
                && processo.getStato() != StatoProcesso.IN_ATTESA) {
            throw new BadRequestException("Il processo non è in stato sospeso/attesa");
        }
        processo.setStato(StatoProcesso.IN_CORSO);
        return processoRepo.save(processo);
    }

    @Transactional
    public ProcessoDocumento annullaProcesso(Long processoId, String motivo) {
        ProcessoDocumento processo = getProcessoById(processoId);
        if (processo.getStato() == StatoProcesso.COMPLETATO) {
            throw new BadRequestException("Un processo completato non può essere annullato");
        }
        processo.setStato(StatoProcesso.ANNULLATO);
        processo.setDataCompletamento(LocalDateTime.now());
        if (motivo != null) {
            processo.setNote((processo.getNote() != null ? processo.getNote() + "\n" : "") + "ANNULLATO: " + motivo);
        }
        // Ripristina stato documento
        if (processo.getDocumento() != null) {
            Documento doc = processo.getDocumento();
            doc.setStatoDocumento(Documento.StatoDocumento.RICEVUTO);
            documentoRepo.save(doc);
        }
        return processoRepo.save(processo);
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    public ProcessoDocumento getProcessoById(Long id) {
        return processoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo", "id", id));
    }

    public List<ProcessoDocumento> getProcessiAttivi() {
        return processoRepo.findProcessiAttivi();
    }

    public Page<ProcessoDocumento> getProcessiAttiviPaged(Pageable pageable) {
        return processoRepo.findProcessiAttiviPaged(pageable);
    }

    public List<ProcessoDocumento> getProcessiByStabilimento(Long stabilimentoId) {
        return processoRepo.findByStabilimentoId(stabilimentoId);
    }

    public List<ProcessoDocumento> getProcessiByDocumento(Long documentoId) {
        return processoRepo.findByDocumentoId(documentoId);
    }

    public List<TaskProcesso> getTaskAttiviPerUtente(String username) {
        return taskRepo.findTaskAttiviPerUtente(username);
    }

    public long countProcessiAttivi() {
        return processoRepo.countProcessiAttivi();
    }

    public List<ProcessoDocumento> getAllProcessi() {
        return processoRepo.findAll();
    }

    // ─── Helpers privati ──────────────────────────────────────────────────────

    private String generaCodiceProcesso(TipoProcesso tipo) {
        String prefisso = switch (tipo) {
            case LAVORAZIONE_DOCUMENTO -> "LAV";
            case RINNOVO_AIA           -> "RIN";
            case NON_CONFORMITA        -> "NC";
            case INTEGRAZIONE_ENTE     -> "INT";
        };
        String anno = DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now());
        long seq = sequencer.getAndIncrement();
        return String.format("%s-%s-%05d", prefisso, anno, seq);
    }

    private List<StepDef> getStepPerTipo(TipoProcesso tipo) {
        return switch (tipo) {
            case LAVORAZIONE_DOCUMENTO -> STEPS_LAVORAZIONE_DOCUMENTO;
            case RINNOVO_AIA           -> STEPS_RINNOVO_AIA;
            case NON_CONFORMITA        -> STEPS_NON_CONFORMITA;
            case INTEGRAZIONE_ENTE     -> STEPS_INTEGRAZIONE_ENTE;
        };
    }

    private String generaCodiceProcessoDinamico(String nomeDefinizione) {
        // Prende le prime 3 lettere del nome, uppercase
        String prefisso = nomeDefinizione == null || nomeDefinizione.isBlank() ? "CUS"
                : nomeDefinizione.replaceAll("[^A-Za-z]", "").toUpperCase()
                        .substring(0, Math.min(3, nomeDefinizione.replaceAll("[^A-Za-z]", "").length()));
        if (prefisso.isBlank()) prefisso = "CUS";
        String anno = DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now());
        long seq = sequencer.getAndIncrement();
        return String.format("%s-%s-%05d", prefisso, anno, seq);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ─── ENGINE: graph traversal — XOR, AND, CALL_ACTIVITY ───────────────────
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Nucleo del motore BPM: avanza il processo oltre un nodo già completato.
     *
     * <p>Legge gli archi uscenti da {@code fromNodeId}, poi per ogni arco:
     * <ul>
     *   <li>END — completa il processo</li>
     *   <li>USER_TASK / SERVICE_TASK / CALL_ACTIVITY — crea il task e lo attiva</li>
     *   <li>XOR_GATEWAY — valuta le condizioni, recursa sul ramo vincente</li>
     *   <li>AND_GATEWAY fork — crea task paralleli con parallelGroupId</li>
     *   <li>AND_GATEWAY join — solo passthrough (chiamato dopo check tutti-completati)</li>
     * </ul>
     *
     * @param processo    processo in esecuzione
     * @param fromNodeId  id del nodo BPMN appena completato (o gateway)
     * @param req         request di completamento del task (per la valutazione condizioni XOR)
     * @param graph       grafo BPMN parsato
     */
    private void avanzaOltre(ProcessoDocumento processo, String fromNodeId,
                             CompletaTaskRequest req, BpmnGraph graph) {

        List<FlowEdge> outEdges = graph.out(fromNodeId);

        for (FlowEdge edge : outEdges) {
            BpmnNode target = graph.node(edge.targetId());
            if (target == null) continue;

            switch (target.type()) {

                case END -> {
                    // Tutti i rami hanno raggiunto la fine
                    completaProcesso(processo);
                }

                case USER_TASK, SERVICE_TASK -> {
                    TaskProcesso nuovoTask = creaTaskDaNodo(processo, target, null, null);
                    attivaTask(nuovoTask, processo);
                    // Esegui automaticamente se SERVICE_TASK con configurazione
                    if (target.type() == NodeType.SERVICE_TASK
                            && (nuovoTask.getAutoTaskConfig() != null || nuovoTask.getServiceCallConfig() != null)) {
                        try {
                            eseguiServiceTask(nuovoTask, processo);
                            nuovoTask.setStatoTask(StatoTask.COMPLETATO);
                            nuovoTask.setDataCompletamento(LocalDateTime.now());
                            nuovoTask.setCompletatoDa("sistema");
                            taskRepo.save(nuovoTask);
                            // Continua ricorsivamente al task successivo
                            avanzaOltre(processo, nuovoTask.getTaskIdBpmn(), req, graph);
                        } catch (TimerScheduledException e) {
                            // Il timer task blocca qui — il scheduler lo riprenderà
                            throw e;
                        }
                    }
                }

                case CALL_ACTIVITY -> {
                    TaskProcesso callTask = creaTaskDaNodo(processo, target, null, null);
                    attivaTask(callTask, processo);
                    avviaSubProcesso(callTask, processo, target.calledElement());
                }

                case XOR_GATEWAY -> {
                    // Valuta gli archi uscenti dal gateway XOR e segui il vincente
                    FlowEdge winning = valutaXor(target.id(), graph, processo, req);
                    if (winning != null) {
                        // Recursa attraverso il gateway verso il nodo vincente
                        // (usiamo il gateway id come "from" per seguire l'arco scelto)
                        avanzaDaArco(processo, winning, req, graph);
                    } else {
                        log.warn("XOR gateway {}: nessuna condizione vera, processo bloccato", target.id());
                    }
                }

                case AND_GATEWAY -> {
                    List<FlowEdge> andOut = graph.out(target.id());
                    if (andOut.size() <= 1) {
                        // AND join usato come passthrough — continua oltre
                        avanzaOltre(processo, target.id(), req, graph);
                    } else {
                        // AND fork: crea task paralleli per tutti i rami
                        String joinGatewayId = trovaNodoJoinAndGateway(target.id(), graph);
                        String groupId = target.id();
                        for (FlowEdge branchEdge : andOut) {
                            BpmnNode branchNode = graph.node(branchEdge.targetId());
                            if (branchNode == null) continue;
                            if (branchNode.type() == NodeType.END) {
                                // Un ramo va subito all'end — solo se tutti gli altri completati
                                continue;
                            }
                            if (branchNode.type() == NodeType.USER_TASK
                                    || branchNode.type() == NodeType.SERVICE_TASK
                                    || branchNode.type() == NodeType.CALL_ACTIVITY) {
                                TaskProcesso parallelTask = creaTaskDaNodo(processo, branchNode, groupId, joinGatewayId);
                                attivaTask(parallelTask, processo);
                                // Esegui automaticamente se SERVICE_TASK
                                if (branchNode.type() == NodeType.SERVICE_TASK
                                        && (parallelTask.getAutoTaskConfig() != null || parallelTask.getServiceCallConfig() != null)) {
                                    try {
                                        eseguiServiceTask(parallelTask, processo);
                                        parallelTask.setStatoTask(StatoTask.COMPLETATO);
                                        parallelTask.setDataCompletamento(LocalDateTime.now());
                                        parallelTask.setCompletatoDa("sistema");
                                        taskRepo.save(parallelTask);
                                        // Controlla se tutti i rami sono terminati
                                        List<TaskProcesso> gruppo = taskRepo.findByProcessoIdAndParallelGroupId(
                                                processo.getId(), groupId);
                                        boolean tuttiDone = gruppo.stream()
                                                .allMatch(t -> t.getStatoTask() == StatoTask.COMPLETATO);
                                        if (tuttiDone && joinGatewayId != null) {
                                            avanzaOltre(processo, joinGatewayId, req, graph);
                                        }
                                    } catch (TimerScheduledException e) {
                                        throw e;
                                    }
                                }
                            }
                        }
                    }
                }

                default -> log.debug("Nodo ignorato: {} (tipo: {})", target.id(), target.type());
            }
        }
    }

    /**
     * Avanza direttamente da un FlowEdge specifico verso il suo target.
     * Usato dai gateway XOR per seguire il ramo vincente.
     */
    private void avanzaDaArco(ProcessoDocumento processo, FlowEdge edge,
                              CompletaTaskRequest req, BpmnGraph graph) {
        BpmnNode target = graph.node(edge.targetId());
        if (target == null) return;

        // Costruiamo un mini-traversal trattando l'edge come un arco da un nodo virtuale
        // verso il target: usiamo avanzaOltre con il SOURCE del edge come from,
        // ma solo per il caso di "già siamo al gateway" — creiamo una lista mono-elemento
        // che punta direttamente al target.

        // Semplificazione: creiamo un grafo temporaneo con un solo arco phantom
        // source=phantom → target, e lo usiamo per avanzare.
        // In pratica è più semplice duplicare la logica di creazione del task:

        switch (target.type()) {
            case END             -> completaProcesso(processo);
            case USER_TASK,
                 SERVICE_TASK    -> {
                TaskProcesso t = creaTaskDaNodo(processo, target, null, null);
                attivaTask(t, processo);
                if (target.type() == NodeType.SERVICE_TASK
                        && (t.getAutoTaskConfig() != null || t.getServiceCallConfig() != null)) {
                    try {
                        eseguiServiceTask(t, processo);
                        t.setStatoTask(StatoTask.COMPLETATO);
                        t.setDataCompletamento(LocalDateTime.now());
                        t.setCompletatoDa("sistema");
                        taskRepo.save(t);
                        avanzaOltre(processo, t.getTaskIdBpmn(), req, graph);
                    } catch (TimerScheduledException e) { throw e; }
                }
            }
            case CALL_ACTIVITY   -> {
                TaskProcesso ct = creaTaskDaNodo(processo, target, null, null);
                attivaTask(ct, processo);
                avviaSubProcesso(ct, processo, target.calledElement());
            }
            case XOR_GATEWAY,
                 AND_GATEWAY     -> avanzaOltre(processo, target.id(), req, graph);
            default              -> {}
        }
    }

    /**
     * Trova il gateway AND di join corrispondente a un AND fork.
     * Fa una BFS in avanti e cerca il primo AND_GATEWAY con più di 1 arco entrante.
     *
     * @param forkId id del gateway AND di fork
     * @param graph  grafo BPMN
     * @return id del join gateway, o null se non trovato
     */
    private String trovaNodoJoinAndGateway(String forkId, BpmnGraph graph) {
        Queue<String> queue   = new LinkedList<>();
        Set<String>   visited = new HashSet<>();
        queue.add(forkId);
        visited.add(forkId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (FlowEdge edge : graph.out(current)) {
                String tid = edge.targetId();
                if (visited.contains(tid)) continue;
                visited.add(tid);

                BpmnNode node = graph.node(tid);
                if (node == null) continue;

                if (node.type() == NodeType.AND_GATEWAY && !tid.equals(forkId)) {
                    // È un AND gateway diverso dal fork — controlla incoming count
                    List<String> inc = graph.in(tid);
                    if (inc.size() > 1) return tid;
                }
                queue.add(tid);
            }
        }
        return null;
    }

    /**
     * Valuta gli archi uscenti di un XOR gateway e ritorna il FlowEdge vincente.
     *
     * <p>Ordine di valutazione:
     * <ol>
     *   <li>Archi con condizione valutata come vera</li>
     *   <li>Arco di default (arco senza condizione)</li>
     * </ol>
     *
     * @param xorId  id del gateway XOR
     * @param graph  grafo BPMN
     * @param processo processo corrente (per il contesto variabili)
     * @param req    request di completamento (contiene esito e formData del task precedente)
     * @return il FlowEdge vincente, o null se nessuno corrisponde
     */
    private FlowEdge valutaXor(String xorId, BpmnGraph graph,
                               ProcessoDocumento processo, CompletaTaskRequest req) {
        List<FlowEdge> outEdges = graph.out(xorId);
        Map<String, Object> ctx = buildStepContext(processo.getId());

        FlowEdge defaultEdge = null;
        for (FlowEdge edge : outEdges) {
            if (edge.condition() == null || edge.condition().isBlank()) {
                defaultEdge = edge;
            } else if (evaluateCondition(edge.condition(), processo, ctx, req)) {
                return edge;
            }
        }
        return defaultEdge; // nessuna condizione vera → default
    }

    /**
     * Valuta una condizione BPMN di tipo {@code ${espressione}}.
     *
     * <p>Espressioni supportate:
     * <ul>
     *   <li>{@code ${esito == 'APPROVATO'}} — confronto col campo esito del task completato</li>
     *   <li>{@code ${formData.campo == 'valore'}} — campo del form JSON</li>
     *   <li>{@code ${Task_Login.status_code >= 200}} — campo dalla risposta di un SERVICE_TASK</li>
     *   <li>{@code ${codiceProcesso == 'RIN-2026-00001'}} — variabile di processo</li>
     * </ul>
     *
     * @param condition espressione BPMN condizione (con o senza {@code ${}})
     * @param processo  processo corrente
     * @param stepCtx   contesto dei SERVICE_TASK completati
     * @param req       dati del task appena completato (per esito e formData)
     * @return true se la condizione è vera
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(String condition, ProcessoDocumento processo,
                                      Map<String, Object> stepCtx, CompletaTaskRequest req) {
        if (condition == null || condition.isBlank()) return true;

        // Rimuovi ${  }
        String expr = condition.trim();
        if (expr.startsWith("${") && expr.endsWith("}")) {
            expr = expr.substring(2, expr.length() - 1).trim();
        }

        // Tenta il parsing come confronto: lhs operatore rhs
        String[] operators = {">=", "<=", "!=", "==", ">", "<"};
        for (String op : operators) {
            int idx = expr.indexOf(op);
            if (idx <= 0) continue;

            String lhsExpr = expr.substring(0, idx).trim();
            String rhsRaw  = expr.substring(idx + op.length()).trim();

            // Rimuovi virgolette dal valore rhs
            if ((rhsRaw.startsWith("'") && rhsRaw.endsWith("'"))
                    || (rhsRaw.startsWith("\"") && rhsRaw.endsWith("\""))) {
                rhsRaw = rhsRaw.substring(1, rhsRaw.length() - 1);
            }

            // Risolvi lhs
            String lhsValue = resolveConditionLhs(lhsExpr, processo, stepCtx, req);
            return compareValues(lhsValue, op, rhsRaw);
        }

        // Nessun operatore trovato: tratta come presenza di un valore (non-empty = true)
        String val = resolveConditionLhs(expr, processo, stepCtx, req);
        return val != null && !val.isBlank() && !"false".equalsIgnoreCase(val) && !"0".equals(val);
    }

    /**
     * Risolve la parte sinistra di una condizione a una stringa.
     */
    @SuppressWarnings("unchecked")
    private String resolveConditionLhs(String lhs, ProcessoDocumento processo,
                                       Map<String, Object> stepCtx, CompletaTaskRequest req) {
        // Variabili built-in
        switch (lhs) {
            case "esito"          -> { return req != null && req.getEsito() != null ? req.getEsito() : ""; }
            case "processoId"     -> { return String.valueOf(processo.getId()); }
            case "codiceProcesso" -> { return processo.getCodiceProcesso() != null ? processo.getCodiceProcesso() : ""; }
        }

        // formData.campo — legge il form data JSON del task appena completato
        if (lhs.startsWith("formData.") && req != null && req.getFormDataJson() != null) {
            String campo = lhs.substring("formData.".length());
            try {
                Map<String, Object> formData = objectMapper.readValue(req.getFormDataJson(), Map.class);
                Object val = formData.get(campo);
                return val != null ? String.valueOf(val) : "";
            } catch (Exception ignored) {}
        }

        // Task_X.campo — output di un SERVICE_TASK completato
        int dot = lhs.indexOf('.');
        if (dot > 0) {
            String taskId = lhs.substring(0, dot);
            String field  = lhs.substring(dot + 1);
            Object taskData = stepCtx.get(taskId);
            if (taskData instanceof Map<?,?> map) {
                Object val = map.get(field);
                return val != null ? String.valueOf(val) : "";
            }
        }

        // Prova come variabile di processo nel contesto step
        Object raw = stepCtx.get("_raw_" + lhs);
        if (raw != null) return String.valueOf(raw);

        return resolveTemplate("${" + lhs + "}", processo, stepCtx);
    }

    /**
     * Confronta due valori stringa con un operatore.
     * Se entrambi sono numerici, usa il confronto numerico.
     */
    private boolean compareValues(String lhs, String op, String rhs) {
        // Prova confronto numerico
        try {
            double l = Double.parseDouble(lhs);
            double r = Double.parseDouble(rhs);
            return switch (op) {
                case "==" -> l == r;
                case "!=" -> l != r;
                case ">"  -> l >  r;
                case "<"  -> l <  r;
                case ">=" -> l >= r;
                case "<=" -> l <= r;
                default   -> false;
            };
        } catch (NumberFormatException ignored) {}

        // Confronto stringa
        int cmp = lhs.compareTo(rhs);
        return switch (op) {
            case "==" -> lhs.equalsIgnoreCase(rhs);
            case "!=" -> !lhs.equalsIgnoreCase(rhs);
            case ">"  -> cmp > 0;
            case "<"  -> cmp < 0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            default   -> false;
        };
    }

    /**
     * Crea e persiste un nuovo TaskProcesso partendo da un BpmnNode.
     *
     * @param parallelGroupId id del gruppo parallelo (null per task sequenziali)
     * @param joinGatewayId   id del gateway AND di join (null per task sequenziali)
     */
    private TaskProcesso creaTaskDaNodo(ProcessoDocumento processo, BpmnNode node,
                                        String parallelGroupId, String joinGatewayId) {
        TaskProcesso task = new TaskProcesso();
        task.setProcesso(processo);
        task.setTaskIdBpmn(node.id());
        task.setNomeTask(node.name());
        task.setTipoTask(node.toTipoTask() != null ? node.toTipoTask() : TipoTask.USER_TASK);
        task.setStatoTask(StatoTask.CREATO);
        task.setParallelGroupId(parallelGroupId);
        task.setJoinGatewayId(joinGatewayId);
        if (node.calledElement() != null) {
            task.setCalledElement(node.calledElement());
        }
        serializeTaskConfigFromNode(task, node);
        return taskRepo.save(task);
    }

    /** Attiva un task appena creato (IN_CORSO + assegnazione) */
    private void attivaTask(TaskProcesso task, ProcessoDocumento processo) {
        task.setStatoTask(StatoTask.IN_CORSO);
        task.setAssegnatoA(processo.getAssegnatoA());
        taskRepo.save(task);
        processo.setTaskCorrente(task.getNomeTask());
        aggiornaStatoDocumento(processo.getDocumento(), task.getTaskIdBpmn());
    }

    /** Completa il processo e archivia il documento */
    private void completaProcesso(ProcessoDocumento processo) {
        processo.setStato(StatoProcesso.COMPLETATO);
        processo.setTaskCorrente(null);
        processo.setDataCompletamento(LocalDateTime.now());
        if (processo.getDocumento() != null) {
            processo.getDocumento().setStatoDocumento(Documento.StatoDocumento.ARCHIVIATO);
            documentoRepo.save(processo.getDocumento());
        }
        processoRepo.save(processo);
    }

    /**
     * Avvia un sotto-processo (callActivity).
     * Cerca la DefinizioneFlusso per nome/chiave e la avvia come processo figlio.
     * Il task CALL_ACTIVITY rimane IN_CORSO fino al completamento del figlio.
     */
    private void avviaSubProcesso(TaskProcesso callTask, ProcessoDocumento parentProcesso,
                                  String calledElement) {
        if (calledElement == null || calledElement.isBlank()) {
            log.warn("CallActivity {} senza calledElement — task richiede completamento manuale",
                    callTask.getTaskIdBpmn());
            return;
        }
        try {
            // Cerca la definizione flusso per nome
            List<DefinizioneFlusso> candidati = definizioneFlussoRepo.findAll().stream()
                    .filter(df -> Boolean.TRUE.equals(df.getAttiva()))
                    .filter(df -> calledElement.equalsIgnoreCase(df.getNome())
                               || calledElement.equalsIgnoreCase(df.getId() != null ? String.valueOf(df.getId()) : null))
                    .toList();

            if (candidati.isEmpty()) {
                log.warn("CallActivity: DefinizioneFlusso '{}' non trovata. Task rimane manuale.", calledElement);
                return;
            }
            DefinizioneFlusso subDef = candidati.get(0);

            // Crea il processo figlio
            ProcessoDocumento childProcesso = new ProcessoDocumento();
            childProcesso.setCodiceProcesso(
                generaCodiceProcessoDinamico(subDef.getNome()) + "-SUB");
            childProcesso.setDefinizioneFlusso(subDef);
            childProcesso.setStato(StatoProcesso.AVVIATO);
            childProcesso.setDocumento(parentProcesso.getDocumento());
            childProcesso.setStabilimento(parentProcesso.getStabilimento());
            childProcesso.setAvviatoDa("sistema");
            childProcesso.setAssegnatoA(parentProcesso.getAssegnatoA());
            childProcesso.setNote("Sotto-processo di " + parentProcesso.getCodiceProcesso());
            ProcessoDocumento savedChild = processoRepo.save(childProcesso);

            // Avvia il figlio
            BpmnGraph childGraph = bpmnParser.parseGraph(subDef.getBpmnXml());
            avanzaOltre(savedChild, childGraph.startId(), null, childGraph);

            // Lega il task padre al processo figlio
            callTask.setChildProcessoId(savedChild.getId());
            callTask.setApiRisposta("Sotto-processo avviato: " + savedChild.getCodiceProcesso()
                + " (id=" + savedChild.getId() + ")");
            taskRepo.save(callTask);

            log.info("CallActivity: sotto-processo {} avviato per task {}",
                    savedChild.getCodiceProcesso(), callTask.getNomeTask());

        } catch (Exception e) {
            log.error("Errore avvio sotto-processo '{}': {}", calledElement, e.getMessage());
            callTask.setApiErrore("Errore avvio sub-processo: " + e.getMessage());
            taskRepo.save(callTask);
        }
    }

    // ─── Helper: serializza configurazione task ───────────────────────────────

    private void serializeTaskConfig(TaskProcesso task, StepDef step) {
        if (step.autoTask() != null) {
            try { task.setAutoTaskConfig(objectMapper.writeValueAsString(step.autoTask())); }
            catch (Exception e) { log.warn("autoTaskConfig serialize error: {}", e.getMessage()); }
        } else if (step.serviceCall() != null) {
            try { task.setServiceCallConfig(objectMapper.writeValueAsString(step.serviceCall())); }
            catch (Exception e) { log.warn("serviceCallConfig serialize error: {}", e.getMessage()); }
        }
    }

    private void serializeTaskConfigFromNode(TaskProcesso task, BpmnNode node) {
        if (node.autoTask() != null) {
            try { task.setAutoTaskConfig(objectMapper.writeValueAsString(node.autoTask())); }
            catch (Exception e) { log.warn("autoTaskConfig serialize error: {}", e.getMessage()); }
        } else if (node.serviceCall() != null) {
            try { task.setServiceCallConfig(objectMapper.writeValueAsString(node.serviceCall())); }
            catch (Exception e) { log.warn("serviceCallConfig serialize error: {}", e.getMessage()); }
        }
    }

    // ─── Esecuzione automatica SERVICE_TASK ───────────────────────────────────

    /**
     * Esecuzione automatica SERVICE_TASK.
     * Prova prima con il nuovo formato AutoTaskConfig, poi fallback a serviceCallConfig.
     */
    private void eseguiServiceTask(TaskProcesso task, ProcessoDocumento processo) {
        // Prima prova AutoTaskConfig (nuovo formato dal frontend)
        if (task.getAutoTaskConfig() != null) {
            try {
                AutoTaskConfig cfg = objectMapper.readValue(task.getAutoTaskConfig(), AutoTaskConfig.class);
                dispatchAutoTask(task, processo, cfg);
                return;
            } catch (Exception e) {
                log.warn("AutoTaskConfig parse error, fallback to serviceCallConfig: {}", e.getMessage());
            }
        }
        // Fallback: serviceCallConfig (vecchio formato / BPMN hardcoded)
        if (task.getServiceCallConfig() == null) return;
        try {
            ServiceCallConfig cfg = objectMapper.readValue(
                    task.getServiceCallConfig(), ServiceCallConfig.class);
            handleApiCall(task, processo, new AutoTaskConfig(
                "API_CALL", cfg.url(), cfg.method(), cfg.body(), cfg.headers(),
                null, null, null, null, null, null, null, null, null)); // outputVar=null
        } catch (Exception e) {
            log.error("ServiceTask [{}] fallito: {}", task.getNomeTask(), e.getMessage());
            task.setApiStatusCode(0);
            task.setApiErrore(e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 1000)) : "errore sconosciuto");
            taskRepo.save(task);
        }
    }

    /**
     * Dispatcher principale per i diversi tipi di AutoTask.
     */
    private void dispatchAutoTask(TaskProcesso task, ProcessoDocumento processo, AutoTaskConfig cfg) {
        String tipo = cfg.tipo() != null ? cfg.tipo().toUpperCase() : "API_CALL";
        try {
            switch (tipo) {
                case "EMAIL"      -> handleEmail(task, processo, cfg);
                case "TIMER"      -> handleTimer(task, processo, cfg);
                case "SCRIPT"     -> handleScript(task, processo, cfg);
                case "WEBHOOK"    -> handleWebhook(task, processo, cfg);
                case "GENERA_PDF" -> handleGeneraPdf(task, processo, cfg);
                default           -> handleApiCall(task, processo, cfg); // API_CALL
            }
        } catch (TimerScheduledException e) {
            // Rilanciato da handleTimer per non completare il task
            throw e;
        } catch (Exception e) {
            log.error("DispatchAutoTask [{}] fallito: {}", task.getNomeTask(), e.getMessage());
            task.setApiStatusCode(0);
            task.setApiErrore(e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 1000)) : "errore sconosciuto");
            taskRepo.save(task);
        }
    }

    /**
     * Costruisce il contesto variabili disponibile per i template degli step successivi.
     *
     * <p>Contiene due insiemi di valori, in ordine di priorità crescente (i secondi
     * sovrascrivono i primi in caso di chiave identica):
     *
     * <ol>
     *   <li><b>variabiliJson del processo</b> – valori persistiti esplicitamente tramite
     *       {@code outputVar} in AutoTaskConfig. Chiave = outputVar scelto (es. "risultatoCalcolo").
     *       Accessibili come {@code ${risultatoCalcolo.campo}}.</li>
     *   <li><b>apiRisposta dei SERVICE_TASK completati</b> – risposta grezza/parsata di ogni step.
     *       Chiave = taskIdBpmn (es. "Task_Login"). Accessibili come {@code ${Task_Login.access_token}}.
     *       Priorità superiore rispetto a variabiliJson per consentire override step-by-step.</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildStepContext(Long processoId) {
        Map<String, Object> ctx = new java.util.LinkedHashMap<>();

        // 1. Carica variabiliJson del processo (valori outputVar persistiti)
        processoRepo.findById(processoId).ifPresent(p -> {
            if (p.getVariabiliJson() != null && !p.getVariabiliJson().isBlank()) {
                try {
                    Map<String, Object> vars = objectMapper.readValue(p.getVariabiliJson(), Map.class);
                    ctx.putAll(vars);
                } catch (Exception ex) {
                    log.warn("Impossibile parsare variabiliJson per processo {}: {}", processoId, ex.getMessage());
                }
            }
        });

        // 2. Aggiungi (con priorità superiore) le risposte dei SERVICE_TASK completati
        taskRepo.findByProcessoId(processoId).stream()
                .filter(t -> t.getTipoTask() == TipoTask.SERVICE_TASK
                          && t.getStatoTask() == StatoTask.COMPLETATO
                          && t.getApiRisposta() != null
                          && !t.getApiRisposta().isBlank())
                .forEach(t -> {
                    try {
                        Object parsed = objectMapper.readValue(t.getApiRisposta(), Object.class);
                        ctx.put(t.getTaskIdBpmn(), parsed);          // Task_Login → {access_token: ...}
                        ctx.put("_raw_" + t.getTaskIdBpmn(), t.getApiRisposta()); // risposta grezza
                    } catch (Exception ex) {
                        // Risposta non è JSON: esponi come stringa grezza
                        ctx.put(t.getTaskIdBpmn(), t.getApiRisposta());
                        ctx.put("_raw_" + t.getTaskIdBpmn(), t.getApiRisposta());
                    }
                });
        return ctx;
    }

    /**
     * Risolve i placeholder {@code ${...}} in un template stringa.
     *
     * <p>Supporta:
     * <ul>
     *   <li>Variabili di processo: {@code processoId}, {@code documentoId}, {@code codiceProcesso}</li>
     *   <li>Output step: {@code Task_Login} → risposta grezza intera</li>
     *   <li>Campi JSON di step: {@code Task_Login.access_token} → valore del campo</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private String resolveTemplate(String template, ProcessoDocumento processo,
                                   Map<String, Object> stepCtx) {
        if (template == null) return null;
        // Pattern: ${qualcosa} oppure ${Task_X.campo}
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher m = p.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String expr = m.group(1).trim();
            String replacement = switch (expr) {
                case "processoId"    -> String.valueOf(processo.getId());
                case "documentoId"   -> processo.getDocumento() != null
                                        ? String.valueOf(processo.getDocumento().getId()) : "null";
                case "codiceProcesso"-> processo.getCodiceProcesso() != null
                                        ? processo.getCodiceProcesso() : "";
                default -> {
                    // Gestione ${Task_Login} oppure ${Task_Login.campo}
                    int dot = expr.indexOf('.');
                    if (dot > 0) {
                        String taskId = expr.substring(0, dot);
                        String field  = expr.substring(dot + 1);
                        Object stepData = stepCtx.get(taskId);
                        if (stepData instanceof Map<?,?> map) {
                            Object val = map.get(field);
                            yield val != null ? String.valueOf(val) : "";
                        }
                        yield "";
                    } else {
                        // ${Task_Login} → intera risposta grezza
                        Object raw = stepCtx.get("_raw_" + expr);
                        if (raw != null) yield String.valueOf(raw);
                        Object data = stepCtx.get(expr);
                        yield data != null ? String.valueOf(data) : "";
                    }
                }
            };
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private void aggiornaStatoDocumento(Documento documento, String taskIdBpmn) {
        if (documento == null) return;
        Documento.StatoDocumento nuovoStato = switch (taskIdBpmn) {
            case "Task_RevisioneMetadati", "Task_AnalisiRichiesta" -> Documento.StatoDocumento.IN_LAVORAZIONE;
            case "Task_Approvazione", "Task_RevioneDoc"            -> Documento.StatoDocumento.IN_REVISIONE;
            case "Task_Archiviazione"                              -> Documento.StatoDocumento.APPROVATO;
            default                                                -> documento.getStatoDocumento();
        };
        documento.setStatoDocumento(nuovoStato);
        documentoRepo.save(documento);
    }

    // ─── Handler per i diversi tipi di AutoTask ───────────────────────────────

    private void handleEmail(TaskProcesso task, ProcessoDocumento processo, AutoTaskConfig cfg) {
        String to      = resolveTemplate(cfg.emailTo(), processo, buildStepContext(processo.getId()));
        String subject = resolveTemplate(cfg.emailSubject(), processo, buildStepContext(processo.getId()));
        String body    = resolveTemplate(cfg.emailBody(), processo, buildStepContext(processo.getId()));
        if (to == null || to.isBlank()) to = "admin@grandimolini.it";
        if (emailService != null) {
            emailService.sendProcessoTaskEmail(to, subject, body, processo);
        }
        task.setApiRisposta("Email inviata a " + to);
        task.setApiStatusCode(200);
        taskRepo.save(task);
    }

    private void handleTimer(TaskProcesso task, ProcessoDocumento processo, AutoTaskConfig cfg) {
        String delay = cfg.delay() != null ? cfg.delay() : "1h";
        LocalDateTime scheduledFor = parseDelay(delay);
        task.setDataEsecuzioneProgrammata(scheduledFor);
        task.setApiRisposta("Timer programmato per " + scheduledFor);
        task.setApiStatusCode(202); // Accepted
        taskRepo.save(task);
        throw new TimerScheduledException("Timer programmato: " + scheduledFor);
    }

    @SuppressWarnings("unchecked")
    private void handleScript(TaskProcesso task, ProcessoDocumento processo, AutoTaskConfig cfg) {
        String action = cfg.action() != null ? cfg.action().toUpperCase() : "";
        String result = switch (action) {
            case "ARCHIVIA_DOCUMENTO" -> {
                if (processo.getDocumento() != null) {
                    processo.getDocumento().setStatoDocumento(Documento.StatoDocumento.ARCHIVIATO);
                    documentoRepo.save(processo.getDocumento());
                }
                yield "Documento archiviato";
            }
            case "CREA_SCADENZA_AUTO" -> {
                try {
                    Map<String, Object> p = objectMapper.readValue(
                        cfg.params() != null ? cfg.params() : "{}", Map.class);
                    int giorni = p.containsKey("giorni") ? ((Number) p.get("giorni")).intValue() : 30;
                    String titolo = (String) p.getOrDefault("titolo", "Scadenza automatica da processo " + processo.getCodiceProcesso());
                    // Nota: assumi che Scadenza abbia costruttore default e setter
                    log.info("Scadenza creata (mock): {} fra {} giorni", titolo, giorni);
                    yield "Scadenza creata: " + titolo;
                } catch (Exception e) {
                    yield "Errore creazione scadenza: " + e.getMessage();
                }
            }
            case "NOTIFICA_INTERNA" -> {
                String msg = resolveTemplate(cfg.params(), processo, buildStepContext(processo.getId()));
                log.info("NOTIFICA INTERNA [{}]: {}", processo.getCodiceProcesso(), msg);
                yield "Notifica interna: " + (msg != null ? msg.substring(0, Math.min(msg.length(), 100)) : "");
            }
            case "AGGIORNA_STATO_DOCUMENTO" -> {
                try {
                    Map<String, Object> p = objectMapper.readValue(
                        cfg.params() != null ? cfg.params() : "{}", Map.class);
                    String stato = (String) p.getOrDefault("stato", "IN_LAVORAZIONE");
                    if (processo.getDocumento() != null) {
                        processo.getDocumento().setStatoDocumento(
                            Documento.StatoDocumento.valueOf(stato));
                        documentoRepo.save(processo.getDocumento());
                    }
                    yield "Stato documento aggiornato a " + stato;
                } catch (Exception e) {
                    yield "Errore aggiornamento stato: " + e.getMessage();
                }
            }
            default -> "Azione non riconosciuta: " + action;
        };
        task.setApiRisposta(result);
        task.setApiStatusCode(200);
        taskRepo.save(task);
    }

    private void handleWebhook(TaskProcesso task, ProcessoDocumento processo, AutoTaskConfig cfg) {
        Map<String, Object> ctx = buildStepContext(processo.getId());
        String body = resolveTemplate(cfg.body(), processo, ctx);
        if (body == null || body.isBlank()) {
            body = """
                {"evento":"processo_avanzato","processoId":%d,"taskNome":"%s","codice":"%s"}
                """.formatted(processo.getId(), task.getNomeTask(), processo.getCodiceProcesso()).trim();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // HMAC signature se presente
        if (cfg.webhookSecret() != null && !cfg.webhookSecret().isBlank()) {
            try {
                javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
                mac.init(new javax.crypto.spec.SecretKeySpec(
                    cfg.webhookSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] sig = mac.doFinal(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                String hex = java.util.HexFormat.of().formatHex(sig);
                headers.set("X-AIA-Signature", "sha256=" + hex);
            } catch (Exception e) {
                log.warn("HMAC generation failed: {}", e.getMessage());
            }
        }
        headers.set("X-AIA-ProcessoId", String.valueOf(processo.getId()));
        headers.set("X-AIA-CodiceProcesso", processo.getCodiceProcesso() != null ? processo.getCodiceProcesso() : "");
        try {
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                cfg.url(), HttpMethod.POST, entity, String.class);
            task.setApiStatusCode(resp.getStatusCode().value());
            String respBody = resp.getBody() != null ? resp.getBody().substring(0, Math.min(resp.getBody().length(), 4000)) : "";
            task.setApiRisposta(respBody);
            if (cfg.outputVar() != null && !cfg.outputVar().isBlank()) {
                mergeOutputVar(processo, cfg.outputVar(), respBody);
            }
        } catch (Exception e) {
            task.setApiStatusCode(0);
            task.setApiErrore(e.getMessage());
        }
        taskRepo.save(task);
    }

    private void handleApiCall(TaskProcesso task, ProcessoDocumento processo, AutoTaskConfig cfg) {
        Map<String, Object> ctx = buildStepContext(processo.getId());
        String body    = resolveTemplate(cfg.body(), processo, ctx);
        String headersRaw = resolveTemplate(cfg.headers(), processo, ctx);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (headersRaw != null) {
            for (String line : headersRaw.split("\n")) {
                int c = line.indexOf(':');
                if (c > 0) headers.set(line.substring(0, c).trim(), line.substring(c+1).trim());
            }
        }
        // Aggiunge automaticamente il token interno per chiamate self-API,
        // evitando la necessità di un JWT. L'header viene strippato da Nginx
        // per le richieste esterne (proxy_set_header X-Internal-Token "").
        if (isInternalUrl(cfg.url())) {
            headers.set(it.grandimolini.aia.security.LocalhostInternalAuthFilter.INTERNAL_TOKEN_HEADER,
                internalSecret);
        }
        try {
            HttpMethod method = HttpMethod.valueOf(cfg.method() != null ? cfg.method().toUpperCase() : "POST");
            ResponseEntity<String> resp = restTemplate.exchange(
                cfg.url(), method, new HttpEntity<>(body, headers), String.class);
            task.setApiStatusCode(resp.getStatusCode().value());
            String respBody = resp.getBody() != null ? resp.getBody().substring(0, Math.min(resp.getBody().length(), 4000)) : "";
            task.setApiRisposta(respBody);
            if (cfg.outputVar() != null && !cfg.outputVar().isBlank()) {
                mergeOutputVar(processo, cfg.outputVar(), respBody);
            }
        } catch (Exception e) {
            task.setApiStatusCode(0);
            task.setApiErrore(e.getMessage());
        }
        taskRepo.save(task);
    }

    /**
     * Restituisce true se l'URL punta all'applicazione stessa (self-call).
     * Sono considerati interni:
     * - URL relativi che iniziano con "/" (es. "/api/rilevazioni/calcola")
     * - URL assoluti che puntano a localhost/127.0.0.1 (es. "http://localhost:8080/api/...")
     */
    private boolean isInternalUrl(String url) {
        if (url == null) return false;
        if (url.startsWith("/")) return true;
        return url.startsWith("http://localhost") || url.startsWith("http://127.0.0.1");
    }

    /**
     * Persiste la response di un SERVICE_TASK in {@code ProcessoDocumento.variabiliJson}
     * sotto la chiave {@code outputVar}.
     *
     * <p>Se la response è JSON valido viene salvata come oggetto (navigabile negli step
     * successivi via buildStepContext); altrimenti come stringa grezza.
     *
     * <p>Nota: la comunicazione inter-step tramite {@code ${Task_NomeStep.campo}} funziona
     * già senza outputVar (via buildStepContext). Usare outputVar solo quando si vuole:
     * <ul>
     *   <li>dare un nome semantico indipendente dal taskIdBpmn</li>
     *   <li>esporre il valore nell'API esterna (GET /api/processi/{id}/variabili)</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private void mergeOutputVar(ProcessoDocumento processo, String outputVar, String responseBody) {
        try {
            // Leggi la mappa corrente (o crea una nuova)
            Map<String, Object> variabili = new java.util.LinkedHashMap<>();
            if (processo.getVariabiliJson() != null && !processo.getVariabiliJson().isBlank()) {
                try {
                    variabili = objectMapper.readValue(processo.getVariabiliJson(), Map.class);
                } catch (Exception ignored) {
                    // variabiliJson corrotto: riparti da mappa vuota
                    log.warn("variabiliJson corrotto per processo {}, sarà sovrascritto", processo.getId());
                }
            }
            // Prova a parsare la response come JSON; se fallisce salva come stringa
            Object valoreParsato;
            try {
                valoreParsato = objectMapper.readValue(responseBody, Object.class);
            } catch (Exception ignored) {
                valoreParsato = responseBody;
            }
            variabili.put(outputVar, valoreParsato);
            processo.setVariabiliJson(objectMapper.writeValueAsString(variabili));
            processoRepo.save(processo);
            log.debug("outputVar '{}' persistito in processo {}", outputVar, processo.getId());
        } catch (Exception e) {
            log.warn("Impossibile persistere outputVar '{}' in processo {}: {}",
                outputVar, processo.getId(), e.getMessage());
        }
    }

    private void handleGeneraPdf(TaskProcesso task, ProcessoDocumento processo, AutoTaskConfig cfg) {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("Riepilogo Processo - " + (processo.getCodiceProcesso() != null ? processo.getCodiceProcesso() : "N/A"));
                cs.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(0, -30);
                cs.showText("Tipo: " + (processo.getTipoProcesso() != null ? processo.getTipoProcesso().name() : "Custom"));
                cs.newLineAtOffset(0, -20);
                cs.showText("Stato: " + processo.getStato().name());
                cs.newLineAtOffset(0, -20);
                cs.showText("Avviato da: " + (processo.getAvviatoDa() != null ? processo.getAvviatoDa() : "N/A"));
                cs.newLineAtOffset(0, -20);
                if (processo.getDocumento() != null) {
                    cs.showText("Documento: " + processo.getDocumento().getNome());
                    cs.newLineAtOffset(0, -20);
                }
                cs.showText("Generato il: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                cs.endText();
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            doc.save(baos);
            String base64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
            task.setApiRisposta("PDF generato (" + baos.size() + " bytes). base64:" + base64.substring(0, Math.min(base64.length(), 100)) + "...");
            task.setApiStatusCode(200);
            log.info("PDF generato per processo {}: {} bytes", processo.getCodiceProcesso(), baos.size());
        } catch (Exception e) {
            task.setApiStatusCode(0);
            task.setApiErrore("Errore PDF: " + e.getMessage());
        }
        taskRepo.save(task);
    }

    private LocalDateTime parseDelay(String delay) {
        if (delay == null) return LocalDateTime.now().plusHours(1);
        try {
            if (delay.endsWith("m")) return LocalDateTime.now().plusMinutes(Long.parseLong(delay.replace("m", "")));
            if (delay.endsWith("h")) return LocalDateTime.now().plusHours(Long.parseLong(delay.replace("h", "")));
            if (delay.endsWith("d")) return LocalDateTime.now().plusDays(Long.parseLong(delay.replace("d", "")));
        } catch (Exception ignored) {}
        return LocalDateTime.now().plusHours(1);
    }

    /**
     * Sblocca un timer task che è scaduto.
     * Completa il task TIMER e attiva il prossimo (con supporto per BPMN dinamico).
     */
    @Transactional
    public void avanzaDaTimer(Long processoId, Long taskId) {
        TaskProcesso task = taskRepo.findById(taskId).orElseThrow();
        ProcessoDocumento processo = processoRepo.findById(processoId).orElseThrow();

        // Completa il timer task
        task.setStatoTask(StatoTask.COMPLETATO);
        task.setDataCompletamento(LocalDateTime.now());
        task.setCompletatoDa("scheduler");
        task.setApiRisposta("Timer scaduto - eseguito automaticamente");
        taskRepo.save(task);

        if (processo.getDefinizioneFlusso() != null) {
            // Modalità dinamica: usa il graph traversal
            try {
                BpmnGraph graph = bpmnParser.parseGraph(processo.getDefinizioneFlusso().getBpmnXml());
                if (task.getParallelGroupId() != null) {
                    List<TaskProcesso> gruppo = taskRepo.findByProcessoIdAndParallelGroupId(
                            processoId, task.getParallelGroupId());
                    boolean tutti = gruppo.stream()
                            .allMatch(t -> t.getStatoTask() == StatoTask.COMPLETATO);
                    if (tutti && task.getJoinGatewayId() != null) {
                        avanzaOltre(processo, task.getJoinGatewayId(), null, graph);
                    }
                } else {
                    avanzaOltre(processo, task.getTaskIdBpmn(), null, graph);
                }
            } catch (Exception e) {
                log.error("Errore avanzaDaTimer BPMN per processo {}: {}", processoId, e.getMessage());
            }
        } else {
            // Modalità hardcoded lineare
            List<TaskProcesso> tutti = taskRepo.findByProcessoId(processoId);
            TaskProcesso prossimo = tutti.stream()
                .filter(t -> t.getStatoTask() == StatoTask.CREATO)
                .findFirst().orElse(null);
            if (prossimo != null) {
                prossimo.setStatoTask(StatoTask.IN_CORSO);
                prossimo.setAssegnatoA(processo.getAssegnatoA());
                taskRepo.save(prossimo);
                processo.setTaskCorrente(prossimo.getNomeTask());
                processo.setStato(StatoProcesso.IN_CORSO);
            } else {
                completaProcesso(processo);
            }
        }
        processoRepo.save(processo);
    }

    /**
     * Eccezione lanciata da handleTimer per non completare il task automaticamente.
     */
    static class TimerScheduledException extends RuntimeException {
        TimerScheduledException(String msg) { super(msg); }
    }
}
