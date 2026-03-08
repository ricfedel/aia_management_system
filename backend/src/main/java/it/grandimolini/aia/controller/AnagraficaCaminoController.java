package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.AnagraficaCaminoDTO;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.AnagraficaCamino;
import it.grandimolini.aia.model.AnagraficaCamino.FaseProcesso;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.AnagraficaCaminoRepository;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import it.grandimolini.aia.service.StabilimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller per l'anagrafica tecnica dei camini.
 * Espone i dati della "Tabella A – Quadro riassuntivo delle emissioni in atmosfera"
 * del provvedimento AIA di ciascun stabilimento.
 */
@RestController
@RequestMapping("/api/anagrafica-camini")
public class AnagraficaCaminoController {

    @Autowired private AnagraficaCaminoRepository repo;
    @Autowired private StabilimentoService stabilimentoService;
    @Autowired private StabilimentoAccessChecker accessChecker;

    // ─── GET tutti (admin vede tutto, altri filtrano per stabilimento) ────

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AnagraficaCaminoDTO>> getAll(
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) FaseProcesso faseProcesso,
            @RequestParam(required = false) Boolean attivo) {

        List<AnagraficaCamino> list;

        if (stabilimentoId != null) {
            if (faseProcesso != null) {
                list = repo.findByStabilimentoIdAndFaseProcessoOrderBySiglaAsc(stabilimentoId, faseProcesso);
            } else if (attivo != null) {
                list = repo.findByStabilimentoIdAndAttivoOrderBySiglaAsc(stabilimentoId, attivo);
            } else {
                list = repo.findByStabilimentoIdOrderBySiglaAsc(stabilimentoId);
            }
        } else {
            list = repo.findAll();
        }

        // filtra per accesso stabilimento
        if (!accessChecker.isAdmin()) {
            list = list.stream()
                    .filter(c -> accessChecker.hasAccessToStabilimento(c.getStabilimento().getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(list.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    // ─── GET singolo ──────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnagraficaCaminoDTO> getById(@PathVariable Long id) {
        AnagraficaCamino c = findAndCheck(id);
        return ResponseEntity.ok(toDTO(c));
    }

    // ─── GET per sigla dentro uno stabilimento ────────────────────────────

    @GetMapping("/stabilimento/{stabilimentoId}/sigla/{sigla}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<AnagraficaCaminoDTO> getBySigla(
            @PathVariable Long stabilimentoId, @PathVariable String sigla) {
        AnagraficaCamino c = repo.findByStabilimentoIdAndSigla(stabilimentoId, sigla)
                .orElseThrow(() -> new ResourceNotFoundException("AnagraficaCamino", "sigla", sigla));
        return ResponseEntity.ok(toDTO(c));
    }

    // ─── CREATE ───────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<AnagraficaCaminoDTO> create(@RequestBody AnagraficaCaminoDTO dto) {
        AnagraficaCamino c = fromDTO(dto, new AnagraficaCamino());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(repo.save(c)));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<AnagraficaCaminoDTO> update(
            @PathVariable Long id, @RequestBody AnagraficaCaminoDTO dto) {
        AnagraficaCamino c = findAndCheck(id);
        fromDTO(dto, c);
        return ResponseEntity.ok(toDTO(repo.save(c)));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        findAndCheck(id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── helpers ──────────────────────────────────────────────────────────

    private AnagraficaCamino findAndCheck(Long id) {
        AnagraficaCamino c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnagraficaCamino", "id", id));
        if (!accessChecker.isAdmin() &&
            !accessChecker.hasAccessToStabilimento(c.getStabilimento().getId())) {
            throw new ResourceNotFoundException("AnagraficaCamino", "id", id);
        }
        return c;
    }

    private AnagraficaCaminoDTO toDTO(AnagraficaCamino c) {
        return AnagraficaCaminoDTO.builder()
                .id(c.getId())
                .stabilimentoId(c.getStabilimento().getId())
                .stabilimentoNome(c.getStabilimento().getNome())
                .sigla(c.getSigla())
                .faseProcesso(c.getFaseProcesso())
                .origine(c.getOrigine())
                .portataNomc3h(c.getPortataNomc3h())
                .sezioneM2(c.getSezioneM2())
                .velocitaMs(c.getVelocitaMs())
                .temperaturaC(c.getTemperaturaC())
                .temperaturaAmbiente(c.getTemperaturaAmbiente())
                .altezzaM(c.getAltezzaM())
                .durataHGiorno(c.getDurataHGiorno())
                .durataGAnno(c.getDurataGAnno())
                .impiantoAbbattimento(c.getImpiantoAbbattimento())
                .note(c.getNote())
                .attivo(c.getAttivo())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private AnagraficaCamino fromDTO(AnagraficaCaminoDTO dto, AnagraficaCamino c) {
        if (dto.getStabilimentoId() != null) {
            Stabilimento st = stabilimentoService.findById(dto.getStabilimentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", dto.getStabilimentoId()));
            c.setStabilimento(st);
        }
        if (dto.getSigla() != null)                 c.setSigla(dto.getSigla().toUpperCase());
        if (dto.getFaseProcesso() != null)           c.setFaseProcesso(dto.getFaseProcesso());
        if (dto.getOrigine() != null)                c.setOrigine(dto.getOrigine());
        if (dto.getPortataNomc3h() != null)          c.setPortataNomc3h(dto.getPortataNomc3h());
        if (dto.getSezioneM2() != null)              c.setSezioneM2(dto.getSezioneM2());
        if (dto.getVelocitaMs() != null)             c.setVelocitaMs(dto.getVelocitaMs());
        // temperatura: null = ambiente
        c.setTemperaturaC(dto.getTemperaturaC());
        c.setTemperaturaAmbiente(dto.getTemperaturaAmbiente() != null ? dto.getTemperaturaAmbiente() : dto.getTemperaturaC() == null);
        if (dto.getAltezzaM() != null)               c.setAltezzaM(dto.getAltezzaM());
        if (dto.getDurataHGiorno() != null)          c.setDurataHGiorno(dto.getDurataHGiorno());
        if (dto.getDurataGAnno() != null)            c.setDurataGAnno(dto.getDurataGAnno());
        if (dto.getImpiantoAbbattimento() != null)   c.setImpiantoAbbattimento(dto.getImpiantoAbbattimento());
        if (dto.getNote() != null)                   c.setNote(dto.getNote());
        c.setAttivo(dto.getAttivo() != null ? dto.getAttivo() : true);
        return c;
    }
}
