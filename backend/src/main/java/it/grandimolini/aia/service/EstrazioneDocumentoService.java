package it.grandimolini.aia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.grandimolini.aia.dto.PropostaEstrazione;
import it.grandimolini.aia.dto.PropostaEstrazione.*;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.Documento;
import it.grandimolini.aia.repository.DocumentoRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servizio di estrazione entità da documenti AIA.
 *
 * Pipeline:
 * 1. Estrazione testo: PDFBox per PDF con testo embedded, fallback regex su filename
 * 2. Analisi: Regex intelligenti specifiche per documenti ambientali italiani
 * 3. (Opzionale) AI Enhancement: se configurata una ANTHROPIC_API_KEY, arricchisce
 *    l'estrazione con Claude per una comprensione semantica del testo
 */
@Service
public class EstrazioneDocumentoService {

    @Autowired private DocumentoRepository documentoRepository;
    @Autowired private FileStorageService fileStorageService;

    @Value("${aia.estrazione.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${aia.estrazione.ai.api-key:}")
    private String anthropicApiKey;

    @Value("${aia.estrazione.ai.model:claude-haiku-4-5-20251001}")
    private String aiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger tempIdCounter = new AtomicInteger(1);

    // ─── Patterns per analisi testo italiano ────────────────────────────────

    // Date: 01/03/2026, 1-3-2026, 01.03.2026
    private static final Pattern DATE_IT = Pattern.compile(
        "\\b(\\d{1,2})[/\\-.](\\d{1,2})[/\\-.](\\d{4})\\b"
    );
    // "entro X giorni", "entro N mesi"
    private static final Pattern ENTRO_GIORNI = Pattern.compile(
        "(?i)entro\\s+(\\d+)\\s+(giorni|mesi|settimane)",
        Pattern.CASE_INSENSITIVE
    );
    // Parole chiave di scadenza nel contesto
    private static final Pattern SCADENZA_CONTEXT = Pattern.compile(
        "(?i)(scadenz[ae]|termin[ei]|entro|comunicare|inviare|trasmettere|presentare|depositare)" +
        ".{0,80}?" +
        "(\\d{1,2}[/\\-.](\\d{1,2})[/\\-.](\\d{4})|entro\\s+\\d+\\s+(?:giorni|mesi))",
        Pattern.DOTALL
    );
    // Codici prescrizione: A.1, B.3, 4.2.1, Punto 3.1
    private static final Pattern PRESCRIZIONE_CODE = Pattern.compile(
        "(?i)(?:punto|prescrizione|art(?:icolo)?\\.|condizione)\\s*([A-Z]?\\.?\\d+(?:\\.\\d+)*)" +
        "|\\b([A-Z]\\.(\\d+)(?:\\.\\d+)*)\\b"
    );
    // Enti emittenti comuni
    private static final Pattern ENTE_EMITTENTE = Pattern.compile(
        "(?i)(ARPA\\s+\\w+|Regione\\s+\\w+|Provincia\\s+(?:di\\s+)?\\w+|" +
        "ISPRA|Ministero\\s+dell'[Aa]mbiente|ARPAE|ARPAV|ARPAL|ARPAS)"
    );
    // Numero protocollo
    private static final Pattern PROTOCOLLO = Pattern.compile(
        "(?i)(?:prot(?:ocollo)?\\s*\\.?\\s*n\\s*\\.?\\s*|n\\s*\\.?\\s*prot\\s*\\.?)\\s*([A-Z0-9/\\-\\.]+)",
        Pattern.CASE_INSENSITIVE
    );
    // Oggetto / titolo
    private static final Pattern OGGETTO = Pattern.compile(
        "(?i)OGGETTO\\s*:\\s*(.{10,200}?)(?:\\n|\\r|$)"
    );
    // Numero AIA
    private static final Pattern NUMERO_AIA = Pattern.compile(
        "(?i)(?:AIA|Autorizzazione Integrata Ambientale)\\s*n\\s*\\.?\\s*([A-Z0-9/\\-]+)"
    );
    // Tipi di prescrizione
    private static final Map<String, String> TIPI_PRESCRIZIONE = Map.of(
        "emissioni? atmosferic", "EMISSIONI_ATMOSFERICHE",
        "scarich[io] idr", "SCARICHI_IDRICI",
        "rifiut[io]", "GESTIONE_RIFIUTI",
        "rumore|acustic", "EMISSIONI_ACUSTICHE",
        "monitoragg", "MONITORAGGIO",
        "suolo|sottosuolo", "SUOLO_SOTTOSUOLO"
    );

    // ─── Entry point ─────────────────────────────────────────────────────────

    public PropostaEstrazione analizzaDocumento(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));

        PropostaEstrazione proposta = new PropostaEstrazione();
        proposta.setDocumentoId(documentoId);
        proposta.setNomeFile(documento.getNomeFile());

        // 1. Estrai testo
        String testo = estraiTesto(documento, proposta);
        proposta.setTestoEstratto(testo);

        // 2. Analisi regex
        if (testo != null && !testo.isBlank()) {
            analizzaConRegex(testo, proposta);

            // 3. Arricchimento AI (se abilitato e testo disponibile)
            if (aiEnabled && anthropicApiKey != null && !anthropicApiKey.isBlank()) {
                try {
                    arricchisciConAI(testo, proposta);
                } catch (Exception e) {
                    proposta.setAvvisi((proposta.getAvvisi() != null ? proposta.getAvvisi() + "; " : "")
                            + "AI non disponibile: " + e.getMessage());
                }
            }
        } else {
            proposta.setAvvisi("Impossibile estrarre testo dal documento. " +
                "Potrebbe essere un'immagine scansionata senza OCR. " +
                "Inserire manualmente le informazioni.");
            proposta.setMetodo(MetodoEstrazione.FALLBACK_REGEX);
            proposta.setConfidenza(0.1);
        }

        return proposta;
    }

    // ─── Estrazione testo ─────────────────────────────────────────────────────

    private String estraiTesto(Documento documento, PropostaEstrazione proposta) {
        String mimeType = documento.getMimeType();

        // PDF con testo embedded
        if ("application/pdf".equals(mimeType) || documento.getNomeFile().toLowerCase().endsWith(".pdf")) {
            try {
                String filePath = documento.getFilePath();
                File file = fileStorageService.resolveFilePath(filePath);
                if (file.exists()) {
                    try (PDDocument pdf = Loader.loadPDF(file)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        String testo = stripper.getText(pdf);
                        if (testo != null && testo.trim().length() > 50) {
                            proposta.setMetodo(MetodoEstrazione.PDFBOX_TEXT);
                            proposta.setConfidenza(0.75);
                            return testo;
                        }
                    }
                }
            } catch (Exception e) {
                proposta.setAvvisi("Errore lettura PDF: " + e.getMessage());
            }
        }

        // Testo già estratto e salvato in precedenza
        if (documento.getTestoEstratto() != null && !documento.getTestoEstratto().isBlank()) {
            proposta.setMetodo(MetodoEstrazione.PDFBOX_TEXT);
            proposta.setConfidenza(0.7);
            return documento.getTestoEstratto();
        }

        proposta.setMetodo(MetodoEstrazione.FALLBACK_REGEX);
        proposta.setConfidenza(0.2);
        return null;
    }

    // ─── Analisi regex ────────────────────────────────────────────────────────

    private void analizzaConRegex(String testo, PropostaEstrazione proposta) {
        MetadatiDocumento meta = proposta.getMetadati();

        // Oggetto
        Matcher m = OGGETTO.matcher(testo);
        if (m.find()) {
            meta.setOggetto(m.group(1).trim().replaceAll("\\s+", " "));
        }

        // Ente emittente
        m = ENTE_EMITTENTE.matcher(testo);
        if (m.find()) meta.setEnteEmittente(m.group(1).trim());

        // Numero protocollo
        m = PROTOCOLLO.matcher(testo);
        if (m.find()) meta.setNumeroProtocollo(m.group(1).trim());

        // AIA reference
        m = NUMERO_AIA.matcher(testo);
        if (m.find()) meta.setRiferimentoAIA(m.group(1).trim());

        // Prima data trovata → data ricezione
        m = DATE_IT.matcher(testo);
        if (m.find()) {
            String dataStr = normalizzaData(m.group(1), m.group(2), m.group(3));
            if (dataStr != null) meta.setDataRicezione(dataStr);
        }

        // Estrai scadenze
        estraiScadenze(testo, proposta);

        // Estrai prescrizioni
        estraiPrescrizioni(testo, proposta);
    }

    private void estraiScadenze(String testo, PropostaEstrazione proposta) {
        Set<String> dataGiàViste = new HashSet<>();

        // Pattern: contesto scadenza + data
        Matcher m = SCADENZA_CONTEXT.matcher(testo);
        while (m.find()) {
            String contestoCompleto = m.group(0);
            String verboAzione = m.group(1);

            // Cerca data nell'intero match
            Matcher dataMatcher = DATE_IT.matcher(contestoCompleto);
            while (dataMatcher.find()) {
                String dataIso = normalizzaData(dataMatcher.group(1), dataMatcher.group(2), dataMatcher.group(3));
                if (dataIso == null || dataGiàViste.contains(dataIso)) continue;
                if (!isDataFutura(dataIso)) continue;
                dataGiàViste.add(dataIso);

                ScadenzaProposta sp = new ScadenzaProposta();
                sp.setTempId("SC-" + tempIdCounter.getAndIncrement());
                sp.setDataScadenza(dataIso);
                sp.setTitolo(inferisciTitoloScadenza(verboAzione, contestoCompleto));
                sp.setDescrizione(pulisciTestoBreve(contestoCompleto));
                sp.setTipo(inferisciTipoScadenza(contestoCompleto));
                sp.setTestoOrigine(contestoCompleto.length() > 200
                    ? contestoCompleto.substring(0, 200) + "…" : contestoCompleto);
                sp.setConfidenza(0.70);
                sp.setSelezionata(true);
                proposta.getScadenzeProposte().add(sp);
            }

            // Cerca "entro N giorni"
            Matcher giorniM = ENTRO_GIORNI.matcher(contestoCompleto);
            if (giorniM.find()) {
                int n = Integer.parseInt(giorniM.group(1));
                String unita = giorniM.group(2).toLowerCase();
                LocalDate dataCalcolata = switch (unita) {
                    case "giorni"      -> LocalDate.now().plusDays(n);
                    case "settimane"   -> LocalDate.now().plusWeeks(n);
                    default            -> LocalDate.now().plusMonths(n);
                };
                String dataIso = dataCalcolata.toString();
                if (!dataGiàViste.contains(dataIso)) {
                    dataGiàViste.add(dataIso);
                    ScadenzaProposta sp = new ScadenzaProposta();
                    sp.setTempId("SC-" + tempIdCounter.getAndIncrement());
                    sp.setDataScadenza(dataIso);
                    sp.setGiorni(n);
                    sp.setTitolo(inferisciTitoloScadenza(verboAzione, contestoCompleto));
                    sp.setDescrizione("Scadenza calcolata: " + n + " " + unita + " dalla data del documento");
                    sp.setTipo(inferisciTipoScadenza(contestoCompleto));
                    sp.setTestoOrigine(contestoCompleto.length() > 200
                        ? contestoCompleto.substring(0, 200) + "…" : contestoCompleto);
                    sp.setConfidenza(0.60);
                    sp.setSelezionata(true);
                    proposta.getScadenzeProposte().add(sp);
                }
            }
        }
    }

    private void estraiPrescrizioni(String testo, PropostaEstrazione proposta) {
        Set<String> codiciVisti = new HashSet<>();
        Matcher m = PRESCRIZIONE_CODE.matcher(testo);

        while (m.find()) {
            String codice = (m.group(1) != null ? m.group(1) : m.group(2)).trim();
            if (codiciVisti.contains(codice)) continue;
            codiciVisti.add(codice);

            // Estrai contesto (200 char dopo il match)
            int start = Math.max(0, m.start() - 30);
            int end = Math.min(testo.length(), m.end() + 300);
            String contesto = testo.substring(start, end).trim();

            PrescrizioneProposta pp = new PrescrizioneProposta();
            pp.setTempId("PR-" + tempIdCounter.getAndIncrement());
            pp.setCodice(codice);
            pp.setDescrizione(pulisciTestoBreve(contesto));
            pp.setTipo(inferisciTipoPrescrizione(contesto));
            pp.setTestoOrigine(contesto.length() > 250 ? contesto.substring(0, 250) + "…" : contesto);
            pp.setConfidenza(0.65);
            pp.setSelezionata(false); // prescrizioni: non pre-selezionate, l'utente sceglie
            proposta.getPrescrizioniProposte().add(pp);
        }
    }

    // ─── Arricchimento AI (Anthropic API) ────────────────────────────────────

    private void arricchisciConAI(String testo, PropostaEstrazione proposta) throws Exception {
        // Tronca il testo a 4000 caratteri per l'API
        String testoTroncato = testo.length() > 4000 ? testo.substring(0, 4000) + "\n[TESTO TRONCATO]" : testo;

        String prompt = """
            Sei un assistente esperto in documenti ambientali italiani (AIA - Autorizzazione Integrata Ambientale).
            Analizza il seguente testo estratto da un documento e restituisci un JSON strutturato.

            TESTO DEL DOCUMENTO:
            ---
            %s
            ---

            Restituisci SOLO un oggetto JSON valido con questa struttura (nessun testo prima o dopo):
            {
              "oggetto": "oggetto/titolo del documento o null",
              "enteEmittente": "ente che ha emesso il documento o null",
              "numeroProtocollo": "numero protocollo o null",
              "dataRicezione": "YYYY-MM-DD o null",
              "scadenze": [
                {
                  "titolo": "titolo breve della scadenza",
                  "descrizione": "descrizione dell'adempimento richiesto",
                  "dataScadenza": "YYYY-MM-DD",
                  "tipo": "uno tra: MONITORAGGIO, REPORTING, COMUNICAZIONE, CAMPIONAMENTO, MANUTENZIONE",
                  "testoOrigine": "frase del documento da cui è stata estratta",
                  "confidenza": 0.0-1.0
                }
              ],
              "prescrizioni": [
                {
                  "codice": "codice prescrizione es. A.3",
                  "descrizione": "descrizione della prescrizione",
                  "tipo": "uno tra: EMISSIONI_ATMOSFERICHE, SCARICHI_IDRICI, GESTIONE_RIFIUTI, EMISSIONI_ACUSTICHE, MONITORAGGIO, SUOLO_SOTTOSUOLO, ALTRO",
                  "testoOrigine": "frase del documento",
                  "confidenza": 0.0-1.0
                }
              ]
            }
            """.formatted(testoTroncato);

        String requestBody = objectMapper.writeValueAsString(Map.of(
            "model", aiModel,
            "max_tokens", 2000,
            "messages", List.of(Map.of("role", "user", "content", prompt))
        ));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", anthropicApiKey)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API AI ha risposto con status " + response.statusCode());
        }

        // Parsa la risposta dell'API
        JsonNode apiResp = objectMapper.readTree(response.body());
        String content = apiResp.at("/content/0/text").asText();

        // Estrai JSON dal contenuto
        int jsonStart = content.indexOf('{');
        int jsonEnd = content.lastIndexOf('}');
        if (jsonStart < 0 || jsonEnd < 0) return;

        JsonNode extracted = objectMapper.readTree(content.substring(jsonStart, jsonEnd + 1));

        // Aggiorna metadati se mancanti
        MetadatiDocumento meta = proposta.getMetadati();
        if (meta.getOggetto() == null && extracted.has("oggetto") && !extracted.get("oggetto").isNull()) {
            meta.setOggetto(extracted.get("oggetto").asText());
        }
        if (meta.getEnteEmittente() == null && extracted.has("enteEmittente") && !extracted.get("enteEmittente").isNull()) {
            meta.setEnteEmittente(extracted.get("enteEmittente").asText());
        }
        if (meta.getNumeroProtocollo() == null && extracted.has("numeroProtocollo") && !extracted.get("numeroProtocollo").isNull()) {
            meta.setNumeroProtocollo(extracted.get("numeroProtocollo").asText());
        }
        if (meta.getDataRicezione() == null && extracted.has("dataRicezione") && !extracted.get("dataRicezione").isNull()) {
            meta.setDataRicezione(extracted.get("dataRicezione").asText());
        }

        // Aggiungi scadenze AI (rimuovi duplicati per data)
        Set<String> dateEsistenti = new HashSet<>();
        proposta.getScadenzeProposte().forEach(s -> dateEsistenti.add(s.getDataScadenza()));

        if (extracted.has("scadenze") && extracted.get("scadenze").isArray()) {
            for (JsonNode sc : extracted.get("scadenze")) {
                String dataIso = sc.has("dataScadenza") ? sc.get("dataScadenza").asText() : null;
                if (dataIso == null || dataIso.isBlank() || dateEsistenti.contains(dataIso)) continue;

                ScadenzaProposta sp = new ScadenzaProposta();
                sp.setTempId("SC-AI-" + tempIdCounter.getAndIncrement());
                sp.setTitolo(sc.has("titolo") ? sc.get("titolo").asText() : "Scadenza");
                sp.setDescrizione(sc.has("descrizione") ? sc.get("descrizione").asText() : null);
                sp.setDataScadenza(dataIso);
                sp.setTipo(sc.has("tipo") ? sc.get("tipo").asText() : "COMUNICAZIONE");
                sp.setTestoOrigine(sc.has("testoOrigine") ? sc.get("testoOrigine").asText() : null);
                sp.setConfidenza(sc.has("confidenza") ? sc.get("confidenza").asDouble() : 0.85);
                sp.setSelezionata(true);
                proposta.getScadenzeProposte().add(sp);
                dateEsistenti.add(dataIso);
            }
        }

        // Aggiungi prescrizioni AI
        Set<String> codiciEsistenti = new HashSet<>();
        proposta.getPrescrizioniProposte().forEach(p -> codiciEsistenti.add(p.getCodice()));

        if (extracted.has("prescrizioni") && extracted.get("prescrizioni").isArray()) {
            for (JsonNode pr : extracted.get("prescrizioni")) {
                String codice = pr.has("codice") ? pr.get("codice").asText() : null;
                if (codice == null || codiciEsistenti.contains(codice)) continue;

                PrescrizioneProposta pp = new PrescrizioneProposta();
                pp.setTempId("PR-AI-" + tempIdCounter.getAndIncrement());
                pp.setCodice(codice);
                pp.setDescrizione(pr.has("descrizione") ? pr.get("descrizione").asText() : null);
                pp.setTipo(pr.has("tipo") ? pr.get("tipo").asText() : "ALTRO");
                pp.setTestoOrigine(pr.has("testoOrigine") ? pr.get("testoOrigine").asText() : null);
                pp.setConfidenza(pr.has("confidenza") ? pr.get("confidenza").asDouble() : 0.80);
                pp.setSelezionata(false);
                proposta.getPrescrizioniProposte().add(pp);
                codiciEsistenti.add(codice);
            }
        }

        // Aggiorna metodo e confidenza
        proposta.setMetodo(MetodoEstrazione.AI_EXTRACTION);
        proposta.setConfidenza(Math.min(1.0, (proposta.getConfidenza() != null ? proposta.getConfidenza() : 0.5) + 0.20));
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private String normalizzaData(String giorno, String mese, String anno) {
        try {
            int g = Integer.parseInt(giorno);
            int m = Integer.parseInt(mese);
            int a = Integer.parseInt(anno);
            if (a < 100) a += 2000;
            if (g < 1 || g > 31 || m < 1 || m > 12 || a < 2000 || a > 2040) return null;
            return LocalDate.of(a, m, g).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isDataFutura(String dataIso) {
        try {
            LocalDate d = LocalDate.parse(dataIso);
            // Accetta date a partire da un anno fa (per documenti ricevuti in ritardo)
            return d.isAfter(LocalDate.now().minusYears(1));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String inferisciTitoloScadenza(String verboAzione, String contesto) {
        if (verboAzione == null) return "Scadenza adempimento";
        String v = verboAzione.toLowerCase();
        if (v.contains("comunicare") || v.contains("trasmettere")) return "Comunicazione agli enti";
        if (v.contains("presentare") || v.contains("depositare"))  return "Presentazione documentazione";
        if (v.contains("inviare"))     return "Invio documentazione";
        if (v.contains("monitoragg"))  return "Monitoraggio ambientale";
        if (v.contains("scadenza") || v.contains("termine")) return "Adempimento prescrizione";
        return "Adempimento richiesto";
    }

    private String inferisciTipoScadenza(String contesto) {
        String c = contesto.toLowerCase();
        if (c.contains("monitoragg") || c.contains("campion")) return "MONITORAGGIO";
        if (c.contains("relazione") || c.contains("report"))   return "REPORTING";
        if (c.contains("comunicaz") || c.contains("notifica")) return "COMUNICAZIONE";
        if (c.contains("analisi") || c.contains("misuraz"))    return "CAMPIONAMENTO";
        if (c.contains("manutenz") || c.contains("verifica"))  return "MANUTENZIONE";
        return "COMUNICAZIONE";
    }

    private String inferisciTipoPrescrizione(String contesto) {
        String c = contesto.toLowerCase();
        for (Map.Entry<String, String> e : TIPI_PRESCRIZIONE.entrySet()) {
            if (c.matches("(?s).*" + e.getKey() + ".*")) return e.getValue();
        }
        return "ALTRO";
    }

    private String pulisciTestoBreve(String testo) {
        if (testo == null) return "";
        return testo.replaceAll("\\s+", " ").trim();
    }
}
