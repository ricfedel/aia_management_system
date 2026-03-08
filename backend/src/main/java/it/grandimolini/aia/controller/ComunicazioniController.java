package it.grandimolini.aia.controller;

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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comunicazioni")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComunicazioniController {

    private final ComunicazioneEnteRepository comunicazioniRepo;
    private final StabilimentoRepository      stabilimentoRepo;
    private final PrescrizioneRepository      prescrizioneRepo;

    // ── GET filtrato ───────────────────────────────────────────────────────
    @GetMapping
    public List<ComunicazioneEnteDTO> getAll(
            @RequestParam Long stabilimentoId,
            @RequestParam(required = false) String stato,
            @RequestParam(required = false) String ente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        StatoComunicazione statoEnum = stato != null ? StatoComunicazione.valueOf(stato) : null;
        EnteEsterno        enteEnum  = ente  != null ? EnteEsterno.valueOf(ente)          : null;

        return comunicazioniRepo
                .findFiltered(stabilimentoId, statoEnum, enteEnum, from, to)
                .stream().map(this::toDTO).toList();
    }

    // ── GET singola ────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ComunicazioneEnteDTO> getById(@PathVariable Long id) {
        return comunicazioniRepo.findById(id)
                .map(c -> ResponseEntity.ok(toDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET in attesa di riscontro ─────────────────────────────────────────
    @GetMapping("/in-attesa-riscontro/{stabilimentoId}")
    public List<ComunicazioneEnteDTO> getInAttesaRiscontro(
            @PathVariable Long stabilimentoId,
            @RequestParam(defaultValue = "30") int giorniSoglia) {

        LocalDate soglia = LocalDate.now().minusDays(giorniSoglia);
        return comunicazioniRepo.findInAttesaRiscontro(stabilimentoId, soglia)
                .stream().map(this::toDTO).toList();
    }

    // ── GET riepilogo per stato ────────────────────────────────────────────
    @GetMapping("/riepilogo/{stabilimentoId}")
    public Map<String, Long> getRiepilogo(@PathVariable Long stabilimentoId) {
        long bozze     = comunicazioniRepo.countByStabilimentoIdAndStato(stabilimentoId, StatoComunicazione.BOZZA);
        long inviate   = comunicazioniRepo.countByStabilimentoIdAndStato(stabilimentoId, StatoComunicazione.INVIATA);
        long consegn   = comunicazioniRepo.countByStabilimentoIdAndStato(stabilimentoId, StatoComunicazione.CONSEGNATA_PEC);
        long rispRic   = comunicazioniRepo.countByStabilimentoIdAndStato(stabilimentoId, StatoComunicazione.RISPOSTA_RICEVUTA);
        long archiviate= comunicazioniRepo.countByStabilimentoIdAndStato(stabilimentoId, StatoComunicazione.ARCHIVIATA);
        return Map.of(
                "BOZZA",            bozze,
                "INVIATA",          inviate,
                "CONSEGNATA_PEC",   consegn,
                "RISPOSTA_RICEVUTA",rispRic,
                "ARCHIVIATA",       archiviate
        );
    }

    // ── POST ───────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ComunicazioneEnteDTO> create(@RequestBody ComunicazioneEnteDTO dto) {
        Stabilimento stab = stabilimentoRepo.findById(dto.getStabilimentoId())
                .orElseThrow(() -> new RuntimeException("Stabilimento non trovato"));

        ComunicazioneEnte entity = fromDTO(dto, new ComunicazioneEnte());
        entity.setStabilimento(stab);

        if (dto.getPrescrizioneId() != null) {
            prescrizioneRepo.findById(dto.getPrescrizioneId())
                    .ifPresent(entity::setPrescrizione);
        }

        return ResponseEntity.ok(toDTO(comunicazioniRepo.save(entity)));
    }

    // ── PUT ────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ComunicazioneEnteDTO> update(
            @PathVariable Long id, @RequestBody ComunicazioneEnteDTO dto) {

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

            return ResponseEntity.ok(toDTO(comunicazioniRepo.save(entity)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── PATCH stato ────────────────────────────────────────────────────────
    @PatchMapping("/{id}/stato")
    public ResponseEntity<ComunicazioneEnteDTO> cambiaStato(
            @PathVariable Long id,
            @RequestParam String stato) {

        return comunicazioniRepo.findById(id).map(entity -> {
            entity.setStato(StatoComunicazione.valueOf(stato));
            return ResponseEntity.ok(toDTO(comunicazioniRepo.save(entity)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!comunicazioniRepo.existsById(id)) return ResponseEntity.notFound().build();
        comunicazioniRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mapping ─────────────────────────────────────────────────────────────
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
        if (dto.getTipo()  != null) entity.setTipo(dto.getTipo());
        if (dto.getStato() != null) entity.setStato(dto.getStato());
        if (dto.getEnte()  != null) entity.setEnte(dto.getEnte());
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
