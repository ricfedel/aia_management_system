package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.ScadenzaDTO;
import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.repository.ScadenzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScadenzaService {

    @Autowired
    private ScadenzaRepository scadenzaRepository;

    @Transactional(readOnly = true)
    public List<Scadenza> findAll() {
        return scadenzaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Scadenza> findByStabilimento(Long stabilimentoId) {
        return scadenzaRepository.findByStabilimentoId(stabilimentoId);
    }

    @Transactional(readOnly = true)
    public List<Scadenza> findScadenzeImminenti(int giorni) {
        LocalDate dataLimite = LocalDate.now().plusDays(giorni);
        return scadenzaRepository.findScadenzeImminenti(dataLimite);
    }

    @Transactional(readOnly = true)
    public List<Scadenza> findScadenzeProssimi30Giorni() {
        LocalDate oggi = LocalDate.now();
        LocalDate tra30Giorni = oggi.plusDays(30);
        return scadenzaRepository.findScadenzeInPeriodo(oggi, tra30Giorni);
    }

    @Transactional(readOnly = true)
    public Optional<Scadenza> findById(Long id) {
        return scadenzaRepository.findById(id);
    }

    @Transactional
    public Scadenza save(Scadenza scadenza) {
        return scadenzaRepository.save(scadenza);
    }

    @Transactional
    public void deleteById(Long id) {
        scadenzaRepository.deleteById(id);
    }

    // ─── DTO Methods ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ScadenzaDTO> findAllAsDTOs() {
        return findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScadenzaDTO> findByStabilimentoAsDTOs(Long stabilimentoId) {
        return findByStabilimento(stabilimentoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScadenzaDTO> findScadenzeProssimi30GiorniAsDTOs() {
        return findScadenzeProssimi30Giorni().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScadenzaDTO> findScadenzeImminentiAsDTOs(int giorni) {
        return findScadenzeImminenti(giorni).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ScadenzaDTO> findByIdAsDTO(Long id) {
        return findById(id).map(this::toDTO);
    }

    @Transactional
    public ScadenzaDTO saveAsDTO(Scadenza scadenza) {
        Scadenza saved = save(scadenza);
        return toDTO(saved);
    }

    // ─── Private DTO Conversion ────────────────────────────────────────

    private ScadenzaDTO toDTO(Scadenza scadenza) {
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
}
