package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.*;
import it.grandimolini.aia.service.EstrazioneDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller per il flusso OCR / AI → revisione → conferma.
 *
 * Endpoints:
 *   POST /api/estrazione/{documentoId}/analizza         → avvia estrazione, ritorna PropostaEstrazione
 *   POST /api/estrazione/{documentoId}/conferma         → conferma proposta, crea entità, avanza BPM
 *
 * NOTA ARCHITETTURALE: Questo controller delega TUTTO al servizio.
 * Non istanzia, non modifica, non gestisce entità; solo orchestrazione HTTP.
 */
@RestController
@RequestMapping("/api/estrazione")
public class EstrazioneController {

    @Autowired private EstrazioneDocumentoService estrazioneService;

    /**
     * Esegue l'estrazione OCR/AI e restituisce la proposta da revisionare.
     * Avvia anche il processo BPM "LAVORAZIONE_DOCUMENTO" se non già attivo.
     */
    @PostMapping("/{documentoId}/analizza")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE','OPERATORE')")
    public ResponseEntity<PropostaEstrazione> analizza(
            @PathVariable Long documentoId,
            Authentication auth) {

        PropostaEstrazione proposta = estrazioneService.analizzaEAvviaProcesso(documentoId, auth.getName());
        return ResponseEntity.ok(proposta);
    }

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
        ConfermaEstrazioneResponse response = estrazioneService.confermaEstrazione(documentoId, req, auth.getName());
        return ResponseEntity.ok(response);
    }
}
