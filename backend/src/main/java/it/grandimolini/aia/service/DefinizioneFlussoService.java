package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.DefinizioneFlussoDTO;
import it.grandimolini.aia.exception.BadRequestException;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.DefinizioneFlusso;
import it.grandimolini.aia.repository.DefinizioneFlussoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefinizioneFlussoService {

    private final DefinizioneFlussoRepository repository;

    // ────── READ methods ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DefinizioneFlussoDTO> findAttiviAsDTOs() {
        return repository.findByAttivaTrue().stream()
                .map(DefinizioneFlussoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DefinizioneFlussoDTO> findAllAsDTOs() {
        return repository.findAll().stream()
                .map(DefinizioneFlussoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DefinizioneFlussoDTO> findByIdAsDTO(Long id) {
        return repository.findById(id).map(DefinizioneFlussoDTO::fromEntity);
    }

    /**
     * Internal method used by EstrazioneController and others for BPMN operations.
     * Returns the entity (not a DTO) — entities stay in session within @Transactional context.
     */
    @Transactional(readOnly = true)
    public Optional<DefinizioneFlusso> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Returns all active entities — used by EstrazioneController.
     * Does not use @Transactional since entities stay in session within their original context.
     */
    public List<DefinizioneFlusso> findAttiviEntities() {
        return repository.findByAttivaTrue();
    }

    // ────── WRITE methods ──────────────────────────────────────────────────────

    @Transactional
    public DefinizioneFlussoDTO creaFromRequest(DefinizioneFlussoDTO.SaveRequest req, String creatoDa) {
        DefinizioneFlusso df = new DefinizioneFlusso();
        df.setNome(req.getNome().trim());
        df.setDescrizione(req.getDescrizione());
        df.setBpmnXml(req.getBpmnXml());
        df.setVersione(1);
        df.setAttiva(true);
        df.setCreatoDa(creatoDa);

        return DefinizioneFlussoDTO.fromEntity(repository.save(df));
    }

    @Transactional
    public DefinizioneFlussoDTO aggiornaFromRequest(Long id, DefinizioneFlussoDTO.SaveRequest req) {
        DefinizioneFlusso df = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinizioneFlusso", "id", id));

        if (Boolean.TRUE.equals(df.getSistema())) {
            throw new BadRequestException("I workflow di sistema non sono modificabili.");
        }

        if (req.getNome() != null) {
            df.setNome(req.getNome().trim());
        }
        if (req.getDescrizione() != null) {
            df.setDescrizione(req.getDescrizione());
        }

        if (req.getBpmnXml() != null && !req.getBpmnXml().equals(df.getBpmnXml())) {
            df.setBpmnXml(req.getBpmnXml());
            df.setVersione(df.getVersione() + 1);
        }

        return DefinizioneFlussoDTO.fromEntity(repository.save(df));
    }

    @Transactional
    public void disattiva(Long id) {
        DefinizioneFlusso df = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinizioneFlusso", "id", id));

        if (Boolean.TRUE.equals(df.getSistema())) {
            throw new BadRequestException("I workflow di sistema non possono essere eliminati.");
        }

        df.setAttiva(false);
        repository.save(df);
    }
}
