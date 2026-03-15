package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.StabilimentoDTO;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.StabilimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StabilimentoService {

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    @Transactional(readOnly = true)
    public List<Stabilimento> findAll() {
        return stabilimentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Stabilimento> findAllAttivi() {
        return stabilimentoRepository.findByAttivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Stabilimento> findById(Long id) {
        return stabilimentoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Stabilimento> findBySigla(String sigla) {
        return stabilimentoRepository.findBySiglaIgnoreCase(sigla);
    }

    @Transactional
    public Stabilimento save(Stabilimento stabilimento) {
        return stabilimentoRepository.save(stabilimento);
    }

    @Transactional
    public void deleteById(Long id) {
        stabilimentoRepository.deleteById(id);
    }

    // ─── DTO Methods ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StabilimentoDTO> findAllAsDTOs() {
        return findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StabilimentoDTO> findAllAttiviAsDTOs() {
        return findAllAttivi().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<StabilimentoDTO> findByIdAsDTO(Long id) {
        return findById(id).map(this::toDTO);
    }

    @Transactional
    public StabilimentoDTO saveAsDTO(Stabilimento stabilimento) {
        Stabilimento saved = save(stabilimento);
        return toDTO(saved);
    }

    // ─── Private DTO Conversion ────────────────────────────────────────

    private StabilimentoDTO toDTO(Stabilimento stabilimento) {
        return StabilimentoDTO.builder()
                .id(stabilimento.getId())
                .nome(stabilimento.getNome())
                .citta(stabilimento.getCitta())
                .indirizzo(stabilimento.getIndirizzo())
                .numeroAIA(stabilimento.getNumeroAIA())
                .dataRilascioAIA(stabilimento.getDataRilascioAIA())
                .dataScadenzaAIA(stabilimento.getDataScadenzaAIA())
                .enteCompetente(stabilimento.getEnteCompetente())
                .responsabileAmbientale(stabilimento.getResponsabileAmbientale())
                .email(stabilimento.getEmail())
                .telefono(stabilimento.getTelefono())
                .attivo(stabilimento.getAttivo())
                .build();
    }
}
