package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.CreateStabilimentoRequest;
import it.grandimolini.aia.dto.StabilimentoDTO;
import it.grandimolini.aia.dto.UpdateStabilimentoRequest;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.StabilimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
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

    @Transactional(readOnly = true)
    public List<StabilimentoDTO> findByIdsAsDTOs(Collection<Long> ids) {
        return stabilimentoRepository.findAllById(ids).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StabilimentoDTO> findByIdsAttiviAsDTOs(Collection<Long> ids) {
        return stabilimentoRepository.findAllById(ids).stream()
                .filter(Stabilimento::getAttivo)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return stabilimentoRepository.existsById(id);
    }

    @Transactional
    public StabilimentoDTO createFromRequest(CreateStabilimentoRequest request) {
        Stabilimento stabilimento = new Stabilimento();
        stabilimento.setNome(request.getNome());
        stabilimento.setCitta(request.getCitta());
        stabilimento.setIndirizzo(request.getIndirizzo());
        stabilimento.setNumeroAIA(request.getNumeroAIA());
        stabilimento.setDataRilascioAIA(request.getDataRilascioAIA());
        stabilimento.setDataScadenzaAIA(request.getDataScadenzaAIA());
        stabilimento.setEnteCompetente(request.getEnteCompetente());
        stabilimento.setResponsabileAmbientale(request.getResponsabileAmbientale());
        stabilimento.setEmail(request.getEmail());
        stabilimento.setTelefono(request.getTelefono());
        stabilimento.setAttivo(request.getAttivo() != null ? request.getAttivo() : true);
        return toDTO(stabilimentoRepository.save(stabilimento));
    }

    @Transactional
    public StabilimentoDTO updateFromRequest(Long id, UpdateStabilimentoRequest request) {
        Stabilimento stabilimento = stabilimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", id));
        if (request.getNome() != null)                 stabilimento.setNome(request.getNome());
        if (request.getCitta() != null)                stabilimento.setCitta(request.getCitta());
        if (request.getIndirizzo() != null)            stabilimento.setIndirizzo(request.getIndirizzo());
        if (request.getNumeroAIA() != null)            stabilimento.setNumeroAIA(request.getNumeroAIA());
        if (request.getDataRilascioAIA() != null)      stabilimento.setDataRilascioAIA(request.getDataRilascioAIA());
        if (request.getDataScadenzaAIA() != null)      stabilimento.setDataScadenzaAIA(request.getDataScadenzaAIA());
        if (request.getEnteCompetente() != null)       stabilimento.setEnteCompetente(request.getEnteCompetente());
        if (request.getResponsabileAmbientale() != null) stabilimento.setResponsabileAmbientale(request.getResponsabileAmbientale());
        if (request.getEmail() != null)                stabilimento.setEmail(request.getEmail());
        if (request.getTelefono() != null)             stabilimento.setTelefono(request.getTelefono());
        if (request.getAttivo() != null)               stabilimento.setAttivo(request.getAttivo());
        return toDTO(stabilimentoRepository.save(stabilimento));
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
