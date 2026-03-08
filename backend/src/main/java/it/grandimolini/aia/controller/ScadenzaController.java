package it.grandimolini.aia.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.grandimolini.aia.dto.CreateScadenzaRequest;
import it.grandimolini.aia.dto.ImportScadenzeResult;
import it.grandimolini.aia.dto.ScadenzaDTO;
import it.grandimolini.aia.dto.UpdateScadenzaRequest;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.Monitoraggio;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.ImportScadenzeService;
import it.grandimolini.aia.service.MonitoraggioService;
import it.grandimolini.aia.service.PrescrizioneService;
import it.grandimolini.aia.service.ScadenzaService;
import it.grandimolini.aia.service.StabilimentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scadenze")
public class ScadenzaController {

    @Autowired
    private ScadenzaService scadenzaService;

    @Autowired
    private StabilimentoService stabilimentoService;

    @Autowired
    private ImportScadenzeService importScadenzeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrescrizioneService prescrizioneService;

    @Autowired
    private MonitoraggioService monitoraggioService;

    @Autowired
    private StabilimentoAccessChecker stabilimentoAccessChecker;

    @GetMapping
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<List<ScadenzaDTO>> getAllScadenze() {
        List<Scadenza> scadenze = scadenzaService.findAll();
        List<ScadenzaDTO> dtos = scadenze.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stabilimento/{stabilimentoId}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<List<ScadenzaDTO>> getScadenzeByStabilimento(@PathVariable Long stabilimentoId) {
        List<Scadenza> scadenze = scadenzaService.findByStabilimento(stabilimentoId);
        List<ScadenzaDTO> dtos = scadenze.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/prossimi-30-giorni")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScadenzaDTO>> getScadenzeProssimi30Giorni() {
        List<Scadenza> scadenze = scadenzaService.findScadenzeProssimi30Giorni();

        // Filtra per stabilimenti accessibili all'utente
        if (!stabilimentoAccessChecker.isAdmin()) {
            scadenze = scadenze.stream()
                    .filter(scadenza -> scadenza.getStabilimento() != null &&
                            stabilimentoAccessChecker.hasAccessToStabilimento(scadenza.getStabilimento().getId()))
                    .collect(Collectors.toList());
        }

        List<ScadenzaDTO> dtos = scadenze.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/imminenti")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScadenzaDTO>> getScadenzeImminenti(
            @RequestParam(defaultValue = "20") int giorni) {

        List<Scadenza> scadenze = scadenzaService.findScadenzeImminenti(giorni);

        // Filtra per stabilimenti accessibili all'utente
        if (!stabilimentoAccessChecker.isAdmin()) {
            scadenze = scadenze.stream()
                    .filter(scadenza -> scadenza.getStabilimento() != null &&
                            stabilimentoAccessChecker.hasAccessToStabilimento(scadenza.getStabilimento().getId()))
                    .collect(Collectors.toList());
        }

        List<ScadenzaDTO> dtos = scadenze.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScadenzaDTO> getScadenzaById(@PathVariable Long id) {
        Scadenza scadenza = scadenzaService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scadenza", "id", id));

        // Verifica accesso allo stabilimento della scadenza
        if (scadenza.getStabilimento() != null &&
                !stabilimentoAccessChecker.hasAccessToStabilimento(scadenza.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Scadenza", "id", id);
        }

        return ResponseEntity.ok(convertToDTO(scadenza));
    }

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<ScadenzaDTO> createScadenza(@Valid @RequestBody CreateScadenzaRequest request) {
        // Verifica accesso allo stabilimento se presente
        if (request.getStabilimentoId() != null &&
                !stabilimentoAccessChecker.hasAccessToStabilimento(request.getStabilimentoId())) {
            throw new ResourceNotFoundException("Stabilimento", "id", request.getStabilimentoId());
        }

        Scadenza scadenza = convertToEntity(request);
        Scadenza saved = scadenzaService.save(scadenza);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScadenzaDTO> updateScadenza(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScadenzaRequest request) {

        Scadenza existing = scadenzaService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scadenza", "id", id));

        // Verifica accesso allo stabilimento della scadenza
        if (existing.getStabilimento() != null &&
                !stabilimentoAccessChecker.hasAccessToStabilimento(existing.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Scadenza", "id", id);
        }

        updateEntityFromRequest(existing, request);
        Scadenza updated = scadenzaService.save(existing);

        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> deleteScadenza(@PathVariable Long id) {
        Scadenza scadenza = scadenzaService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scadenza", "id", id));

        // Verifica accesso allo stabilimento della scadenza
        if (scadenza.getStabilimento() != null &&
                !stabilimentoAccessChecker.hasAccessToStabilimento(scadenza.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Scadenza", "id", id);
        }

        scadenzaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Import da Excel ──────────────────────────────────────────────────

    /**
     * STEP 1 – Preview: carica il file e restituisce la lista delle righe parsate
     * senza salvare nulla. Il client può specificare un mapping sito→stabilimentoId
     * come parametro JSON opzionale.
     */
    @PostMapping(value = "/import/preview", consumes = "multipart/form-data")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<ImportScadenzeResult> previewImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mapping", required = false) String mappingJson) {
        try {
            Map<String, Long> mapping = parseMappingJson(mappingJson);
            ImportScadenzeResult result = importScadenzeService.preview(file, mapping);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * STEP 2 – Import effettivo: riceve la lista di righe (già elaborate dalla preview)
     * con il flag selezionata e le persiste.
     */
    @PostMapping("/import/confirm")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<ImportScadenzeResult> confirmImport(
            @RequestBody List<ImportScadenzeResult.RigaImport> righe) {
        ImportScadenzeResult result = importScadenzeService.importa(righe);
        return ResponseEntity.ok(result);
    }

    private Map<String, Long> parseMappingJson(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Long>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    // Metodi di conversione DTO <-> Entity

    private ScadenzaDTO convertToDTO(Scadenza scadenza) {
        ScadenzaDTO.ScadenzaDTOBuilder builder = ScadenzaDTO.builder()
                .id(scadenza.getId())
                .titolo(scadenza.getTitolo())
                .descrizione(scadenza.getDescrizione())
                .tipoScadenza(scadenza.getTipoScadenza())
                .dataScadenza(scadenza.getDataScadenza())
                .stato(scadenza.getStato())
                .priorita(scadenza.getPriorita())
                .responsabile(scadenza.getResponsabile())
                .emailNotifica(scadenza.getEmailNotifica())
                .giorniPreavviso(scadenza.getGiorniPreavviso())
                .dataCompletamento(scadenza.getDataCompletamento())
                .note(scadenza.getNote())
                .dataPrevistaAttivazione(scadenza.getDataPrevistaAttivazione())
                .riferimento(scadenza.getRiferimento())
                .sitoOrigine(scadenza.getSitoOrigine())
                .createdAt(scadenza.getCreatedAt());

        if (scadenza.getStabilimento() != null) {
            builder.stabilimentoId(scadenza.getStabilimento().getId())
                    .stabilimentoNome(scadenza.getStabilimento().getNome());
        }
        if (scadenza.getPrescrizione() != null) {
            builder.prescrizioneId(scadenza.getPrescrizione().getId());
        }
        if (scadenza.getMonitoraggio() != null) {
            builder.monitoraggioId(scadenza.getMonitoraggio().getId());
        }

        return builder.build();
    }

    private Scadenza convertToEntity(CreateScadenzaRequest request) {
        Scadenza scadenza = new Scadenza();

        if (request.getStabilimentoId() != null) {
            Stabilimento stabilimento = stabilimentoService.findById(request.getStabilimentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", request.getStabilimentoId()));
            scadenza.setStabilimento(stabilimento);
        }

        if (request.getPrescrizioneId() != null) {
            Prescrizione prescrizione = prescrizioneService.findById(request.getPrescrizioneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prescrizione", "id", request.getPrescrizioneId()));
            scadenza.setPrescrizione(prescrizione);
        }

        if (request.getMonitoraggioId() != null) {
            Monitoraggio monitoraggio = monitoraggioService.findById(request.getMonitoraggioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", request.getMonitoraggioId()));
            scadenza.setMonitoraggio(monitoraggio);
        }

        scadenza.setTitolo(request.getTitolo());
        scadenza.setDescrizione(request.getDescrizione());
        scadenza.setTipoScadenza(request.getTipoScadenza());
        scadenza.setDataScadenza(request.getDataScadenza());
        scadenza.setStato(request.getStato() != null ? request.getStato() : Scadenza.StatoScadenza.PENDING);
        scadenza.setPriorita(request.getPriorita());
        scadenza.setResponsabile(request.getResponsabile());
        scadenza.setEmailNotifica(request.getEmailNotifica());
        scadenza.setGiorniPreavviso(request.getGiorniPreavviso() != null ? request.getGiorniPreavviso() : 20);
        scadenza.setNote(request.getNote());

        return scadenza;
    }

    private void updateEntityFromRequest(Scadenza scadenza, UpdateScadenzaRequest request) {
        if (request.getTitolo() != null) {
            scadenza.setTitolo(request.getTitolo());
        }
        if (request.getDescrizione() != null) {
            scadenza.setDescrizione(request.getDescrizione());
        }
        if (request.getTipoScadenza() != null) {
            scadenza.setTipoScadenza(request.getTipoScadenza());
        }
        if (request.getDataScadenza() != null) {
            scadenza.setDataScadenza(request.getDataScadenza());
        }
        if (request.getStato() != null) {
            scadenza.setStato(request.getStato());
        }
        if (request.getPriorita() != null) {
            scadenza.setPriorita(request.getPriorita());
        }
        if (request.getResponsabile() != null) {
            scadenza.setResponsabile(request.getResponsabile());
        }
        if (request.getEmailNotifica() != null) {
            scadenza.setEmailNotifica(request.getEmailNotifica());
        }
        if (request.getGiorniPreavviso() != null) {
            scadenza.setGiorniPreavviso(request.getGiorniPreavviso());
        }
        if (request.getDataCompletamento() != null) {
            scadenza.setDataCompletamento(request.getDataCompletamento());
        }
        if (request.getNote() != null) {
            scadenza.setNote(request.getNote());
        }
    }
}
