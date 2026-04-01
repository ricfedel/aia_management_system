package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.CodiceRifiutoDTO;
import it.grandimolini.aia.dto.MovimentoRifiutoDTO;
import it.grandimolini.aia.service.RifiutiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rifiuti")
public class RifiutiController {

    private final RifiutiService rifiutiService;

    public RifiutiController(RifiutiService rifiutiService) {
        this.rifiutiService = rifiutiService;
    }

    // ─── Codici CER (anagrafica) ──────────────────────────────────────
    @GetMapping("/codici")
    public List<CodiceRifiutoDTO> getCodici(
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) Boolean soloAttivi,
            @RequestParam(required = false) Boolean pericoloso) {
        return rifiutiService.getCodici(stabilimentoId, soloAttivi, pericoloso);
    }

    @GetMapping("/codici/{id}")
    public ResponseEntity<CodiceRifiutoDTO> getCodice(@PathVariable Long id) {
        return rifiutiService.findCodiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/codici")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<CodiceRifiutoDTO> createCodice(@RequestBody CodiceRifiutoDTO dto) {
        CodiceRifiutoDTO created = rifiutiService.createCodice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/codici/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<CodiceRifiutoDTO> updateCodice(@PathVariable Long id, @RequestBody CodiceRifiutoDTO dto) {
        return rifiutiService.updateCodice(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/codici/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCodice(@PathVariable Long id) {
        boolean deleted = rifiutiService.deleteCodice(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ─── Movimenti ────────────────────────────────────────────────────
    @GetMapping("/movimenti")
    public List<MovimentoRifiutoDTO> getMovimenti(
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) Long codiceRifiutoId,
            @RequestParam(required = false) Integer anno,
            @RequestParam(required = false) Integer mese) {
        return rifiutiService.getMovimenti(stabilimentoId, codiceRifiutoId, anno, mese);
    }

    @GetMapping("/movimenti/{id}")
    public ResponseEntity<MovimentoRifiutoDTO> getMovimento(@PathVariable Long id) {
        return rifiutiService.findMovimentoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/movimenti")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<MovimentoRifiutoDTO> createMovimento(@RequestBody MovimentoRifiutoDTO dto) {
        MovimentoRifiutoDTO created = rifiutiService.createMovimento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/movimenti/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<MovimentoRifiutoDTO> updateMovimento(@PathVariable Long id,
                                                               @RequestBody MovimentoRifiutoDTO dto) {
        return rifiutiService.updateMovimento(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/movimenti/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<Void> deleteMovimento(@PathVariable Long id) {
        boolean deleted = rifiutiService.deleteMovimento(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/anni")
    public List<Integer> getAnni(@RequestParam Long stabilimentoId) {
        return rifiutiService.getAnniByStabilimento(stabilimentoId);
    }
}
