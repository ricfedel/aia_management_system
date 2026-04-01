package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.AltraAutorizzazioneDTO;
import it.grandimolini.aia.service.AltraAutorizzazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/altre-autorizzazioni")
public class AltraAutorizzazioneController {

    @Autowired
    private AltraAutorizzazioneService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AltraAutorizzazioneDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/stabilimento/{stabilimentoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AltraAutorizzazioneDTO>> getByStabilimento(@PathVariable Long stabilimentoId) {
        return ResponseEntity.ok(service.findByStabilimento(stabilimentoId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABILE')")
    public ResponseEntity<AltraAutorizzazioneDTO> create(@RequestBody AltraAutorizzazioneDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABILE')")
    public ResponseEntity<AltraAutorizzazioneDTO> update(@PathVariable Long id, @RequestBody AltraAutorizzazioneDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
