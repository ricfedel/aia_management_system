package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.AltraAutorizzazioneDTO;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.AltraAutorizzazione;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.AltraAutorizzazioneRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AltraAutorizzazioneService {

    @Autowired
    private AltraAutorizzazioneRepository repository;

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    @Transactional(readOnly = true)
    public List<AltraAutorizzazioneDTO> findByStabilimento(Long stabilimentoId) {
        return repository.findByStabilimentoId(stabilimentoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AltraAutorizzazioneDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AltraAutorizzazioneDTO save(AltraAutorizzazioneDTO dto) {
        Stabilimento stabilimento = stabilimentoRepository.findById(dto.getStabilimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", dto.getStabilimentoId()));

        AltraAutorizzazione entity = new AltraAutorizzazione();
        entity.setStabilimento(stabilimento);
        entity.setTipo(dto.getTipo());
        entity.setNumeroAutorizzazione(dto.getNumeroAutorizzazione());
        entity.setEnteCompetente(dto.getEnteCompetente());
        entity.setDataRilascio(dto.getDataRilascio());
        entity.setDataScadenza(dto.getDataScadenza());
        entity.setDescrizione(dto.getDescrizione());

        return toDTO(repository.save(entity));
    }

    @Transactional
    public AltraAutorizzazioneDTO update(Long id, AltraAutorizzazioneDTO dto) {
        AltraAutorizzazione entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AltraAutorizzazione", "id", id));

        if (dto.getTipo() != null) entity.setTipo(dto.getTipo());
        if (dto.getNumeroAutorizzazione() != null) entity.setNumeroAutorizzazione(dto.getNumeroAutorizzazione());
        if (dto.getEnteCompetente() != null) entity.setEnteCompetente(dto.getEnteCompetente());
        if (dto.getDataRilascio() != null) entity.setDataRilascio(dto.getDataRilascio());
        if (dto.getDataScadenza() != null) entity.setDataScadenza(dto.getDataScadenza());
        if (dto.getDescrizione() != null) entity.setDescrizione(dto.getDescrizione());

        return toDTO(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private AltraAutorizzazioneDTO toDTO(AltraAutorizzazione e) {
        return AltraAutorizzazioneDTO.builder()
                .id(e.getId())
                .stabilimentoId(e.getStabilimento().getId())
                .stabilimentoNome(e.getStabilimento().getNome())
                .tipo(e.getTipo())
                .numeroAutorizzazione(e.getNumeroAutorizzazione())
                .enteCompetente(e.getEnteCompetente())
                .dataRilascio(e.getDataRilascio())
                .dataScadenza(e.getDataScadenza())
                .descrizione(e.getDescrizione())
                .build();
    }
}
