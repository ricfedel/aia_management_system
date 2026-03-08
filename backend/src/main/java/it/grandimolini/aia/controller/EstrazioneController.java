package it.grandimolini.aia.controller;

import it.grandimolini.aia.config.DataInitializer;
import it.grandimolini.aia.dto.*;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.*;
import it.grandimolini.aia.repository.DefinizioneFlussoRepository;
import it.grandimolini.aia.repository.DocumentoRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import it.grandimolini.aia.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller per il flusso OCR / AI → revisione → conferma.
 *
 * Endpoints:
 *   POST /api/estrazione/{documentoId}/analizza         → avvia estrazione, ritorna PropostaEstrazione
 *   POST /api/estrazione/{documentoId}/conferma         → conferma proposta, crea entità, avanza BPM
 */
@RestController
@RequestMapping("/api/estrazione")
public class EstrazioneController {

    @Autowired private EstrazioneDocumentoService estrazioneService;
    @Autowired private DocumentoRepository documentoRepository;
    @Autowired private StabilimentoRepository stabilimentoRepository;
    @Autowired private DefinizioneFlussoRepository definizioneFlussoRepo;
    @Autowired private ScadenzaService scadenzaService;
    @Autowired private PrescrizioneService prescrizioneService;
    @Autowired private DocumentoService documentoService;
    @Autowired private BpmService bpmService;

    // ─── Step 1: Analisi ─────────────────────────────────────────────────────

    /**
     * Esegue l'estrazione OCR/AI e restituisce la proposta da revisionare.
     * Avvia anche il processo BPM "LAVORAZIONE_DOCUMENTO" se non già attivo.
     */
    @PostMapping("/{documentoId}/analizza")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE','OPERATORE')")
    public ResponseEntity<PropostaEstrazione> analizza(
            @PathVariable Long documentoId,
            Authentication auth) {

        PropostaEstrazione proposta = estrazioneService.analizzaDocumento(documentoId);

        // Aggiorna stato documento → RICEVUTO (se era BOZZA)
        Documento doc = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));
        if (doc.getStatoDocumento() == null
                || doc.getStatoDocumento() == Documento.StatoDocumento.BOZZA) {
            documentoService.aggiornaMetadatiDms(documentoId,
                    Documento.StatoDocumento.RICEVUTO, null, null, null, null);
        }

        // Avvia processo BPM se non già presente
        List<ProcessoDocumento> processiEsistenti = bpmService.getProcessiByDocumento(documentoId);
        boolean haProcessoAttivo = processiEsistenti.stream()
                .anyMatch(p -> p.getStato() != ProcessoDocumento.StatoProcesso.COMPLETATO
                            && p.getStato() != ProcessoDocumento.StatoProcesso.ANNULLATO);

        if (!haProcessoAttivo) {
            AvviaProcessoRequest avviaReq = new AvviaProcessoRequest();
            avviaReq.setDocumentoId(documentoId);
            avviaReq.setStabilimentoId(doc.getStabilimento() != null ? doc.getStabilimento().getId() : null);
            avviaReq.setAssegnatoA(auth.getName());
            avviaReq.setNote("Avviato automaticamente da analisi documento");

            // Cerca la DefinizioneFlusso standard per l'estrazione; se trovata, usa
            // il flusso BPMN personalizzabile invece del tipo hardcoded.
            definizioneFlussoRepo.findByAttivaTrue().stream()
                    .filter(df -> DataInitializer.FLUSSO_ESTRAZIONE_NOME.equals(df.getNome()))
                    .findFirst()
                    .ifPresentOrElse(
                        df -> avviaReq.setDefinizioneFlussoId(df.getId()),
                        ()  -> avviaReq.setTipoProcesso(
                                   ProcessoDocumento.TipoProcesso.LAVORAZIONE_DOCUMENTO)
                    );

            try {
                bpmService.avviaProcesso(avviaReq, auth.getName());
            } catch (Exception e) {
                // Il processo potrebbe già esistere in una race condition - ignora
            }
        }

        return ResponseEntity.ok(proposta);
    }

    // ─── Step 2: Conferma ────────────────────────────────────────────────────

    /**
     * L'operatore ha revisionato la proposta e conferma la creazione delle entità.
     * Il sistema crea scadenze e prescrizioni, aggiorna il documento e avanza il task BPM.
     */
    @PostMapping("/{documentoId}/conferma")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE','OPERATORE')")
    public ResponseEntity<ConfermaEstrazioneResponse> conferma(
            @PathVariable Long documentoId,
            @RequestBody ConfermaEstrazioneRequest req,
            Authentication auth) {

        req.setDocumentoId(documentoId);

        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));

        Long stabilimentoId = documento.getStabilimento() != null
                ? documento.getStabilimento().getId() : null;

        List<Long> idScadenze       = new ArrayList<>();
        List<Long> idPrescrizioni   = new ArrayList<>();

        // 1. Aggiorna metadati documento
        if (req.getMetadati() != null) {
            ConfermaEstrazioneRequest.MetadatiConfermati m = req.getMetadati();
            documentoService.aggiornaMetadatiDms(
                documentoId,
                Documento.StatoDocumento.APPROVATO,
                m.getOggetto(),
                m.getEnteEmittente(),
                m.getNumeroProtocollo(),
                null
            );
            // Aggiorna il nome del documento se abbiamo l'oggetto
            if (m.getOggetto() != null && !m.getOggetto().isBlank()) {
                documento.setOggetto(m.getOggetto());
            }
        }

        // 2. Crea Scadenze confermate
        if (req.getScadenze() != null) {
            for (ConfermaEstrazioneRequest.ScadenzaConfermata sc : req.getScadenze()) {
                if (sc.getTitolo() == null || sc.getDataScadenza() == null) continue;

                Long stabId = sc.getStabilimentoId() != null ? sc.getStabilimentoId() : stabilimentoId;
                Stabilimento stab = stabId != null
                        ? stabilimentoRepository.findById(stabId).orElse(documento.getStabilimento())
                        : documento.getStabilimento();

                Scadenza scadenza = new Scadenza();
                scadenza.setStabilimento(stab);
                scadenza.setTitolo(sc.getTitolo());
                scadenza.setDescrizione(sc.getDescrizione());
                scadenza.setDataScadenza(LocalDate.parse(sc.getDataScadenza()));
                scadenza.setTipoScadenza(mapTipoScadenza(sc.getTipo()));
                scadenza.setStato(Scadenza.StatoScadenza.PENDING);
                scadenza.setNote("Creata da analisi documento: " + documento.getNome()
                        + (req.getNoteRevisione() != null ? "\nNote operatore: " + req.getNoteRevisione() : ""));

                Scadenza saved = scadenzaService.save(scadenza);
                idScadenze.add(saved.getId());
            }
        }

        // 3. Crea Prescrizioni confermate
        if (req.getPrescrizioni() != null) {
            for (ConfermaEstrazioneRequest.PrescrizioneConfermata pc : req.getPrescrizioni()) {
                if (pc.getCodice() == null || pc.getDescrizione() == null) continue;

                Long stabId = pc.getStabilimentoId() != null ? pc.getStabilimentoId() : stabilimentoId;
                Stabilimento stab = stabId != null
                        ? stabilimentoRepository.findById(stabId).orElse(documento.getStabilimento())
                        : documento.getStabilimento();

                if (stab == null) continue; // prescrizione richiede stabilimento

                Prescrizione prescrizione = new Prescrizione();
                prescrizione.setStabilimento(stab);
                prescrizione.setCodice(pc.getCodice());
                prescrizione.setDescrizione(pc.getDescrizione());
                prescrizione.setMatriceAmbientale(mapMatriceAmbientale(pc.getTipo()));
                prescrizione.setStato(Prescrizione.StatoPrescrizione.APERTA);
                prescrizione.setDataEmissione(LocalDate.now());
                if (documento.getEnteEmittente() != null) {
                    prescrizione.setEnteEmittente(documento.getEnteEmittente());
                }
                prescrizione.setNote("Estratta da documento: " + documento.getNome()
                        + (req.getNoteRevisione() != null ? "\nNote operatore: " + req.getNoteRevisione() : ""));

                Prescrizione saved = prescrizioneService.save(prescrizione);
                idPrescrizioni.add(saved.getId());
            }
        }

        // 4. Avanza task BPM (se il processo è attivo)
        ProcessoDocumentoDTO processoAggiornato = null;
        if (req.getProcessoId() != null && req.getTaskId() != null) {
            CompletaTaskRequest completaReq = new CompletaTaskRequest();
            completaReq.setEsito("APPROVATO");
            completaReq.setCommento("Estrazione confermata: create " + idScadenze.size()
                    + " scadenze, " + idPrescrizioni.size() + " prescrizioni."
                    + (req.getNoteRevisione() != null ? " Note: " + req.getNoteRevisione() : ""));
            try {
                ProcessoDocumento proc = bpmService.completaTask(
                        req.getProcessoId(), req.getTaskId(), completaReq, auth.getName());
                processoAggiornato = ProcessoDocumentoDTO.fromEntity(proc);
            } catch (Exception e) {
                // Non bloccare la conferma se il BPM fallisce
            }
        } else {
            // Cerca e avanza il primo task attivo collegato al documento
            List<ProcessoDocumento> processi = bpmService.getProcessiByDocumento(documentoId);
            for (ProcessoDocumento proc : processi) {
                if (proc.getStato() == ProcessoDocumento.StatoProcesso.COMPLETATO
                        || proc.getStato() == ProcessoDocumento.StatoProcesso.ANNULLATO) continue;

                proc.getTasks().stream()
                    .filter(t -> t.getStatoTask() == TaskProcesso.StatoTask.IN_CORSO)
                    .findFirst()
                    .ifPresent(taskAttivo -> {
                        CompletaTaskRequest cr = new CompletaTaskRequest();
                        cr.setEsito("APPROVATO");
                        cr.setCommento("Estrazione confermata automaticamente");
                        try {
                            bpmService.completaTask(proc.getId(), taskAttivo.getId(), cr, auth.getName());
                        } catch (Exception ignored) {}
                    });

                processoAggiornato = ProcessoDocumentoDTO.fromEntity(
                        bpmService.getProcessoById(proc.getId()));
                break;
            }
        }

        // 5. Costruisci risposta
        ConfermaEstrazioneResponse response = new ConfermaEstrazioneResponse();
        response.setSuccesso(true);
        response.setMessaggio(String.format(
            "Conferma completata: create %d scadenze e %d prescrizioni.",
            idScadenze.size(), idPrescrizioni.size()
        ));
        response.setScadenzeCreate(idScadenze.size());
        response.setPrescrizioniCreate(idPrescrizioni.size());
        response.setIdScadenzeCreate(idScadenze);
        response.setIdPrescrizioniCreate(idPrescrizioni);
        response.setNuovoStatoDocumento(Documento.StatoDocumento.APPROVATO.name());
        response.setProcessoAggiornato(processoAggiornato);

        return ResponseEntity.ok(response);
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    private Scadenza.TipoScadenza mapTipoScadenza(String tipo) {
        if (tipo == null) return Scadenza.TipoScadenza.ALTRO;
        return switch (tipo.toUpperCase()) {
            case "MONITORAGGIO"  -> Scadenza.TipoScadenza.MONITORAGGIO_PMC;
            case "REPORTING"     -> Scadenza.TipoScadenza.RELAZIONE_ANNUALE;
            case "COMUNICAZIONE" -> Scadenza.TipoScadenza.COMUNICAZIONE;
            case "CAMPIONAMENTO" -> Scadenza.TipoScadenza.MONITORAGGIO_PMC;
            default              -> Scadenza.TipoScadenza.ALTRO;
        };
    }

    private Prescrizione.MatriceAmbientale mapMatriceAmbientale(String tipo) {
        if (tipo == null) return Prescrizione.MatriceAmbientale.ARIA;
        return switch (tipo.toUpperCase()) {
            case "EMISSIONI_ATMOSFERICHE" -> Prescrizione.MatriceAmbientale.ARIA;
            case "SCARICHI_IDRICI"        -> Prescrizione.MatriceAmbientale.ACQUA;
            case "GESTIONE_RIFIUTI"       -> Prescrizione.MatriceAmbientale.RIFIUTI;
            case "EMISSIONI_ACUSTICHE"    -> Prescrizione.MatriceAmbientale.RUMORE;
            case "SUOLO_SOTTOSUOLO"       -> Prescrizione.MatriceAmbientale.SUOLO;
            case "MONITORAGGIO"           -> Prescrizione.MatriceAmbientale.ARIA;
            default                       -> Prescrizione.MatriceAmbientale.ARIA;
        };
    }
}
