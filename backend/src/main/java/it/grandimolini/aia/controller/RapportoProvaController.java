package it.grandimolini.aia.controller;

import it.grandimolini.aia.model.RapportoProva;
import it.grandimolini.aia.model.RigaRapportoProva;
import it.grandimolini.aia.service.RapportoProvaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller per la gestione dei Rapporti di Prova (RdP).
 *
 * Endpoints:
 * - GET  /api/rapporti-prova?stabilimentoId=&dal=&al=  → lista per stabilimento e periodo
 * - GET  /api/rapporti-prova/{id}                      → dettaglio singolo rapporto con righe
 * - POST /api/rapporti-prova                            → crea nuovo rapporto
 * - PUT  /api/rapporti-prova/{id}                      → aggiorna rapporto
 * - PUT  /api/rapporti-prova/{id}/righe                → sostituisce tutte le righe analitiche
 * - DELETE /api/rapporti-prova/{id}                    → elimina rapporto
 * - GET  /api/rapporti-prova/non-conformi?stabilimentoId=  → soli non conformi
 * - GET  /api/rapporti-prova/monitoraggio/{monitoraggioId} → per punto di monitoraggio
 */
@RestController
@RequestMapping("/api/rapporti-prova")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RapportoProvaController {

    private final RapportoProvaService rapportoProvaService;

    // ── Lista ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<RapportoProva>> findAll(
            @RequestParam Long stabilimentoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al) {

        List<RapportoProva> result;
        if (dal != null && al != null) {
            result = rapportoProvaService.findByStabilimentoAndPeriodo(stabilimentoId, dal, al);
        } else {
            result = rapportoProvaService.findByStabilimento(stabilimentoId);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monitoraggio/{monitoraggioId}")
    public ResponseEntity<List<RapportoProva>> findByMonitoraggio(@PathVariable Long monitoraggioId) {
        return ResponseEntity.ok(rapportoProvaService.findByMonitoraggio(monitoraggioId));
    }

    @GetMapping("/non-conformi")
    public ResponseEntity<List<RapportoProva>> findNonConformi(@RequestParam Long stabilimentoId) {
        return ResponseEntity.ok(rapportoProvaService.findNonConformi(stabilimentoId));
    }

    // ── Dettaglio ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<RapportoProva> findById(@PathVariable Long id) {
        return rapportoProvaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Creazione ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<RapportoProva> create(@RequestBody RapportoProva rapporto) {
        try {
            RapportoProva saved = rapportoProvaService.save(rapporto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── Aggiornamento ──────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<RapportoProva> update(
            @PathVariable Long id,
            @RequestBody RapportoProva rapporto) {
        return rapportoProvaService.findById(id)
                .map(existing -> {
                    rapporto.setId(id);
                    return ResponseEntity.ok(rapportoProvaService.save(rapporto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Sostituisce l'intero set di righe analitiche di un rapporto.
     * Utilizzato per importare i risultati dal laboratorio.
     */
    @PutMapping("/{id}/righe")
    public ResponseEntity<RapportoProva> aggiornaRighe(
            @PathVariable Long id,
            @RequestBody List<RigaRapportoProva> righe) {
        try {
            return ResponseEntity.ok(rapportoProvaService.salvaRighe(id, righe));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Eliminazione ───────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            rapportoProvaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Statistiche ────────────────────────────────────────────────────────────

    @GetMapping("/count")
    public ResponseEntity<Long> count(
            @RequestParam Long stabilimentoId,
            @RequestParam int anno) {
        return ResponseEntity.ok(rapportoProvaService.countByStabilimentoAndAnno(stabilimentoId, anno));
    }
}
