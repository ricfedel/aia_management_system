package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.RilevazioneMisuraDTO;
import it.grandimolini.aia.model.ParametroMonitoraggio;
import it.grandimolini.aia.model.RilevazioneMisura;
import it.grandimolini.aia.repository.ParametroMonitoraggioRepository;
import it.grandimolini.aia.repository.RilevazioneMisuraRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conformita")
public class ConformitaController {

    private final RilevazioneMisuraRepository rilevRepo;
    private final ParametroMonitoraggioRepository parametroRepo;

    public ConformitaController(RilevazioneMisuraRepository rilevRepo,
                                ParametroMonitoraggioRepository parametroRepo) {
        this.rilevRepo   = rilevRepo;
        this.parametroRepo = parametroRepo;
    }

    // ─── Rilevazioni ──────────────────────────────────────────────────
    @GetMapping("/rilevazioni")
    public List<RilevazioneMisuraDTO> getRilevazioni(
            @RequestParam(required = false) Long parametroId,
            @RequestParam(required = false) Long monitoraggioId,
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<RilevazioneMisura> list;
        if (parametroId != null && from != null && to != null) {
            list = rilevRepo.findByParametroMonitoraggioIdAndDataCampionamentoBetweenOrderByDataCampionamentoDesc(parametroId, from, to);
        } else if (parametroId != null) {
            list = rilevRepo.findByParametroMonitoraggioIdOrderByDataCampionamentoDesc(parametroId);
        } else if (monitoraggioId != null) {
            list = rilevRepo.findByMonitoraggioId(monitoraggioId);
        } else if (stabilimentoId != null && from != null && to != null) {
            list = rilevRepo.findByStabilimentoAndPeriodo(stabilimentoId, from, to);
        } else {
            list = rilevRepo.findAll();
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/rilevazioni/{id}")
    public ResponseEntity<RilevazioneMisuraDTO> getById(@PathVariable Long id) {
        return rilevRepo.findById(id)
                .map(r -> ResponseEntity.ok(toDTO(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rilevazioni")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE','OPERATORE')")
    public ResponseEntity<RilevazioneMisuraDTO> create(@RequestBody RilevazioneMisuraDTO dto) {
        ParametroMonitoraggio param = parametroRepo.findById(dto.getParametroMonitoraggioId())
                .orElseThrow(() -> new RuntimeException("Parametro non trovato"));

        RilevazioneMisura r = new RilevazioneMisura();
        r.setParametroMonitoraggio(param);
        r.setDataCampionamento(dto.getDataCampionamento());
        r.setValoreMisurato(dto.getValoreMisurato());
        r.setUnitaMisura(dto.getUnitaMisura() != null ? dto.getUnitaMisura() : param.getUnitaMisura());
        r.setRapportoProva(dto.getRapportoProva());
        r.setLaboratorio(dto.getLaboratorio());
        r.setNote(dto.getNote());
        // calcolaConformita() called in @PrePersist
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(rilevRepo.save(r)));
    }

    @PutMapping("/rilevazioni/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<RilevazioneMisuraDTO> update(@PathVariable Long id,
                                                       @RequestBody RilevazioneMisuraDTO dto) {
        return rilevRepo.findById(id).map(r -> {
            if (dto.getDataCampionamento() != null) r.setDataCampionamento(dto.getDataCampionamento());
            if (dto.getValoreMisurato() != null)    r.setValoreMisurato(dto.getValoreMisurato());
            if (dto.getUnitaMisura() != null)       r.setUnitaMisura(dto.getUnitaMisura());
            if (dto.getRapportoProva() != null)     r.setRapportoProva(dto.getRapportoProva());
            if (dto.getLaboratorio() != null)       r.setLaboratorio(dto.getLaboratorio());
            if (dto.getNote() != null)              r.setNote(dto.getNote());
            r.calcolaConformita();
            return ResponseEntity.ok(toDTO(rilevRepo.save(r)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/rilevazioni/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!rilevRepo.existsById(id)) return ResponseEntity.notFound().build();
        rilevRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Dashboard: ultima conformità per ogni parametro ──────────────
    @GetMapping("/dashboard/{stabilimentoId}")
    public List<RilevazioneMisuraDTO> getDashboard(@PathVariable Long stabilimentoId) {
        return rilevRepo.findUltimeRilevazioniByStabilimento(stabilimentoId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/dashboard/monitoraggio/{monitoraggioId}")
    public List<RilevazioneMisuraDTO> getDashboardMonitoraggio(@PathVariable Long monitoraggioId) {
        return rilevRepo.findUltimeRilevazioniByMonitoraggio(monitoraggioId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Rilevazioni non conformi per lo stabilimento */
    @GetMapping("/non-conformi/{stabilimentoId}")
    public List<RilevazioneMisuraDTO> getNonConformi(@PathVariable Long stabilimentoId) {
        return rilevRepo.findNonConformiByStabilimento(stabilimentoId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Riepilogo conteggi CONFORME/ATTENZIONE/NON_CONFORME per stabilimento */
    @GetMapping("/riepilogo/{stabilimentoId}")
    public Map<String, Long> getRiepilogo(@PathVariable Long stabilimentoId) {
        List<RilevazioneMisura> ultime = rilevRepo.findUltimeRilevazioniByStabilimento(stabilimentoId);
        return ultime.stream()
                .filter(r -> r.getStatoConformita() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getStatoConformita().name(),
                        Collectors.counting()
                ));
    }

    // ─── Mapping ──────────────────────────────────────────────────────
    private RilevazioneMisuraDTO toDTO(RilevazioneMisura r) {
        RilevazioneMisuraDTO dto = new RilevazioneMisuraDTO();
        dto.setId(r.getId());

        ParametroMonitoraggio param = r.getParametroMonitoraggio();
        dto.setParametroMonitoraggioId(param.getId());
        dto.setParametroNome(param.getNome());
        dto.setParametroCodice(param.getCodice());
        dto.setParametroUnitaMisura(param.getUnitaMisura());
        dto.setParametroLimiteValore(param.getLimiteValore());
        dto.setParametroLimiteRiferimento(param.getLimiteRiferimento());

        var mon = param.getMonitoraggio();
        dto.setMonitoraggioId(mon.getId());
        dto.setMonitoraggioCodice(mon.getCodice());
        dto.setMonitoraggioDescrizione(mon.getDescrizione());
        dto.setMonitoraggioTipo(mon.getTipoMonitoraggio() != null ? mon.getTipoMonitoraggio().name() : null);

        var stab = mon.getStabilimento();
        dto.setStabilimentoId(stab.getId());
        dto.setStabilimentoNome(stab.getNome());

        dto.setDataCampionamento(r.getDataCampionamento());
        dto.setValoreMisurato(r.getValoreMisurato());
        dto.setUnitaMisura(r.getUnitaMisura());
        dto.setStatoConformita(r.getStatoConformita());
        dto.setRapportoProva(r.getRapportoProva());
        dto.setLaboratorio(r.getLaboratorio());
        dto.setNote(r.getNote());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
