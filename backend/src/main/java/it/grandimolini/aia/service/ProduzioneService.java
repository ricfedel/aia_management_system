package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.RegistroMensileDTO;
import it.grandimolini.aia.dto.VoceProduzioneDTO;
import it.grandimolini.aia.model.RegistroMensile;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.model.VoceProduzione;
import it.grandimolini.aia.repository.RegistroMensileRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import it.grandimolini.aia.repository.VoceProduzioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProduzioneService {

    private final RegistroMensileRepository registroRepo;
    private final VoceProduzioneRepository voceRepo;
    private final StabilimentoRepository stabilimentoRepo;

    public ProduzioneService(RegistroMensileRepository registroRepo,
                            VoceProduzioneRepository voceRepo,
                            StabilimentoRepository stabilimentoRepo) {
        this.registroRepo = registroRepo;
        this.voceRepo = voceRepo;
        this.stabilimentoRepo = stabilimentoRepo;
    }

    // ─── Anni disponibili ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Integer> getAnni() {
        return registroRepo.findAnniDistinti();
    }

    @Transactional(readOnly = true)
    public List<Integer> getAnniByStabilimento(Long stabilimentoId) {
        return registroRepo.findAnniByStabilimento(stabilimentoId);
    }

    // ─── Registri ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RegistroMensileDTO> getAll(Long stabilimentoId, Integer anno) {
        List<RegistroMensile> list;
        if (stabilimentoId != null && anno != null) {
            list = registroRepo.findByStabilimentoIdAndAnnoOrderByMeseAsc(stabilimentoId, anno);
        } else if (stabilimentoId != null) {
            list = registroRepo.findByStabilimentoIdOrderByAnnoDescMeseDesc(stabilimentoId);
        } else if (anno != null) {
            list = registroRepo.findByAnnoOrderByStabilimentoNomeAscMeseAsc(anno);
        } else {
            list = registroRepo.findAll();
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RegistroMensileDTO> findById(Long id) {
        return registroRepo.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<RegistroMensileDTO> findByStabilimentoAnnoMese(Long stabilimentoId, Integer anno, Integer mese) {
        return registroRepo.findByStabilimentoIdAndAnnoAndMese(stabilimentoId, anno, mese)
                .map(this::toDTO);
    }

    @Transactional
    public RegistroMensileDTO create(RegistroMensileDTO dto) {
        // Verifica unicità
        if (registroRepo.findByStabilimentoIdAndAnnoAndMese(dto.getStabilimentoId(), dto.getAnno(), dto.getMese()).isPresent()) {
            throw new IllegalStateException("CONFLICT");
        }

        Stabilimento stabilimento = stabilimentoRepo.findById(dto.getStabilimentoId())
                .orElseThrow(() -> new RuntimeException("Stabilimento non trovato"));

        RegistroMensile registro = new RegistroMensile();
        registro.setStabilimento(stabilimento);
        registro.setAnno(dto.getAnno());
        registro.setMese(dto.getMese());
        registro.setStato(dto.getStato() != null ? dto.getStato() : RegistroMensile.StatoRegistro.BOZZA);
        registro.setNote(dto.getNote());
        registro.setCompilatoDa(dto.getCompilatoDa());

        RegistroMensile saved = registroRepo.save(registro);

        // Salva voci se presenti
        if (dto.getVoci() != null) {
            for (VoceProduzioneDTO vDTO : dto.getVoci()) {
                VoceProduzione voce = toVoceEntity(vDTO, saved);
                voceRepo.save(voce);
            }
            saved = registroRepo.findById(saved.getId()).orElse(saved);
        }

        return toDTO(saved);
    }

    @Transactional
    public Optional<RegistroMensileDTO> update(Long id, RegistroMensileDTO dto) {
        return registroRepo.findById(id).map(registro -> {
            if (dto.getStato() != null) registro.setStato(dto.getStato());
            if (dto.getNote() != null) registro.setNote(dto.getNote());
            if (dto.getCompilatoDa() != null) registro.setCompilatoDa(dto.getCompilatoDa());
            return toDTO(registroRepo.save(registro));
        });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!registroRepo.existsById(id)) {
            return false;
        }
        registroRepo.deleteById(id);
        return true;
    }

    // ─── Voci ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VoceProduzioneDTO> getVoci(Long registroId) {
        return voceRepo.findByRegistroMensileIdOrderByCategoriaAscSortOrderAscDescrizioneAsc(registroId)
                .stream().map(this::toVoceDTO).collect(Collectors.toList());
    }

    @Transactional
    public VoceProduzioneDTO addVoce(Long registroId, VoceProduzioneDTO dto) {
        RegistroMensile registro = registroRepo.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro non trovato"));
        VoceProduzione voce = toVoceEntity(dto, registro);
        return toVoceDTO(voceRepo.save(voce));
    }

    @Transactional
    public Optional<VoceProduzioneDTO> updateVoce(Long registroId, Long voceId, VoceProduzioneDTO dto) {
        return voceRepo.findById(voceId).map(voce -> {
            if (voce.getRegistroMensile().getId().equals(registroId)) {
                if (dto.getCategoria() != null) voce.setCategoria(dto.getCategoria());
                if (dto.getDescrizione() != null) voce.setDescrizione(dto.getDescrizione());
                if (dto.getCodice() != null) voce.setCodice(dto.getCodice());
                voce.setQuantita(dto.getQuantita());
                if (dto.getUnitaMisura() != null) voce.setUnitaMisura(dto.getUnitaMisura());
                voce.setQuantitaAnnoPrecedente(dto.getQuantitaAnnoPrecedente());
                if (dto.getNote() != null) voce.setNote(dto.getNote());
                if (dto.getSortOrder() != null) voce.setSortOrder(dto.getSortOrder());
            }
            return toVoceDTO(voceRepo.save(voce));
        });
    }

    @Transactional
    public boolean deleteVoce(Long voceId) {
        if (!voceRepo.existsById(voceId)) {
            return false;
        }
        voceRepo.deleteById(voceId);
        return true;
    }

    // ─── Batch: salva tutte le voci di un registro ────────────────────

    @Transactional
    public RegistroMensileDTO saveVociBatch(Long registroId, List<VoceProduzioneDTO> vociDTO) {
        RegistroMensile registro = registroRepo.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro non trovato"));

        // Rimuovi voci esistenti e ricrea
        registro.getVoci().clear();
        registroRepo.save(registro);

        for (VoceProduzioneDTO dto : vociDTO) {
            VoceProduzione voce = toVoceEntity(dto, registro);
            voceRepo.save(voce);
        }

        return toDTO(registroRepo.findById(registroId).orElse(registro));
    }

    // ─── Mapping ──────────────────────────────────────────────────────

    private RegistroMensileDTO toDTO(RegistroMensile r) {
        RegistroMensileDTO dto = new RegistroMensileDTO();
        dto.setId(r.getId());
        dto.setStabilimentoId(r.getStabilimento().getId());
        dto.setStabilimentoNome(r.getStabilimento().getNome());
        dto.setAnno(r.getAnno());
        dto.setMese(r.getMese());
        dto.setStato(r.getStato());
        dto.setNote(r.getNote());
        dto.setCompilatoDa(r.getCompilatoDa());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        dto.setVoci(r.getVoci().stream().map(this::toVoceDTO).collect(Collectors.toList()));
        return dto;
    }

    private VoceProduzioneDTO toVoceDTO(VoceProduzione v) {
        VoceProduzioneDTO dto = new VoceProduzioneDTO();
        dto.setId(v.getId());
        dto.setRegistroMensileId(v.getRegistroMensile().getId());
        dto.setCategoria(v.getCategoria());
        dto.setDescrizione(v.getDescrizione());
        dto.setCodice(v.getCodice());
        dto.setQuantita(v.getQuantita());
        dto.setUnitaMisura(v.getUnitaMisura());
        dto.setQuantitaAnnoPrecedente(v.getQuantitaAnnoPrecedente());
        dto.setNote(v.getNote());
        dto.setSortOrder(v.getSortOrder());
        return dto;
    }

    private VoceProduzione toVoceEntity(VoceProduzioneDTO dto, RegistroMensile registro) {
        VoceProduzione v = new VoceProduzione();
        v.setRegistroMensile(registro);
        v.setCategoria(dto.getCategoria());
        v.setDescrizione(dto.getDescrizione());
        v.setCodice(dto.getCodice());
        v.setQuantita(dto.getQuantita());
        v.setUnitaMisura(dto.getUnitaMisura() != null ? dto.getUnitaMisura() : "");
        v.setQuantitaAnnoPrecedente(dto.getQuantitaAnnoPrecedente());
        v.setNote(dto.getNote());
        v.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        return v;
    }
}
