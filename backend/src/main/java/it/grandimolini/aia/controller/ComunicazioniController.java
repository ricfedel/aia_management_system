package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.ComunicazioneEnteDTO;
import it.grandimolini.aia.service.ComunicazioneService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comunicazioni")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComunicazioniController {

    private final ComunicazioneService comunicazioneService;

    // ── GET filtrato ───────────────────────────────────────────────────────
    @GetMapping
    public List<ComunicazioneEnteDTO> getAll(
            @RequestParam Long stabilimentoId,
            @RequestParam(required = false) String stato,
            @RequestParam(required = false) String ente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return comunicazioneService.findFiltered(stabilimentoId, stato, ente, from, to);
    }

    // ── GET singola ────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ComunicazioneEnteDTO> getById(@PathVariable Long id) {
        return comunicazioneService.findByIdAsDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET in attesa di riscontro ─────────────────────────────────────────
    @GetMapping("/in-attesa-riscontro/{stabilimentoId}")
    public List<ComunicazioneEnteDTO> getInAttesaRiscontro(
            @PathVariable Long stabilimentoId,
            @RequestParam(defaultValue = "30") int giorniSoglia) {

        return comunicazioneService.findInAttesaRiscontro(stabilimentoId, giorniSoglia);
    }

    // ── GET riepilogo per stato ────────────────────────────────────────────
    @GetMapping("/riepilogo/{stabilimentoId}")
    public Map<String, Long> getRiepilogo(@PathVariable Long stabilimentoId) {
        return comunicazioneService.getRiepilogoPerStato(stabilimentoId);
    }

    // ── POST ───────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ComunicazioneEnteDTO> create(@RequestBody ComunicazioneEnteDTO dto) {
        return ResponseEntity.ok(comunicazioneService.createFromDTO(dto));
    }

    // ── PUT ────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ComunicazioneEnteDTO> update(
            @PathVariable Long id, @RequestBody ComunicazioneEnteDTO dto) {

        return comunicazioneService.updateFromDTO(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── PATCH stato ────────────────────────────────────────────────────────
    @PatchMapping("/{id}/stato")
    public ResponseEntity<ComunicazioneEnteDTO> cambiaStato(
            @PathVariable Long id,
            @RequestParam String stato) {

        return comunicazioneService.cambiaStato(id, stato)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return comunicazioneService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}
