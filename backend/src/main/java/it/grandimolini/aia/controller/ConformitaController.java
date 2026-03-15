package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.RilevazioneMisuraDTO;
import it.grandimolini.aia.service.ConformitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conformita")
public class ConformitaController {

    private final ConformitaService conformitaService;

    public ConformitaController(ConformitaService conformitaService) {
        this.conformitaService = conformitaService;
    }

    // ─── Rilevazioni ──────────────────────────────────────────────────
    @GetMapping("/rilevazioni")
    public List<RilevazioneMisuraDTO> getRilevazioni(
            @RequestParam(required = false) Long parametroId,
            @RequestParam(required = false) Long monitoraggioId,
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return conformitaService.getRilevazioni(parametroId, monitoraggioId, stabilimentoId, from, to);
    }

    @GetMapping("/rilevazioni/{id}")
    public ResponseEntity<RilevazioneMisuraDTO> getById(@PathVariable Long id) {
        return conformitaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rilevazioni")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE','OPERATORE')")
    public ResponseEntity<RilevazioneMisuraDTO> create(@RequestBody RilevazioneMisuraDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conformitaService.create(dto));
    }

    @PutMapping("/rilevazioni/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<RilevazioneMisuraDTO> update(@PathVariable Long id,
                                                       @RequestBody RilevazioneMisuraDTO dto) {
        return conformitaService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/rilevazioni/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!conformitaService.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // ─── Dashboard: ultima conformità per ogni parametro ──────────────
    @GetMapping("/dashboard/{stabilimentoId}")
    public List<RilevazioneMisuraDTO> getDashboard(@PathVariable Long stabilimentoId) {
        return conformitaService.getDashboard(stabilimentoId);
    }

    @GetMapping("/dashboard/monitoraggio/{monitoraggioId}")
    public List<RilevazioneMisuraDTO> getDashboardMonitoraggio(@PathVariable Long monitoraggioId) {
        return conformitaService.getDashboardMonitoraggio(monitoraggioId);
    }

    /** Rilevazioni non conformi per lo stabilimento */
    @GetMapping("/non-conformi/{stabilimentoId}")
    public List<RilevazioneMisuraDTO> getNonConformi(@PathVariable Long stabilimentoId) {
        return conformitaService.getNonConformi(stabilimentoId);
    }

    /** Riepilogo conteggi CONFORME/ATTENZIONE/NON_CONFORME per stabilimento */
    @GetMapping("/riepilogo/{stabilimentoId}")
    public Map<String, Long> getRiepilogo(@PathVariable Long stabilimentoId) {
        return conformitaService.getRiepilogo(stabilimentoId);
    }
}
