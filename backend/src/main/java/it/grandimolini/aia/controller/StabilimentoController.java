package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.CreateStabilimentoRequest;
import it.grandimolini.aia.dto.StabilimentoDTO;
import it.grandimolini.aia.dto.UpdateStabilimentoRequest;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.StabilimentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stabilimenti")
public class StabilimentoController {

    @Autowired
    private StabilimentoService stabilimentoService;

    @Autowired
    private StabilimentoAccessChecker stabilimentoAccessChecker;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StabilimentoDTO>> getAllStabilimenti() {
        List<StabilimentoDTO> dtos;

        if (stabilimentoAccessChecker.isAdmin()) {
            dtos = stabilimentoService.findAllAsDTOs();
        } else {
            // Gli utenti non admin vedono solo i propri stabilimenti (JOIN FETCH → no LazyInit)
            List<Long> ids = stabilimentoAccessChecker.getCurrentUserStabilimentoIds();
            dtos = stabilimentoService.findByIdsAsDTOs(ids);
        }

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/attivi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StabilimentoDTO>> getStabilimentiAttivi() {
        List<StabilimentoDTO> dtos;

        if (stabilimentoAccessChecker.isAdmin()) {
            dtos = stabilimentoService.findAllAttiviAsDTOs();
        } else {
            List<Long> ids = stabilimentoAccessChecker.getCurrentUserStabilimentoIds();
            dtos = stabilimentoService.findByIdsAttiviAsDTOs(ids);
        }

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#id)")
    public ResponseEntity<StabilimentoDTO> getStabilimentoById(@PathVariable Long id) {
        StabilimentoDTO dto = stabilimentoService.findByIdAsDTO(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", id));
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<StabilimentoDTO> createStabilimento(
            @Valid @RequestBody CreateStabilimentoRequest request) {
        StabilimentoDTO dto = stabilimentoService.createFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<StabilimentoDTO> updateStabilimento(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStabilimentoRequest request) {
        StabilimentoDTO dto = stabilimentoService.updateFromRequest(id, request);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<Void> deleteStabilimento(@PathVariable Long id) {
        if (!stabilimentoService.existsById(id)) {
            throw new ResourceNotFoundException("Stabilimento", "id", id);
        }
        stabilimentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
