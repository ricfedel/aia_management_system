package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.CreateDatiAmbientaliRequest;
import it.grandimolini.aia.dto.DatiAmbientaliDTO;
import it.grandimolini.aia.dto.UpdateDatiAmbientaliRequest;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.DatiAmbientali;
import it.grandimolini.aia.model.Monitoraggio;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.DatiAmbientaliService;
import it.grandimolini.aia.service.MonitoraggioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dati-ambientali")
public class DatiAmbientaliController {

    @Autowired
    private DatiAmbientaliService datiAmbientaliService;

    @Autowired
    private MonitoraggioService monitoraggioService;

    @Autowired
    private StabilimentoAccessChecker stabilimentoAccessChecker;

    @GetMapping
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<List<DatiAmbientaliDTO>> getAllDatiAmbientali() {
        List<DatiAmbientali> datiList = datiAmbientaliService.findAll();
        List<DatiAmbientaliDTO> dtos = datiList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/monitoraggio/{monitoraggioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DatiAmbientaliDTO>> getDatiByMonitoraggio(@PathVariable Long monitoraggioId) {
        // Verifica che il monitoraggio esista e l'utente abbia accesso allo stabilimento
        Monitoraggio monitoraggio = monitoraggioService.findById(monitoraggioId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", monitoraggioId));

        if (!stabilimentoAccessChecker.hasAccessToStabilimento(monitoraggio.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Monitoraggio", "id", monitoraggioId);
        }

        List<DatiAmbientali> datiList = datiAmbientaliService.findByMonitoraggio(monitoraggioId);
        List<DatiAmbientaliDTO> dtos = datiList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stabilimento/{stabilimentoId}/anno/{anno}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<List<DatiAmbientaliDTO>> getDatiByStabilimentoAndAnno(
            @PathVariable Long stabilimentoId,
            @PathVariable int anno) {

        List<DatiAmbientali> datiList = datiAmbientaliService.findByStabilimentoAndAnno(stabilimentoId, anno);
        List<DatiAmbientaliDTO> dtos = datiList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/non-conformi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DatiAmbientaliDTO>> getDatiNonConformi() {
        List<DatiAmbientali> datiList = datiAmbientaliService.findNonConformi();

        // Filtra per stabilimenti accessibili all'utente
        if (!stabilimentoAccessChecker.isAdmin()) {
            datiList = datiList.stream()
                    .filter(dato -> stabilimentoAccessChecker.hasAccessToStabilimento(
                            dato.getMonitoraggio().getStabilimento().getId()))
                    .collect(Collectors.toList());
        }

        List<DatiAmbientaliDTO> dtos = datiList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DatiAmbientaliDTO> getDatiById(@PathVariable Long id) {
        DatiAmbientali dati = datiAmbientaliService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatiAmbientali", "id", id));

        // Verifica accesso allo stabilimento del monitoraggio
        if (!stabilimentoAccessChecker.hasAccessToStabilimento(
                dati.getMonitoraggio().getStabilimento().getId())) {
            throw new ResourceNotFoundException("DatiAmbientali", "id", id);
        }

        return ResponseEntity.ok(convertToDTO(dati));
    }

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.canEditDatiAmbientali()")
    public ResponseEntity<DatiAmbientaliDTO> createDati(@Valid @RequestBody CreateDatiAmbientaliRequest request) {
        // Verifica che il monitoraggio esista e l'utente abbia accesso
        Monitoraggio monitoraggio = monitoraggioService.findById(request.getMonitoraggioId())
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", request.getMonitoraggioId()));

        if (!stabilimentoAccessChecker.hasAccessToStabilimento(monitoraggio.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Monitoraggio", "id", request.getMonitoraggioId());
        }

        DatiAmbientali dati = convertToEntity(request);
        DatiAmbientali saved = datiAmbientaliService.save(dati);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.canEditDatiAmbientali()")
    public ResponseEntity<DatiAmbientaliDTO> updateDati(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDatiAmbientaliRequest request) {

        DatiAmbientali existing = datiAmbientaliService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatiAmbientali", "id", id));

        // Verifica accesso allo stabilimento del monitoraggio
        if (!stabilimentoAccessChecker.hasAccessToStabilimento(
                existing.getMonitoraggio().getStabilimento().getId())) {
            throw new ResourceNotFoundException("DatiAmbientali", "id", id);
        }

        updateEntityFromRequest(existing, request);
        DatiAmbientali updated = datiAmbientaliService.save(existing);

        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> deleteDati(@PathVariable Long id) {
        DatiAmbientali dati = datiAmbientaliService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DatiAmbientali", "id", id));

        // Verifica accesso allo stabilimento del monitoraggio
        if (!stabilimentoAccessChecker.hasAccessToStabilimento(
                dati.getMonitoraggio().getStabilimento().getId())) {
            throw new ResourceNotFoundException("DatiAmbientali", "id", id);
        }

        datiAmbientaliService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Metodi di conversione DTO <-> Entity

    private DatiAmbientaliDTO convertToDTO(DatiAmbientali dati) {
        return DatiAmbientaliDTO.builder()
                .id(dati.getId())
                .monitoraggioId(dati.getMonitoraggio().getId())
                .dataCampionamento(dati.getDataCampionamento())
                .parametro(dati.getParametro())
                .valoreMisurato(dati.getValoreMisurato())
                .unitaMisura(dati.getUnitaMisura())
                .limiteAutorizzato(dati.getLimiteAutorizzato())
                .statoConformita(dati.getStatoConformita())
                .rapportoProva(dati.getRapportoProva())
                .laboratorio(dati.getLaboratorio())
                .note(dati.getNote())
                .createdAt(dati.getCreatedAt())
                .build();
    }

    private DatiAmbientali convertToEntity(CreateDatiAmbientaliRequest request) {
        Monitoraggio monitoraggio = monitoraggioService.findById(request.getMonitoraggioId())
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", request.getMonitoraggioId()));

        DatiAmbientali dati = new DatiAmbientali();
        dati.setMonitoraggio(monitoraggio);
        dati.setDataCampionamento(request.getDataCampionamento());
        dati.setParametro(request.getParametro());
        dati.setValoreMisurato(request.getValoreMisurato());
        dati.setUnitaMisura(request.getUnitaMisura());
        dati.setLimiteAutorizzato(request.getLimiteAutorizzato());
        dati.setRapportoProva(request.getRapportoProva());
        dati.setLaboratorio(request.getLaboratorio());
        dati.setNote(request.getNote());
        return dati;
    }

    private void updateEntityFromRequest(DatiAmbientali dati, UpdateDatiAmbientaliRequest request) {
        if (request.getDataCampionamento() != null) {
            dati.setDataCampionamento(request.getDataCampionamento());
        }
        if (request.getParametro() != null) {
            dati.setParametro(request.getParametro());
        }
        if (request.getValoreMisurato() != null) {
            dati.setValoreMisurato(request.getValoreMisurato());
        }
        if (request.getUnitaMisura() != null) {
            dati.setUnitaMisura(request.getUnitaMisura());
        }
        if (request.getLimiteAutorizzato() != null) {
            dati.setLimiteAutorizzato(request.getLimiteAutorizzato());
        }
        if (request.getRapportoProva() != null) {
            dati.setRapportoProva(request.getRapportoProva());
        }
        if (request.getLaboratorio() != null) {
            dati.setLaboratorio(request.getLaboratorio());
        }
        if (request.getNote() != null) {
            dati.setNote(request.getNote());
        }
    }
}
