package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.ComunicazioneEnteDTO;
import it.grandimolini.aia.model.ComunicazioneEnte;
import it.grandimolini.aia.model.ComunicazioneEnte.EnteEsterno;
import it.grandimolini.aia.model.ComunicazioneEnte.StatoComunicazione;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.ComunicazioneEnteRepository;
import it.grandimolini.aia.repository.PrescrizioneRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComunicazioneService {

    private final ComunicazioneEnteRepository comunicazioniRepo;
    private final StabilimentoRepository stabilimentoRepo;
    private final PrescrizioneRepository prescrizioneRepo;

    // ────── READ methods ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ComunicazioneEnteDTO> findFiltered(
            Long stabilimentoId, String stato, String ente, LocalDate from, LocalDate to) {

        StatoComunicazione statoEnum = stato != null ? StatoComunicazione.valueOf(stato) : null;
        EnteEsterno enteEnum = ente != null ? EnteEsterno.valueOf(ente) : null;

        return comunicazioniRepo
                .findFiltered(stabilimentoId, statoEnum, enteEnum, from, to)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ComunicazioneEnteDTO> findByIdAsDTO(Long id) {
        return comunicazioniRepo.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ComunicazioneEnteDTO> findInAttesaRiscontro(Long stabilimentoId, int giorniSoglia) {
        LocalDate soglia = LocalDate.now().minusDays(giorniSoglia);
        return comunicazioniRepo.findInAttesaRiscontro(stabilimentoId, soglia)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getRiepilogoPerStato(Long stabilimentoId) {
        Map<String, Long> riepilogo = new HashMap<>();
        for (StatoComunicazione stato : StatoComunicazione.values()) {
            long count = comunicazioniRepo.countByStabilimentoIdAndStato(stabilimentoId, stato);
            riepilogo.put(stato.name(), count);
        }
        return riepilogo;
    }

    // ────── WRITE methods ──────────────────────────────────────────────────────

    @Transactional
    public ComunicazioneEnteDTO createFromDTO(ComunicazioneEnteDTO dto) {
        Stabilimento stab = stabilimentoRepo.findById(dto.getStabilimentoId())
                .orElseThrow(() -> new RuntimeException("Stabilimento non trovato"));

        ComunicazioneEnte entity = fromDTO(dto, new ComunicazioneEnte());
        entity.setStabilimento(stab);

        if (dto.getPrescrizioneId() != null) {
            prescrizioneRepo.findById(dto.getPrescrizioneId())
                    .ifPresent(entity::setPrescrizione);
        }

        return toDTO(comunicazioniRepo.save(entity));
    }

    @Transactional
    public Optional<ComunicazioneEnteDTO> updateFromDTO(Long id, ComunicazioneEnteDTO dto) {
        return comunicazioniRepo.findById(id).map(entity -> {
            fromDTO(dto, entity);

            if (dto.getStabilimentoId() != null) {
                stabilimentoRepo.findById(dto.getStabilimentoId())
                        .ifPresent(entity::setStabilimento);
            }

            if (dto.getPrescrizioneId() != null) {
                prescrizioneRepo.findById(dto.getPrescrizioneId())
                        .ifPresent(entity::setPrescrizione);
            } else {
                entity.setPrescrizione(null);
            }

            return toDTO(comunicazioniRepo.save(entity));
        });
    }

    @Transactional
    public Optional<ComunicazioneEnteDTO> cambiaStato(Long id, String stato) {
        return comunicazioniRepo.findById(id).map(entity -> {
            entity.setStato(StatoComunicazione.valueOf(stato));
            return toDTO(comunicazioniRepo.save(entity));
        });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!comunicazioniRepo.existsById(id)) {
            return false;
        }
        comunicazioniRepo.deleteById(id);
        return true;
    }

    // ────── Mapping methods ────────────────────────────────────────────────────

    private ComunicazioneEnteDTO toDTO(ComunicazioneEnte c) {
        ComunicazioneEnteDTO dto = new ComunicazioneEnteDTO();
        dto.setId(c.getId());
        if (c.getStabilimento() != null) {
            dto.setStabilimentoId(c.getStabilimento().getId());
            dto.setStabilimentoNome(c.getStabilimento().getNome());
        }
        dto.setTipo(c.getTipo());
        dto.setStato(c.getStato());
        dto.setEnte(c.getEnte());
        dto.setEnteUfficio(c.getEnteUfficio());
        dto.setEnteReferente(c.getEnteReferente());
        dto.setOggetto(c.getOggetto());
        dto.setDataInvio(c.getDataInvio());
        dto.setNumeroPecInvio(c.getNumeroPecInvio());
        dto.setProtocolloInterno(c.getProtocolloInterno());
        dto.setProtocolloEnte(c.getProtocolloEnte());
        dto.setContenuto(c.getContenuto());
        dto.setNote(c.getNote());
        dto.setAllegati(c.getAllegati());
        dto.setHasRiscontro(c.getHasRiscontro());
        dto.setDataRiscontro(c.getDataRiscontro());
        dto.setProtocolloRiscontro(c.getProtocolloRiscontro());
        dto.setNoteRiscontro(c.getNoteRiscontro());
        dto.setAllegatiRiscontro(c.getAllegatiRiscontro());
        if (c.getPrescrizione() != null) {
            dto.setPrescrizioneId(c.getPrescrizione().getId());
            dto.setPrescrizioneOggetto(c.getPrescrizione().getDescrizione());
        }
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setCreatedBy(c.getCreatedBy());
        return dto;
    }

    private ComunicazioneEnte fromDTO(ComunicazioneEnteDTO dto, ComunicazioneEnte entity) {
        if (dto.getTipo() != null) entity.setTipo(dto.getTipo());
        if (dto.getStato() != null) entity.setStato(dto.getStato());
        if (dto.getEnte() != null) entity.setEnte(dto.getEnte());
        entity.setEnteUfficio(dto.getEnteUfficio());
        entity.setEnteReferente(dto.getEnteReferente());
        entity.setOggetto(dto.getOggetto());
        entity.setDataInvio(dto.getDataInvio());
        entity.setNumeroPecInvio(dto.getNumeroPecInvio());
        entity.setProtocolloInterno(dto.getProtocolloInterno());
        entity.setProtocolloEnte(dto.getProtocolloEnte());
        entity.setContenuto(dto.getContenuto());
        entity.setNote(dto.getNote());
        entity.setAllegati(dto.getAllegati());
        if (dto.getHasRiscontro() != null) entity.setHasRiscontro(dto.getHasRiscontro());
        entity.setDataRiscontro(dto.getDataRiscontro());
        entity.setProtocolloRiscontro(dto.getProtocolloRiscontro());
        entity.setNoteRiscontro(dto.getNoteRiscontro());
        entity.setAllegatiRiscontro(dto.getAllegatiRiscontro());
        entity.setCreatedBy(dto.getCreatedBy());
        return entity;
    }
}
