package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.CreatePrescrizioneRequest;
import it.grandimolini.aia.dto.PrescrizioneDTO;
import it.grandimolini.aia.dto.UpdatePrescrizioneRequest;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.PrescrizioneService;
import it.grandimolini.aia.service.StabilimentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescrizioni")
public class PrescrizioneController {

    @Autowired
    private PrescrizioneService prescrizioneService;

    @Autowired
    private StabilimentoService stabilimentoService;

    @Autowired
    private StabilimentoAccessChecker stabilimentoAccessChecker;

    @GetMapping
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<List<PrescrizioneDTO>> getAllPrescrizioni() {
        List<PrescrizioneDTO> dtos = prescrizioneService.findAllAsDTOs();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stabilimento/{stabilimentoId}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<List<PrescrizioneDTO>> getPrescrizioniByStabilimento(@PathVariable Long stabilimentoId) {
        List<PrescrizioneDTO> dtos = prescrizioneService.findByStabilimentoAsDTOs(stabilimentoId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrescrizioneDTO> getPrescrizioneById(@PathVariable Long id) {
        Prescrizione prescrizione = prescrizioneService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescrizione", "id", id));

        // Verifica accesso allo stabilimento della prescrizione
        if (!stabilimentoAccessChecker.hasAccessToStabilimento(prescrizione.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Prescrizione", "id", id);
        }

        return ResponseEntity.ok(prescrizioneService.findByIdAsDTO(id).orElseThrow());
    }

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<PrescrizioneDTO> createPrescrizione(@Valid @RequestBody CreatePrescrizioneRequest request) {
        // Verifica accesso allo stabilimento
        if (!stabilimentoAccessChecker.hasAccessToStabilimento(request.getStabilimentoId())) {
            throw new ResourceNotFoundException("Stabilimento", "id", request.getStabilimentoId());
        }

        Prescrizione prescrizione = convertToEntity(request);
        PrescrizioneDTO dto = prescrizioneService.saveAsDTO(prescrizione);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<PrescrizioneDTO> updatePrescrizione(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrescrizioneRequest request) {

        Prescrizione existing = prescrizioneService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescrizione", "id", id));

        // Verifica accesso allo stabilimento della prescrizione
        if (!stabilimentoAccessChecker.hasAccessToStabilimento(existing.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Prescrizione", "id", id);
        }

        updateEntityFromRequest(existing, request);
        PrescrizioneDTO dto = prescrizioneService.saveAsDTO(existing);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> deletePrescrizione(@PathVariable Long id) {
        Prescrizione prescrizione = prescrizioneService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescrizione", "id", id));

        // Verifica accesso allo stabilimento della prescrizione
        if (!stabilimentoAccessChecker.hasAccessToStabilimento(prescrizione.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Prescrizione", "id", id);
        }

        prescrizioneService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Metodi di conversione Entity ← Request (controller responsibility)

    private Prescrizione convertToEntity(CreatePrescrizioneRequest request) {
        Stabilimento stabilimento = stabilimentoService.findById(request.getStabilimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", request.getStabilimentoId()));

        Prescrizione prescrizione = new Prescrizione();
        prescrizione.setStabilimento(stabilimento);
        prescrizione.setCodice(request.getCodice());
        prescrizione.setDescrizione(request.getDescrizione());
        prescrizione.setMatriceAmbientale(request.getMatriceAmbientale());
        prescrizione.setStato(request.getStato() != null ? request.getStato() : Prescrizione.StatoPrescrizione.APERTA);
        prescrizione.setDataEmissione(request.getDataEmissione());
        prescrizione.setDataScadenza(request.getDataScadenza());
        prescrizione.setEnteEmittente(request.getEnteEmittente());
        prescrizione.setRiferimentoNormativo(request.getRiferimentoNormativo());
        prescrizione.setPriorita(request.getPriorita());
        prescrizione.setNote(request.getNote());
        return prescrizione;
    }

    private void updateEntityFromRequest(Prescrizione prescrizione, UpdatePrescrizioneRequest request) {
        if (request.getCodice() != null) {
            prescrizione.setCodice(request.getCodice());
        }
        if (request.getDescrizione() != null) {
            prescrizione.setDescrizione(request.getDescrizione());
        }
        if (request.getMatriceAmbientale() != null) {
            prescrizione.setMatriceAmbientale(request.getMatriceAmbientale());
        }
        if (request.getStato() != null) {
            prescrizione.setStato(request.getStato());
        }
        if (request.getDataEmissione() != null) {
            prescrizione.setDataEmissione(request.getDataEmissione());
        }
        if (request.getDataScadenza() != null) {
            prescrizione.setDataScadenza(request.getDataScadenza());
        }
        if (request.getEnteEmittente() != null) {
            prescrizione.setEnteEmittente(request.getEnteEmittente());
        }
        if (request.getRiferimentoNormativo() != null) {
            prescrizione.setRiferimentoNormativo(request.getRiferimentoNormativo());
        }
        if (request.getPriorita() != null) {
            prescrizione.setPriorita(request.getPriorita());
        }
        if (request.getNote() != null) {
            prescrizione.setNote(request.getNote());
        }
        if (request.getDataChiusura() != null) {
            prescrizione.setDataChiusura(request.getDataChiusura());
        }
    }
}
