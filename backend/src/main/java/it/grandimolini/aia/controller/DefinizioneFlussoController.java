package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.DefinizioneFlussoDTO;
import it.grandimolini.aia.exception.BadRequestException;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.DefinizioneFlusso;
import it.grandimolini.aia.repository.DefinizioneFlussoRepository;
import it.grandimolini.aia.service.BpmnParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD per le DefinizioniFlusso BPMN definite dagli utenti.
 *
 * GET    /api/definizioni-flusso             lista flussi attivi
 * GET    /api/definizioni-flusso/tutti       lista tutti (inclusi inattivi)
 * GET    /api/definizioni-flusso/{id}        singola definizione
 * POST   /api/definizioni-flusso             crea nuova definizione
 * PUT    /api/definizioni-flusso/{id}        aggiorna
 * DELETE /api/definizioni-flusso/{id}        disattiva (soft-delete)
 * POST   /api/definizioni-flusso/{id}/preview parse e restituisce gli step
 * POST   /api/definizioni-flusso/preview     parse XML inline senza salvare
 */
@RestController
@RequestMapping("/api/definizioni-flusso")
public class DefinizioneFlussoController {

    @Autowired private DefinizioneFlussoRepository repository;
    @Autowired private BpmnParserService bpmnParser;

    // ─── GET: lista flussi attivi ────────────────────────────────────────────

    @GetMapping
    public List<DefinizioneFlussoDTO> getAttivi() {
        return repository.findByAttivaTrue().stream()
                .map(DefinizioneFlussoDTO::fromEntity)
                .toList();
    }

    @GetMapping("/tutti")
    public List<DefinizioneFlussoDTO> getTutti() {
        return repository.findAll().stream()
                .map(DefinizioneFlussoDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public DefinizioneFlussoDTO getById(@PathVariable Long id) {
        DefinizioneFlusso df = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinizioneFlusso", "id", id));
        return DefinizioneFlussoDTO.fromEntity(df);
    }

    // ─── POST: crea nuova definizione ────────────────────────────────────────

    @PostMapping
    public DefinizioneFlussoDTO crea(@RequestBody DefinizioneFlussoDTO.SaveRequest req,
                                     Authentication auth) {
        validaRequest(req);
        parseOThrow(req.getBpmnXml()); // verifica che il BPMN sia valido prima di salvare

        DefinizioneFlusso df = new DefinizioneFlusso();
        df.setNome(req.getNome().trim());
        df.setDescrizione(req.getDescrizione());
        df.setBpmnXml(req.getBpmnXml());
        df.setVersione(1);
        df.setAttiva(true);
        df.setCreatoDa(auth != null ? auth.getName() : "sistema");

        return DefinizioneFlussoDTO.fromEntity(repository.save(df));
    }

    // ─── PUT: aggiorna definizione esistente ─────────────────────────────────

    @PutMapping("/{id}")
    public DefinizioneFlussoDTO aggiorna(@PathVariable Long id,
                                          @RequestBody DefinizioneFlussoDTO.SaveRequest req,
                                          Authentication auth) {
        DefinizioneFlusso df = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinizioneFlusso", "id", id));

        if (Boolean.TRUE.equals(df.getSistema()))
            throw new BadRequestException("I workflow di sistema non sono modificabili.");

        if (req.getNome() != null)        df.setNome(req.getNome().trim());
        if (req.getDescrizione() != null) df.setDescrizione(req.getDescrizione());

        if (req.getBpmnXml() != null && !req.getBpmnXml().equals(df.getBpmnXml())) {
            parseOThrow(req.getBpmnXml()); // valida il nuovo XML
            df.setBpmnXml(req.getBpmnXml());
            df.setVersione(df.getVersione() + 1);
        }

        return DefinizioneFlussoDTO.fromEntity(repository.save(df));
    }

    // ─── DELETE: soft-delete (disattiva) ─────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disattiva(@PathVariable Long id) {
        DefinizioneFlusso df = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinizioneFlusso", "id", id));

        if (Boolean.TRUE.equals(df.getSistema()))
            throw new BadRequestException("I workflow di sistema non possono essere eliminati.");

        df.setAttiva(false);
        repository.save(df);
        return ResponseEntity.noContent().build();
    }

    // ─── POST /preview: parse e mostra step senza salvare ─────────────────

    @PostMapping("/preview")
    public List<DefinizioneFlussoDTO.StepPreview> previewXml(
            @RequestBody DefinizioneFlussoDTO.SaveRequest req) {
        if (req.getBpmnXml() == null || req.getBpmnXml().isBlank()) {
            throw new BadRequestException("bpmnXml è obbligatorio");
        }
        return bpmnParser.toPreview(parseOThrow(req.getBpmnXml()));
    }

    @PostMapping("/{id}/preview")
    public List<DefinizioneFlussoDTO.StepPreview> previewById(@PathVariable Long id) {
        DefinizioneFlusso df = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinizioneFlusso", "id", id));
        return bpmnParser.toPreview(parseOThrow(df.getBpmnXml()));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void validaRequest(DefinizioneFlussoDTO.SaveRequest req) {
        if (req.getNome() == null || req.getNome().isBlank()) {
            throw new BadRequestException("Il nome del flusso è obbligatorio");
        }
        if (req.getBpmnXml() == null || req.getBpmnXml().isBlank()) {
            throw new BadRequestException("Il BPMN XML è obbligatorio");
        }
    }

    private List<BpmnParserService.BpmnStep> parseOThrow(String xml) {
        try {
            return bpmnParser.parseSteps(xml);
        } catch (Exception e) {
            throw new BadRequestException("BPMN non valido: " + e.getMessage());
        }
    }
}
