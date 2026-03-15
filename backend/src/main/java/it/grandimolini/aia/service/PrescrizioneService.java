package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.PrescrizioneDTO;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.repository.PrescrizioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrescrizioneService {

    @Autowired
    private PrescrizioneRepository prescrizioneRepository;

    @Transactional(readOnly = true)
    public List<Prescrizione> findAll() {
        return prescrizioneRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Prescrizione> findByStabilimento(Long stabilimentoId) {
        return prescrizioneRepository.findByStabilimentoId(stabilimentoId);
    }

    @Transactional(readOnly = true)
    public Optional<Prescrizione> findById(Long id) {
        return prescrizioneRepository.findById(id);
    }

    @Transactional
    public Prescrizione save(Prescrizione prescrizione) {
        return prescrizioneRepository.save(prescrizione);
    }

    @Transactional
    public void deleteById(Long id) {
        prescrizioneRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Prescrizione> findByStato(Prescrizione.StatoPrescrizione stato) {
        return prescrizioneRepository.findByStato(stato);
    }

    // ─── DTO Methods ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PrescrizioneDTO> findAllAsDTOs() {
        return findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescrizioneDTO> findByStabilimentoAsDTOs(Long stabilimentoId) {
        return findByStabilimento(stabilimentoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PrescrizioneDTO> findByIdAsDTO(Long id) {
        return findById(id).map(this::toDTO);
    }

    @Transactional
    public PrescrizioneDTO saveAsDTO(Prescrizione prescrizione) {
        Prescrizione saved = save(prescrizione);
        return toDTO(saved);
    }

    // ─── Private DTO Conversion ────────────────────────────────────────

    private PrescrizioneDTO toDTO(Prescrizione prescrizione) {
        return PrescrizioneDTO.builder()
                .id(prescrizione.getId())
                .stabilimentoId(prescrizione.getStabilimento().getId())
                .stabilimentoNome(prescrizione.getStabilimento().getNome())
                .codice(prescrizione.getCodice())
                .descrizione(prescrizione.getDescrizione())
                .matriceAmbientale(prescrizione.getMatriceAmbientale())
                .stato(prescrizione.getStato())
                .dataEmissione(prescrizione.getDataEmissione())
                .dataScadenza(prescrizione.getDataScadenza())
                .enteEmittente(prescrizione.getEnteEmittente())
                .riferimentoNormativo(prescrizione.getRiferimentoNormativo())
                .priorita(prescrizione.getPriorita())
                .note(prescrizione.getNote())
                .dataChiusura(prescrizione.getDataChiusura())
                .createdAt(prescrizione.getCreatedAt())
                .updatedAt(prescrizione.getUpdatedAt())
                .build();
    }
}
