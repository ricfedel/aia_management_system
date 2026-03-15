package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.AnagraficaCaminoDTO;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.AnagraficaCamino.FaseProcesso;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.AnagraficaCaminoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller per l'anagrafica tecnica dei camini.
 * Espone i dati della "Tabella A – Quadro riassuntivo delle emissioni in atmosfera"
 * del provvedimento AIA di ciascun stabilimento.
 */
@RestController
@RequestMapping("/api/anagrafica-camini")
public class AnagraficaCaminoController {

    private final AnagraficaCaminoService anagraficaCaminoService;
    private final StabilimentoAccessChecker accessChecker;

    public AnagraficaCaminoController(AnagraficaCaminoService anagraficaCaminoService,
                                      StabilimentoAccessChecker accessChecker) {
        this.anagraficaCaminoService = anagraficaCaminoService;
        this.accessChecker = accessChecker;
    }

    // ─── GET tutti (admin vede tutto, altri filtrano per stabilimento) ────

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AnagraficaCaminoDTO>> getAll(
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) FaseProcesso faseProcesso,
            @RequestParam(required = false) Boolean attivo) {

        List<AnagraficaCaminoDTO> list;

        if (stabilimentoId != null) {
            if (faseProcesso != null) {
                list = anagraficaCaminoService.findByStabilimentoIdAndFaseProcessoAsDTOs(stabilimentoId, faseProcesso);
            } else if (attivo != null) {
                list = anagraficaCaminoService.findByStabilimentoIdAndAttivoAsDTOs(stabilimentoId, attivo);
            } else {
                list = anagraficaCaminoService.findByStabilimentoIdAsDTOs(stabilimentoId);
            }
        } else {
            list = anagraficaCaminoService.findAllAsDTOs();
        }

        // filtra per accesso stabilimento
        if (!accessChecker.isAdmin()) {
            list = list.stream()
                    .filter(dto -> accessChecker.hasAccessToStabilimento(dto.getStabilimentoId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(list);
    }

    // ─── GET singolo ──────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnagraficaCaminoDTO> getById(@PathVariable Long id) {
        AnagraficaCaminoDTO dto = findAndCheckAccess(id);
        return ResponseEntity.ok(dto);
    }

    // ─── GET per sigla dentro uno stabilimento ────────────────────────────

    @GetMapping("/stabilimento/{stabilimentoId}/sigla/{sigla}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<AnagraficaCaminoDTO> getBySigla(
            @PathVariable Long stabilimentoId, @PathVariable String sigla) {
        AnagraficaCaminoDTO dto = anagraficaCaminoService.findByStabilimentoIdAndSiglaAsDTO(stabilimentoId, sigla)
                .orElseThrow(() -> new ResourceNotFoundException("AnagraficaCamino", "sigla", sigla));
        return ResponseEntity.ok(dto);
    }

    // ─── CREATE ───────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<AnagraficaCaminoDTO> create(@RequestBody AnagraficaCaminoDTO dto) {
        AnagraficaCaminoDTO created = anagraficaCaminoService.createFromDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<AnagraficaCaminoDTO> update(
            @PathVariable Long id, @RequestBody AnagraficaCaminoDTO dto) {
        findAndCheckAccess(id);  // access check
        AnagraficaCaminoDTO updated = anagraficaCaminoService.updateFromDTO(id, dto);
        return ResponseEntity.ok(updated);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        findAndCheckAccess(id);  // access check
        anagraficaCaminoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── helpers ──────────────────────────────────────────────────────────

    private AnagraficaCaminoDTO findAndCheckAccess(Long id) {
        AnagraficaCaminoDTO dto = anagraficaCaminoService.findByIdAsDTO(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnagraficaCamino", "id", id));
        if (!accessChecker.isAdmin() && !accessChecker.hasAccessToStabilimento(dto.getStabilimentoId())) {
            throw new ResourceNotFoundException("AnagraficaCamino", "id", id);
        }
        return dto;
    }
}
