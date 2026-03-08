package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.AvviaProcessoRequest;
import it.grandimolini.aia.dto.CompletaTaskRequest;
import it.grandimolini.aia.dto.ProcessoDocumentoDTO;
import it.grandimolini.aia.model.ProcessoDocumento;
import it.grandimolini.aia.model.TaskProcesso;
import it.grandimolini.aia.service.BpmService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bpm")
public class BpmController {

    @Autowired
    private BpmService bpmService;

    // ─── Processi ─────────────────────────────────────────────────────────────

    /** Avvia un nuovo processo BPM su un documento */
    @PostMapping("/processi/avvia")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE','OPERATORE')")
    public ResponseEntity<ProcessoDocumentoDTO> avviaProcesso(
            @Valid @RequestBody AvviaProcessoRequest req,
            Authentication auth) {
        ProcessoDocumento processo = bpmService.avviaProcesso(req, auth.getName());
        return ResponseEntity.ok(ProcessoDocumentoDTO.fromEntity(processo));
    }

    /** Elenco di tutti i processi */
    @GetMapping("/processi")
    public ResponseEntity<List<ProcessoDocumentoDTO>> getAllProcessi() {
        return ResponseEntity.ok(
            bpmService.getAllProcessi().stream()
                .map(ProcessoDocumentoDTO::fromEntity)
                .collect(Collectors.toList())
        );
    }

    /** Elenco processi attivi (non completati / annullati), paginati */
    @GetMapping("/processi/attivi")
    public ResponseEntity<Page<ProcessoDocumentoDTO>> getProcessiAttivi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            bpmService.getProcessiAttiviPaged(pageable)
                .map(ProcessoDocumentoDTO::fromEntity)
        );
    }

    /** Dettaglio singolo processo */
    @GetMapping("/processi/{id}")
    public ResponseEntity<ProcessoDocumentoDTO> getProcesso(@PathVariable Long id) {
        return ResponseEntity.ok(ProcessoDocumentoDTO.fromEntity(bpmService.getProcessoById(id)));
    }

    /** Processi per stabilimento */
    @GetMapping("/processi/stabilimento/{stabilimentoId}")
    public ResponseEntity<List<ProcessoDocumentoDTO>> getProcessiByStabilimento(
            @PathVariable Long stabilimentoId) {
        return ResponseEntity.ok(
            bpmService.getProcessiByStabilimento(stabilimentoId).stream()
                .map(ProcessoDocumentoDTO::fromEntity)
                .collect(Collectors.toList())
        );
    }

    /** Processi collegati a un documento */
    @GetMapping("/processi/documento/{documentoId}")
    public ResponseEntity<List<ProcessoDocumentoDTO>> getProcessiByDocumento(
            @PathVariable Long documentoId) {
        return ResponseEntity.ok(
            bpmService.getProcessiByDocumento(documentoId).stream()
                .map(ProcessoDocumentoDTO::fromEntity)
                .collect(Collectors.toList())
        );
    }

    /** Sospendi processo */
    @PutMapping("/processi/{id}/sospendi")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<ProcessoDocumentoDTO> sospendiProcesso(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return ResponseEntity.ok(ProcessoDocumentoDTO.fromEntity(bpmService.sospendiProcesso(id, motivo)));
    }

    /** Riprendi processo sospeso */
    @PutMapping("/processi/{id}/riprendi")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<ProcessoDocumentoDTO> riprendiProcesso(@PathVariable Long id) {
        return ResponseEntity.ok(ProcessoDocumentoDTO.fromEntity(bpmService.riprendiProcesso(id)));
    }

    /** Annulla processo */
    @PutMapping("/processi/{id}/annulla")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<ProcessoDocumentoDTO> annullaProcesso(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return ResponseEntity.ok(ProcessoDocumentoDTO.fromEntity(bpmService.annullaProcesso(id, motivo)));
    }

    // ─── Task ────────────────────────────────────────────────────────────────

    /** Task attivi per l'utente corrente */
    @GetMapping("/tasks/miei")
    public ResponseEntity<List<ProcessoDocumentoDTO.TaskProcessoDTO>> getMieiTask(Authentication auth) {
        List<TaskProcesso> tasks = bpmService.getTaskAttiviPerUtente(auth.getName());
        return ResponseEntity.ok(
            tasks.stream()
                .map(ProcessoDocumentoDTO.TaskProcessoDTO::fromEntity)
                .collect(Collectors.toList())
        );
    }

    /** Completa un task di un processo */
    @PostMapping("/processi/{processoId}/tasks/{taskId}/completa")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE','OPERATORE')")
    public ResponseEntity<ProcessoDocumentoDTO> completaTask(
            @PathVariable Long processoId,
            @PathVariable Long taskId,
            @RequestBody CompletaTaskRequest req,
            Authentication auth) {
        ProcessoDocumento processo = bpmService.completaTask(processoId, taskId, req, auth.getName());
        return ResponseEntity.ok(ProcessoDocumentoDTO.fromEntity(processo));
    }

    // ─── Statistiche ─────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long attivi = bpmService.countProcessiAttivi();
        long totale = bpmService.getAllProcessi().size();
        return ResponseEntity.ok(Map.of(
            "processiAttivi", attivi,
            "processiTotali", totale
        ));
    }
}
