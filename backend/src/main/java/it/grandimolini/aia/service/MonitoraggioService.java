package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.AnagraficaCaminoDTO;
import it.grandimolini.aia.dto.MonitoraggioDTO;
import it.grandimolini.aia.dto.ParametroMonitoraggioDTO;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.AnagraficaCamino;
import it.grandimolini.aia.model.Monitoraggio;
import it.grandimolini.aia.model.ParametroMonitoraggio;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.AnagraficaCaminoRepository;
import it.grandimolini.aia.repository.MonitoraggioRepository;
import it.grandimolini.aia.repository.ParametroMonitoraggioRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Monitoraggio (Monitoring Point) management.
 *
 * This service owns all business logic, entity↔DTO mapping, and
 * parametri (ParametroMonitoraggio) management. Controllers must
 * never import entity classes or repositories — they talk only to
 * this service and work exclusively with DTOs.
 *
 * All read methods are @Transactional(readOnly=true) to keep the
 * Hibernate session open during DTO mapping (lazy-loaded fields).
 * All write methods are @Transactional.
 */
@Service
public class MonitoraggioService {

    private final MonitoraggioRepository monitoraggioRepository;
    private final ParametroMonitoraggioRepository parametroRepo;
    private final StabilimentoRepository stabilimentoRepository;
    private final AnagraficaCaminoRepository anagraficaCaminoRepository;

    public MonitoraggioService(MonitoraggioRepository monitoraggioRepository,
                               ParametroMonitoraggioRepository parametroRepo,
                               StabilimentoRepository stabilimentoRepository,
                               AnagraficaCaminoRepository anagraficaCaminoRepository) {
        this.monitoraggioRepository = monitoraggioRepository;
        this.parametroRepo = parametroRepo;
        this.stabilimentoRepository = stabilimentoRepository;
        this.anagraficaCaminoRepository = anagraficaCaminoRepository;
    }

    // ────── READ — return DTOs ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MonitoraggioDTO> findAllAsDTOs() {
        return monitoraggioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MonitoraggioDTO> findByStabilimentoIdAsDTOs(Long stabilimentoId) {
        return monitoraggioRepository.findByStabilimentoId(stabilimentoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MonitoraggioDTO> findByIdAsDTO(Long id) {
        return monitoraggioRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ParametroMonitoraggioDTO> findParametriByMonitoraggioId(Long monitoraggioId) {
        return parametroRepo.findByMonitoraggioId(monitoraggioId).stream()
                .map(this::toParamDTO)
                .collect(Collectors.toList());
    }

    // ────── WRITE — Monitoraggio ────────────────────────────────────────────

    /**
     * Creates a new Monitoraggio from a DTO and returns the persisted DTO.
     */
    @Transactional
    public MonitoraggioDTO createFromDTO(MonitoraggioDTO dto) {
        Monitoraggio m = applyDtoToEntity(dto, new Monitoraggio());
        Monitoraggio saved = monitoraggioRepository.save(m);
        return toDTO(saved);
    }

    /**
     * Updates an existing Monitoraggio and returns the updated DTO.
     * Throws ResourceNotFoundException if not found.
     */
    @Transactional
    public MonitoraggioDTO updateFromDTO(Long id, MonitoraggioDTO dto) {
        Monitoraggio m = monitoraggioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", id));
        applyDtoToEntity(dto, m);
        Monitoraggio saved = monitoraggioRepository.save(m);
        return toDTO(saved);
    }

    /**
     * Deletes a Monitoraggio by id.
     * Throws ResourceNotFoundException if not found.
     */
    @Transactional
    public void deleteById(Long id) {
        if (!monitoraggioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Monitoraggio", "id", id);
        }
        monitoraggioRepository.deleteById(id);
    }

    // ────── WRITE — ParametroMonitoraggio ──────────────────────────────────

    /**
     * Adds a new ParametroMonitoraggio to the given monitoraggio.
     */
    @Transactional
    public ParametroMonitoraggioDTO addParametro(Long monitoraggioId, ParametroMonitoraggioDTO dto) {
        Monitoraggio m = monitoraggioRepository.findById(monitoraggioId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoraggio", "id", monitoraggioId));
        ParametroMonitoraggio p = applyParamDtoToEntity(dto, new ParametroMonitoraggio());
        p.setMonitoraggio(m);
        ParametroMonitoraggio saved = parametroRepo.save(p);
        return toParamDTO(saved);
    }

    /**
     * Updates an existing ParametroMonitoraggio.
     */
    @Transactional
    public ParametroMonitoraggioDTO updateParametro(Long monitoraggioId, Long parametroId,
                                                     ParametroMonitoraggioDTO dto) {
        ParametroMonitoraggio p = parametroRepo.findById(parametroId)
                .orElseThrow(() -> new ResourceNotFoundException("ParametroMonitoraggio", "id", parametroId));
        applyParamDtoToEntity(dto, p);
        ParametroMonitoraggio saved = parametroRepo.save(p);
        return toParamDTO(saved);
    }

    /**
     * Deletes a ParametroMonitoraggio by id.
     */
    @Transactional
    public void deleteParametro(Long parametroId) {
        parametroRepo.deleteById(parametroId);
    }

    // ────── INTERNAL: entity-level helpers (used by other services) ────────

    /** @deprecated Use DTO methods. Only for services that need entity access. */
    @Transactional(readOnly = true)
    public Optional<Monitoraggio> findById(Long id) {
        return monitoraggioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Monitoraggio> findAll() {
        return monitoraggioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Monitoraggio> findByStabilimentoId(Long stabilimentoId) {
        return monitoraggioRepository.findByStabilimentoId(stabilimentoId);
    }

    @Transactional(readOnly = true)
    public List<Monitoraggio> findAllAttivi() {
        return monitoraggioRepository.findByAttivoTrue();
    }

    // ────── MAPPING — private ───────────────────────────────────────────────

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

    /**
     * Applies a MonitoraggioDTO to a Monitoraggio entity (create or update).
     * Resolves Stabilimento and AnagraficaCamino from their repositories.
     */
    private Monitoraggio applyDtoToEntity(MonitoraggioDTO dto, Monitoraggio m) {
        if (dto.getStabilimentoId() != null) {
            Stabilimento st = stabilimentoRepository.findById(dto.getStabilimentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", dto.getStabilimentoId()));
            m.setStabilimento(st);
        }
        if (dto.getCodice() != null)               m.setCodice(dto.getCodice());
        if (dto.getDescrizione() != null)          m.setDescrizione(dto.getDescrizione());
        if (dto.getTipoMonitoraggio() != null)     m.setTipoMonitoraggio(dto.getTipoMonitoraggio());
        if (dto.getPuntoEmissione() != null)       m.setPuntoEmissione(dto.getPuntoEmissione());
        if (dto.getFrequenza() != null)            m.setFrequenza(dto.getFrequenza());
        if (dto.getProssimaScadenza() != null)     m.setProssimaScadenza(dto.getProssimaScadenza());
        if (dto.getLaboratorio() != null)          m.setLaboratorio(dto.getLaboratorio());
        if (dto.getMetodica() != null)             m.setMetodica(dto.getMetodica());
        if (dto.getNormativaRiferimento() != null) m.setNormativaRiferimento(dto.getNormativaRiferimento());
        if (dto.getMatricola() != null)            m.setMatricola(dto.getMatricola());
        if (dto.getAttivo() != null)               m.setAttivo(dto.getAttivo());
        else if (m.getAttivo() == null)            m.setAttivo(true);

        if (dto.getAnagraficaCaminoId() != null) {
            AnagraficaCamino ac = anagraficaCaminoRepository.findById(dto.getAnagraficaCaminoId())
                    .orElseThrow(() -> new ResourceNotFoundException("AnagraficaCamino", "id", dto.getAnagraficaCaminoId()));
            m.setAnagraficaCamino(ac);
        } else {
            m.setAnagraficaCamino(null);
        }
        return m;
    }

    /**
     * Applies a ParametroMonitoraggioDTO to a ParametroMonitoraggio entity (create or update).
     */
    private ParametroMonitoraggio applyParamDtoToEntity(ParametroMonitoraggioDTO dto,
                                                         ParametroMonitoraggio p) {
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
