package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.RilevazioneMisuraDTO;
import it.grandimolini.aia.model.ParametroMonitoraggio;
import it.grandimolini.aia.model.RilevazioneMisura;
import it.grandimolini.aia.repository.ParametroMonitoraggioRepository;
import it.grandimolini.aia.repository.RilevazioneMisuraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Conformity (Conformita) management.
 *
 * Handles DTO mapping for RilevazioneMisura entities that require access to lazy-loaded relationships:
 * - parametroMonitoraggio.monitoraggio (lazy)
 * - monitoraggio.stabilimento (lazy)
 *
 * All query methods are marked @Transactional(readOnly=true) to keep the Hibernate session
 * open during DTO mapping, preventing LazyInitializationException.
 */
@Service
public class ConformitaService {

    private final RilevazioneMisuraRepository rilevRepo;
    private final ParametroMonitoraggioRepository parametroRepo;

    public ConformitaService(RilevazioneMisuraRepository rilevRepo,
                             ParametroMonitoraggioRepository parametroRepo) {
        this.rilevRepo = rilevRepo;
        this.parametroRepo = parametroRepo;
    }

    // ────── READ METHODS (all @Transactional(readOnly = true)) ──────────

    @Transactional(readOnly = true)
    public List<RilevazioneMisuraDTO> getRilevazioni(
            Long parametroId,
            Long monitoraggioId,
            Long stabilimentoId,
            LocalDate from,
            LocalDate to) {

        List<RilevazioneMisura> list;
        if (parametroId != null && from != null && to != null) {
            list = rilevRepo.findByParametroMonitoraggioIdAndDataCampionamentoBetweenOrderByDataCampionamentoDesc(parametroId, from, to);
        } else if (parametroId != null) {
            list = rilevRepo.findByParametroMonitoraggioIdOrderByDataCampionamentoDesc(parametroId);
        } else if (monitoraggioId != null) {
            list = rilevRepo.findByMonitoraggioId(monitoraggioId);
        } else if (stabilimentoId != null && from != null && to != null) {
            list = rilevRepo.findByStabilimentoAndPeriodo(stabilimentoId, from, to);
        } else {
            list = rilevRepo.findAll();
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RilevazioneMisuraDTO> findById(Long id) {
        return rilevRepo.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<RilevazioneMisuraDTO> getDashboard(Long stabilimentoId) {
        return rilevRepo.findUltimeRilevazioniByStabilimento(stabilimentoId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RilevazioneMisuraDTO> getDashboardMonitoraggio(Long monitoraggioId) {
        return rilevRepo.findUltimeRilevazioniByMonitoraggio(monitoraggioId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RilevazioneMisuraDTO> getNonConformi(Long stabilimentoId) {
        return rilevRepo.findNonConformiByStabilimento(stabilimentoId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getRiepilogo(Long stabilimentoId) {
        List<RilevazioneMisura> ultime = rilevRepo.findUltimeRilevazioniByStabilimento(stabilimentoId);
        return ultime.stream()
                .filter(r -> r.getStatoConformita() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getStatoConformita().name(),
                        Collectors.counting()
                ));
    }

    // ────── WRITE METHODS ──────────

    @Transactional
    public RilevazioneMisuraDTO create(RilevazioneMisuraDTO dto) {
        ParametroMonitoraggio param = parametroRepo.findById(dto.getParametroMonitoraggioId())
                .orElseThrow(() -> new RuntimeException("Parametro non trovato"));

        RilevazioneMisura r = new RilevazioneMisura();
        r.setParametroMonitoraggio(param);
        r.setDataCampionamento(dto.getDataCampionamento());
        r.setValoreMisurato(dto.getValoreMisurato());
        r.setUnitaMisura(dto.getUnitaMisura() != null ? dto.getUnitaMisura() : param.getUnitaMisura());
        r.setRapportoProva(dto.getRapportoProva());
        r.setLaboratorio(dto.getLaboratorio());
        r.setNote(dto.getNote());
        // calcolaConformita() called in @PrePersist
        RilevazioneMisura saved = rilevRepo.save(r);
        return toDTO(saved);
    }

    @Transactional
    public Optional<RilevazioneMisuraDTO> update(Long id, RilevazioneMisuraDTO dto) {
        return rilevRepo.findById(id).map(r -> {
            if (dto.getDataCampionamento() != null) r.setDataCampionamento(dto.getDataCampionamento());
            if (dto.getValoreMisurato() != null)    r.setValoreMisurato(dto.getValoreMisurato());
            if (dto.getUnitaMisura() != null)       r.setUnitaMisura(dto.getUnitaMisura());
            if (dto.getRapportoProva() != null)     r.setRapportoProva(dto.getRapportoProva());
            if (dto.getLaboratorio() != null)       r.setLaboratorio(dto.getLaboratorio());
            if (dto.getNote() != null)              r.setNote(dto.getNote());
            r.calcolaConformita();
            RilevazioneMisura saved = rilevRepo.save(r);
            return toDTO(saved);
        });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!rilevRepo.existsById(id)) {
            return false;
        }
        rilevRepo.deleteById(id);
        return true;
    }

    // ────── MAPPING (only accessible within transaction) ──────────

    /**
     * Maps RilevazioneMisura to DTO.
     * Must be called within a @Transactional context to avoid LazyInitializationException
     * when accessing lazy-loaded relationships.
     */
    private RilevazioneMisuraDTO toDTO(RilevazioneMisura r) {
        RilevazioneMisuraDTO dto = new RilevazioneMisuraDTO();
        dto.setId(r.getId());

        ParametroMonitoraggio param = r.getParametroMonitoraggio();
        dto.setParametroMonitoraggioId(param.getId());
        dto.setParametroNome(param.getNome());
        dto.setParametroCodice(param.getCodice());
        dto.setParametroUnitaMisura(param.getUnitaMisura());
        dto.setParametroLimiteValore(param.getLimiteValore());
        dto.setParametroLimiteRiferimento(param.getLimiteRiferimento());

        var mon = param.getMonitoraggio();
        dto.setMonitoraggioId(mon.getId());
        dto.setMonitoraggioCodice(mon.getCodice());
        dto.setMonitoraggioDescrizione(mon.getDescrizione());
        dto.setMonitoraggioTipo(mon.getTipoMonitoraggio() != null ? mon.getTipoMonitoraggio().name() : null);

        var stab = mon.getStabilimento();
        dto.setStabilimentoId(stab.getId());
        dto.setStabilimentoNome(stab.getNome());

        dto.setDataCampionamento(r.getDataCampionamento());
        dto.setValoreMisurato(r.getValoreMisurato());
        dto.setUnitaMisura(r.getUnitaMisura());
        dto.setStatoConformita(r.getStatoConformita());
        dto.setRapportoProva(r.getRapportoProva());
        dto.setLaboratorio(r.getLaboratorio());
        dto.setNote(r.getNote());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
