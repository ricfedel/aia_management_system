package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.RegistroMensileDTO;
import it.grandimolini.aia.dto.VoceProduzioneDTO;
import it.grandimolini.aia.service.ProduzioneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produzione")
public class ProduzioneController {

    private final ProduzioneService produzioneService;

    public ProduzioneController(ProduzioneService produzioneService) {
        this.produzioneService = produzioneService;
    }

    // ─── Anni disponibili ─────────────────────────────────────────────
    @GetMapping("/anni")
    public List<Integer> getAnni() {
        return produzioneService.getAnni();
    }

    @GetMapping("/anni/stabilimento/{stabilimentoId}")
    public List<Integer> getAnniByStabilimento(@PathVariable Long stabilimentoId) {
        return produzioneService.getAnniByStabilimento(stabilimentoId);
    }

    // ─── Registri ─────────────────────────────────────────────────────
    @GetMapping
    public List<RegistroMensileDTO> getAll(
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) Integer anno) {
        return produzioneService.getAll(stabilimentoId, anno);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroMensileDTO> getById(@PathVariable Long id) {
        return produzioneService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stabilimento/{stabilimentoId}/anno/{anno}/mese/{mese}")
    public ResponseEntity<RegistroMensileDTO> getByStabilimentoAnnoMese(
            @PathVariable Long stabilimentoId,
            @PathVariable Integer anno,
            @PathVariable Integer mese) {
        return produzioneService.findByStabilimentoAnnoMese(stabilimentoId, anno, mese)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<RegistroMensileDTO> create(@RequestBody RegistroMensileDTO dto) {
        try {
            RegistroMensileDTO created = produzioneService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException e) {
            if ("CONFLICT".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<RegistroMensileDTO> update(@PathVariable Long id, @RequestBody RegistroMensileDTO dto) {
        return produzioneService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!produzioneService.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // ─── Voci ─────────────────────────────────────────────────────────
    @GetMapping("/{registroId}/voci")
    public List<VoceProduzioneDTO> getVoci(@PathVariable Long registroId) {
        return produzioneService.getVoci(registroId);
    }

    @PostMapping("/{registroId}/voci")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<VoceProduzioneDTO> addVoce(@PathVariable Long registroId,
                                                      @RequestBody VoceProduzioneDTO dto) {
        VoceProduzioneDTO voce = produzioneService.addVoce(registroId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(voce);
    }

    @PutMapping("/{registroId}/voci/{voceId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<VoceProduzioneDTO> updateVoce(@PathVariable Long registroId,
                                                         @PathVariable Long voceId,
                                                         @RequestBody VoceProduzioneDTO dto) {
        return produzioneService.updateVoce(registroId, voceId, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{registroId}/voci/{voceId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<Void> deleteVoce(@PathVariable Long registroId, @PathVariable Long voceId) {
        if (!produzioneService.deleteVoce(voceId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // ─── Batch: salva tutte le voci di un registro ────────────────────
    @PutMapping("/{registroId}/voci/batch")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<RegistroMensileDTO> saveVociBatch(@PathVariable Long registroId,
                                                             @RequestBody List<VoceProduzioneDTO> vociDTO) {
        RegistroMensileDTO result = produzioneService.saveVociBatch(registroId, vociDTO);
        return ResponseEntity.ok(result);
    }
}
