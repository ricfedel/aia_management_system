package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.MonitoraggioDTO;
import it.grandimolini.aia.dto.ParametroMonitoraggioDTO;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.MonitoraggioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Punti di Monitoraggio and their Parametri.
 *
 * This controller is a thin HTTP dispatcher:
 * - No entity imports
 * - No repository imports
 * - No @Transactional
 * - No mapping logic
 * All business logic and mapping is delegated to MonitoraggioService.
 */
@RestController
@RequestMapping("/api/punti-monitoraggio")
public class PuntiMonitoraggioController {

    private final MonitoraggioService monitoraggioService;
    private final StabilimentoAccessChecker stabilimentoAccessChecker;

    public PuntiMonitoraggioController(MonitoraggioService monitoraggioService,
                                       StabilimentoAccessChecker stabilimentoAccessChecker) {
        this.monitoraggioService = monitoraggioService;
        this.stabilimentoAccessChecker = stabilimentoAccessChecker;
    }

    // ─── PUNTI DI MONITORAGGIO ────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MonitoraggioDTO>> getAll() {
        List<MonitoraggioDTO> list = stabilimentoAccessChecker.isAdmin()
                ? monitoraggioService.findAllAsDTOs()
                : monitoraggioService.findAllAsDTOs().stream()
                    .filter(m -> stabilimentoAccessChecker.hasAccessToStabilimento(m.getStabilimentoId()))
                    .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stabilimento/{stabilimentoId}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<List<MonitoraggioDTO>> getByStabilimento(@PathVariable Long stabilimentoId) {
        return ResponseEntity.ok(monitoraggioService.findByStabilimentoIdAsDTOs(stabilimentoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonitoraggioDTO> getById(@PathVariable Long id) {
        MonitoraggioDTO dto = monitoraggioService.findByIdAsDTO(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", id));
        checkAccess(dto);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<MonitoraggioDTO> create(@RequestBody MonitoraggioDTO dto) {
        MonitoraggioDTO created = monitoraggioService.createFromDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<MonitoraggioDTO> update(@PathVariable Long id,
                                                   @RequestBody MonitoraggioDTO dto) {
        MonitoraggioDTO existing = monitoraggioService.findByIdAsDTO(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", id));
        checkAccess(existing);
        return ResponseEntity.ok(monitoraggioService.updateFromDTO(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        MonitoraggioDTO existing = monitoraggioService.findByIdAsDTO(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", id));
        checkAccess(existing);
        monitoraggioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── PARAMETRI ────────────────────────────────────────────────────────

    @GetMapping("/{monitoraggioId}/parametri")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParametroMonitoraggioDTO>> getParametri(
            @PathVariable Long monitoraggioId) {
        MonitoraggioDTO m = monitoraggioService.findByIdAsDTO(monitoraggioId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", monitoraggioId));
        checkAccess(m);
        return ResponseEntity.ok(monitoraggioService.findParametriByMonitoraggioId(monitoraggioId));
    }

    @PostMapping("/{monitoraggioId}/parametri")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<ParametroMonitoraggioDTO> addParametro(
            @PathVariable Long monitoraggioId,
            @RequestBody ParametroMonitoraggioDTO dto) {
        MonitoraggioDTO m = monitoraggioService.findByIdAsDTO(monitoraggioId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", monitoraggioId));
        checkAccess(m);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(monitoraggioService.addParametro(monitoraggioId, dto));
    }

    @PutMapping("/{monitoraggioId}/parametri/{parametroId}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<ParametroMonitoraggioDTO> updateParametro(
            @PathVariable Long monitoraggioId,
            @PathVariable Long parametroId,
            @RequestBody ParametroMonitoraggioDTO dto) {
        return ResponseEntity.ok(
                monitoraggioService.updateParametro(monitoraggioId, parametroId, dto));
    }

    @DeleteMapping("/{monitoraggioId}/parametri/{parametroId}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> deleteParametro(
            @PathVariable Long monitoraggioId,
            @PathVariable Long parametroId) {
        monitoraggioService.deleteParametro(parametroId);
        return ResponseEntity.noContent().build();
    }

    // ─── access control helper ────────────────────────────────────────────

    private void checkAccess(MonitoraggioDTO dto) {
        if (!stabilimentoAccessChecker.isAdmin() &&
            !stabilimentoAccessChecker.hasAccessToStabilimento(dto.getStabilimentoId())) {
            throw new ResourceNotFoundException("Monitoraggio", "id", dto.getId());
        }
    }
}
