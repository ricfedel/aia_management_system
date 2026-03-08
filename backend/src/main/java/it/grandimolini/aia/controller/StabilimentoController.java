package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.CreateStabilimentoRequest;
import it.grandimolini.aia.dto.StabilimentoDTO;
import it.grandimolini.aia.dto.UpdateStabilimentoRequest;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.StabilimentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        List<Stabilimento> stabilimenti;

        if (stabilimentoAccessChecker.isAdmin()) {
            stabilimenti = stabilimentoService.findAll();
        } else {
            // Gli utenti non admin vedono solo i loro stabilimenti
            stabilimenti = stabilimentoAccessChecker.getCurrentUser().getStabilimenti()
                    .stream().toList();
        }

        List<StabilimentoDTO> dtos = stabilimenti.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/attivi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StabilimentoDTO>> getStabilimentiAttivi() {
        List<Stabilimento> stabilimenti;

        if (stabilimentoAccessChecker.isAdmin()) {
            stabilimenti = stabilimentoService.findAllAttivi();
        } else {
            stabilimenti = stabilimentoAccessChecker.getCurrentUser().getStabilimenti()
                    .stream()
                    .filter(Stabilimento::getAttivo)
                    .toList();
        }

        List<StabilimentoDTO> dtos = stabilimenti.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#id)")
    public ResponseEntity<StabilimentoDTO> getStabilimentoById(@PathVariable Long id) {
        Stabilimento stabilimento = stabilimentoService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", id));

        return ResponseEntity.ok(convertToDTO(stabilimento));
    }

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<StabilimentoDTO> createStabilimento(@Valid @RequestBody CreateStabilimentoRequest request) {
        Stabilimento stabilimento = convertToEntity(request);
        Stabilimento saved = stabilimentoService.save(stabilimento);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<StabilimentoDTO> updateStabilimento(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStabilimentoRequest request) {

        Stabilimento existing = stabilimentoService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", id));

        updateEntityFromRequest(existing, request);
        Stabilimento updated = stabilimentoService.save(existing);

        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<Void> deleteStabilimento(@PathVariable Long id) {
        if (!stabilimentoService.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Stabilimento", "id", id);
        }

        stabilimentoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Metodi di conversione DTO <-> Entity

    private StabilimentoDTO convertToDTO(Stabilimento stabilimento) {
        return StabilimentoDTO.builder()
                .id(stabilimento.getId())
                .nome(stabilimento.getNome())
                .citta(stabilimento.getCitta())
                .indirizzo(stabilimento.getIndirizzo())
                .numeroAIA(stabilimento.getNumeroAIA())
                .dataRilascioAIA(stabilimento.getDataRilascioAIA())
                .dataScadenzaAIA(stabilimento.getDataScadenzaAIA())
                .enteCompetente(stabilimento.getEnteCompetente())
                .responsabileAmbientale(stabilimento.getResponsabileAmbientale())
                .email(stabilimento.getEmail())
                .telefono(stabilimento.getTelefono())
                .attivo(stabilimento.getAttivo())
                .build();
    }

    private Stabilimento convertToEntity(CreateStabilimentoRequest request) {
        Stabilimento stabilimento = new Stabilimento();
        stabilimento.setNome(request.getNome());
        stabilimento.setCitta(request.getCitta());
        stabilimento.setIndirizzo(request.getIndirizzo());
        stabilimento.setNumeroAIA(request.getNumeroAIA());
        stabilimento.setDataRilascioAIA(request.getDataRilascioAIA());
        stabilimento.setDataScadenzaAIA(request.getDataScadenzaAIA());
        stabilimento.setEnteCompetente(request.getEnteCompetente());
        stabilimento.setResponsabileAmbientale(request.getResponsabileAmbientale());
        stabilimento.setEmail(request.getEmail());
        stabilimento.setTelefono(request.getTelefono());
        stabilimento.setAttivo(request.getAttivo() != null ? request.getAttivo() : true);
        return stabilimento;
    }

    private void updateEntityFromRequest(Stabilimento stabilimento, UpdateStabilimentoRequest request) {
        if (request.getNome() != null) {
            stabilimento.setNome(request.getNome());
        }
        if (request.getCitta() != null) {
            stabilimento.setCitta(request.getCitta());
        }
        if (request.getIndirizzo() != null) {
            stabilimento.setIndirizzo(request.getIndirizzo());
        }
        if (request.getNumeroAIA() != null) {
            stabilimento.setNumeroAIA(request.getNumeroAIA());
        }
        if (request.getDataRilascioAIA() != null) {
            stabilimento.setDataRilascioAIA(request.getDataRilascioAIA());
        }
        if (request.getDataScadenzaAIA() != null) {
            stabilimento.setDataScadenzaAIA(request.getDataScadenzaAIA());
        }
        if (request.getEnteCompetente() != null) {
            stabilimento.setEnteCompetente(request.getEnteCompetente());
        }
        if (request.getResponsabileAmbientale() != null) {
            stabilimento.setResponsabileAmbientale(request.getResponsabileAmbientale());
        }
        if (request.getEmail() != null) {
            stabilimento.setEmail(request.getEmail());
        }
        if (request.getTelefono() != null) {
            stabilimento.setTelefono(request.getTelefono());
        }
        if (request.getAttivo() != null) {
            stabilimento.setAttivo(request.getAttivo());
        }
    }
}
