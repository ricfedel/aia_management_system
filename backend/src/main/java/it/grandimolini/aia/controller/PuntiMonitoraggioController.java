package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.AnagraficaCaminoDTO;
import it.grandimolini.aia.dto.MonitoraggioDTO;
import it.grandimolini.aia.dto.ParametroMonitoraggioDTO;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.AnagraficaCamino;
import it.grandimolini.aia.model.Monitoraggio;
import it.grandimolini.aia.model.ParametroMonitoraggio;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.AnagraficaCaminoRepository;
import it.grandimolini.aia.repository.ParametroMonitoraggioRepository;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.MonitoraggioService;
import it.grandimolini.aia.service.StabilimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestione dei Punti di Monitoraggio e dei loro Parametri.
 * I punti di monitoraggio (scarichi S1/S2, camini E1-E98, piezometri, sorgenti sonore)
 * sono la struttura di base del PMC: ogni punto ha parametri con limiti e frequenze.
 */
@RestController
@RequestMapping("/api/punti-monitoraggio")
public class PuntiMonitoraggioController {

    @Autowired private MonitoraggioService monitoraggioService;
    @Autowired private StabilimentoService stabilimentoService;
    @Autowired private ParametroMonitoraggioRepository parametroRepo;
    @Autowired private AnagraficaCaminoRepository anagraficaCaminoRepo;
    @Autowired private StabilimentoAccessChecker stabilimentoAccessChecker;

    // ─── PUNTI DI MONITORAGGIO ────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MonitoraggioDTO>> getAll() {
        List<Monitoraggio> list = stabilimentoAccessChecker.isAdmin()
                ? monitoraggioService.findAll()
                : monitoraggioService.findAll().stream()
                    .filter(m -> stabilimentoAccessChecker.hasAccessToStabilimento(m.getStabilimento().getId()))
                    .collect(Collectors.toList());
        return ResponseEntity.ok(list.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/stabilimento/{stabilimentoId}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<List<MonitoraggioDTO>> getByStabilimento(@PathVariable Long stabilimentoId) {
        List<Monitoraggio> list = monitoraggioService.findByStabilimentoId(stabilimentoId);
        return ResponseEntity.ok(list.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MonitoraggioDTO> getById(@PathVariable Long id) {
        Monitoraggio m = monitoraggioService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", id));
        checkAccess(m);
        return ResponseEntity.ok(toDTO(m));
    }

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<MonitoraggioDTO> create(@RequestBody MonitoraggioDTO dto) {
        Monitoraggio m = fromDTO(dto, new Monitoraggio());
        Monitoraggio saved = monitoraggioService.save(m);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<MonitoraggioDTO> update(@PathVariable Long id, @RequestBody MonitoraggioDTO dto) {
        Monitoraggio m = monitoraggioService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", id));
        checkAccess(m);
        fromDTO(dto, m);
        return ResponseEntity.ok(toDTO(monitoraggioService.save(m)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Monitoraggio m = monitoraggioService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", id));
        checkAccess(m);
        monitoraggioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── PARAMETRI ────────────────────────────────────────────────────────

    @GetMapping("/{monitoraggioId}/parametri")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParametroMonitoraggioDTO>> getParametri(@PathVariable Long monitoraggioId) {
        Monitoraggio m = monitoraggioService.findById(monitoraggioId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", monitoraggioId));
        checkAccess(m);
        List<ParametroMonitoraggio> parametri = parametroRepo.findByMonitoraggioId(monitoraggioId);
        return ResponseEntity.ok(parametri.stream().map(this::toParamDTO).collect(Collectors.toList()));
    }

    @PostMapping("/{monitoraggioId}/parametri")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<ParametroMonitoraggioDTO> addParametro(
            @PathVariable Long monitoraggioId,
            @RequestBody ParametroMonitoraggioDTO dto) {
        Monitoraggio m = monitoraggioService.findById(monitoraggioId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", monitoraggioId));
        checkAccess(m);
        ParametroMonitoraggio p = fromParamDTO(dto, new ParametroMonitoraggio());
        p.setMonitoraggio(m);
        ParametroMonitoraggio saved = parametroRepo.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(toParamDTO(saved));
    }

    @PutMapping("/{monitoraggioId}/parametri/{parametroId}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<ParametroMonitoraggioDTO> updateParametro(
            @PathVariable Long monitoraggioId,
            @PathVariable Long parametroId,
            @RequestBody ParametroMonitoraggioDTO dto) {
        ParametroMonitoraggio p = parametroRepo.findById(parametroId)
                .orElseThrow(() -> new ResourceNotFoundException("ParametroMonitoraggio", "id", parametroId));
        fromParamDTO(dto, p);
        return ResponseEntity.ok(toParamDTO(parametroRepo.save(p)));
    }

    @DeleteMapping("/{monitoraggioId}/parametri/{parametroId}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> deleteParametro(
            @PathVariable Long monitoraggioId,
            @PathVariable Long parametroId) {
        parametroRepo.deleteById(parametroId);
        return ResponseEntity.noContent().build();
    }

    // ─── helpers ──────────────────────────────────────────────────────────

    private void checkAccess(Monitoraggio m) {
        if (!stabilimentoAccessChecker.isAdmin() &&
            !stabilimentoAccessChecker.hasAccessToStabilimento(m.getStabilimento().getId())) {
            throw new ResourceNotFoundException("Monitoraggio", "id", m.getId());
        }
    }

    private MonitoraggioDTO toDTO(Monitoraggio m) {
        List<ParametroMonitoraggioDTO> parametriDTO = parametroRepo
                .findByMonitoraggioId(m.getId()).stream()
                .map(this::toParamDTO)
                .collect(Collectors.toList());
        return MonitoraggioDTO.builder()
                .id(m.getId())
                .stabilimentoId(m.getStabilimento().getId())
                .stabilimentoNome(m.getStabilimento().getNome())
                .codice(m.getCodice())
                .descrizione(m.getDescrizione())
                .tipoMonitoraggio(m.getTipoMonitoraggio())
                .puntoEmissione(m.getPuntoEmissione())
                .frequenza(m.getFrequenza())
                .prossimaScadenza(m.getProssimaScadenza())
                .laboratorio(m.getLaboratorio())
                .metodica(m.getMetodica())
                .normativaRiferimento(m.getNormativaRiferimento())
                .matricola(m.getMatricola())
                .attivo(m.getAttivo())
                .createdAt(m.getCreatedAt())
                .parametri(parametriDTO)
                .anagraficaCaminoId(m.getAnagraficaCamino() != null ? m.getAnagraficaCamino().getId() : null)
                .anagraficaCamino(m.getAnagraficaCamino() != null ? toAnagraficaDTO(m.getAnagraficaCamino()) : null)
                .build();
    }

    private AnagraficaCaminoDTO toAnagraficaDTO(AnagraficaCamino c) {
        return AnagraficaCaminoDTO.builder()
                .id(c.getId())
                .stabilimentoId(c.getStabilimento().getId())
                .stabilimentoNome(c.getStabilimento().getNome())
                .sigla(c.getSigla())
                .faseProcesso(c.getFaseProcesso())
                .origine(c.getOrigine())
                .portataNomc3h(c.getPortataNomc3h())
                .sezioneM2(c.getSezioneM2())
                .velocitaMs(c.getVelocitaMs())
                .temperaturaC(c.getTemperaturaC())
                .temperaturaAmbiente(c.getTemperaturaAmbiente())
                .altezzaM(c.getAltezzaM())
                .durataHGiorno(c.getDurataHGiorno())
                .durataGAnno(c.getDurataGAnno())
                .impiantoAbbattimento(c.getImpiantoAbbattimento())
                .note(c.getNote())
                .attivo(c.getAttivo())
                .build();
    }

    private Monitoraggio fromDTO(MonitoraggioDTO dto, Monitoraggio m) {
        if (dto.getStabilimentoId() != null) {
            Stabilimento st = stabilimentoService.findById(dto.getStabilimentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stabilimento","id",dto.getStabilimentoId()));
            m.setStabilimento(st);
        }
        if (dto.getCodice() != null)              m.setCodice(dto.getCodice());
        if (dto.getDescrizione() != null)         m.setDescrizione(dto.getDescrizione());
        if (dto.getTipoMonitoraggio() != null)    m.setTipoMonitoraggio(dto.getTipoMonitoraggio());
        if (dto.getPuntoEmissione() != null)      m.setPuntoEmissione(dto.getPuntoEmissione());
        if (dto.getFrequenza() != null)           m.setFrequenza(dto.getFrequenza());
        if (dto.getProssimaScadenza() != null)    m.setProssimaScadenza(dto.getProssimaScadenza());
        if (dto.getLaboratorio() != null)         m.setLaboratorio(dto.getLaboratorio());
        if (dto.getMetodica() != null)            m.setMetodica(dto.getMetodica());
        if (dto.getNormativaRiferimento() != null) m.setNormativaRiferimento(dto.getNormativaRiferimento());
        if (dto.getMatricola() != null)           m.setMatricola(dto.getMatricola());
        if (dto.getAttivo() != null)              m.setAttivo(dto.getAttivo());
        else if (m.getAttivo() == null)           m.setAttivo(true);
        // collegamento anagrafica camino (nullable)
        if (dto.getAnagraficaCaminoId() != null) {
            AnagraficaCamino ac = anagraficaCaminoRepo.findById(dto.getAnagraficaCaminoId())
                    .orElseThrow(() -> new ResourceNotFoundException("AnagraficaCamino", "id", dto.getAnagraficaCaminoId()));
            m.setAnagraficaCamino(ac);
        } else {
            m.setAnagraficaCamino(null);
        }
        return m;
    }

    private ParametroMonitoraggioDTO toParamDTO(ParametroMonitoraggio p) {
        return ParametroMonitoraggioDTO.builder()
                .id(p.getId())
                .monitoraggioId(p.getMonitoraggio().getId())
                .nome(p.getNome())
                .codice(p.getCodice())
                .unitaMisura(p.getUnitaMisura())
                .limiteValore(p.getLimiteValore())
                .limiteUnita(p.getLimiteUnita())
                .limiteRiferimento(p.getLimiteRiferimento())
                .frequenza(p.getFrequenza())
                .metodoAnalisi(p.getMetodoAnalisi())
                .note(p.getNote())
                .attivo(p.getAttivo())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private ParametroMonitoraggio fromParamDTO(ParametroMonitoraggioDTO dto, ParametroMonitoraggio p) {
        if (dto.getNome() != null)              p.setNome(dto.getNome());
        if (dto.getCodice() != null)            p.setCodice(dto.getCodice());
        if (dto.getUnitaMisura() != null)       p.setUnitaMisura(dto.getUnitaMisura());
        if (dto.getLimiteValore() != null)      p.setLimiteValore(dto.getLimiteValore());
        if (dto.getLimiteUnita() != null)       p.setLimiteUnita(dto.getLimiteUnita());
        if (dto.getLimiteRiferimento() != null) p.setLimiteRiferimento(dto.getLimiteRiferimento());
        if (dto.getFrequenza() != null)         p.setFrequenza(dto.getFrequenza());
        if (dto.getMetodoAnalisi() != null)     p.setMetodoAnalisi(dto.getMetodoAnalisi());
        if (dto.getNote() != null)              p.setNote(dto.getNote());
        p.setAttivo(dto.getAttivo() != null ? dto.getAttivo() : true);
        return p;
    }
}
