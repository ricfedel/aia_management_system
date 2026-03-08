package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.CodiceRifiutoDTO;
import it.grandimolini.aia.dto.MovimentoRifiutoDTO;
import it.grandimolini.aia.model.CodiceRifiuto;
import it.grandimolini.aia.model.MovimentoRifiuto;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.CodiceRifiutoRepository;
import it.grandimolini.aia.repository.MovimentoRifiutoRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rifiuti")
public class RifiutiController {

    private final CodiceRifiutoRepository codiceRepo;
    private final MovimentoRifiutoRepository movimentoRepo;
    private final StabilimentoRepository stabilimentoRepo;

    public RifiutiController(CodiceRifiutoRepository codiceRepo,
                             MovimentoRifiutoRepository movimentoRepo,
                             StabilimentoRepository stabilimentoRepo) {
        this.codiceRepo       = codiceRepo;
        this.movimentoRepo    = movimentoRepo;
        this.stabilimentoRepo = stabilimentoRepo;
    }

    // ─── Codici CER (anagrafica) ──────────────────────────────────────
    @GetMapping("/codici")
    public List<CodiceRifiutoDTO> getCodici(
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) Boolean soloAttivi,
            @RequestParam(required = false) Boolean pericoloso) {

        List<CodiceRifiuto> list;
        if (stabilimentoId != null && Boolean.TRUE.equals(pericoloso)) {
            list = codiceRepo.findByStabilimentoIdAndPericolosoOrderByCodiceCerAsc(stabilimentoId, true);
        } else if (stabilimentoId != null && Boolean.FALSE.equals(pericoloso)) {
            list = codiceRepo.findByStabilimentoIdAndPericolosoOrderByCodiceCerAsc(stabilimentoId, false);
        } else if (stabilimentoId != null && Boolean.TRUE.equals(soloAttivi)) {
            list = codiceRepo.findByStabilimentoIdAndAttivoTrueOrderByCodiceCerAsc(stabilimentoId);
        } else if (stabilimentoId != null) {
            list = codiceRepo.findByStabilimentoIdOrderByCodiceCerAsc(stabilimentoId);
        } else {
            list = codiceRepo.findAll();
        }
        return list.stream().map(this::toCodiceDTO).collect(Collectors.toList());
    }

    @GetMapping("/codici/{id}")
    public ResponseEntity<CodiceRifiutoDTO> getCodice(@PathVariable Long id) {
        return codiceRepo.findById(id)
                .map(c -> ResponseEntity.ok(toCodiceDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/codici")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<CodiceRifiutoDTO> createCodice(@RequestBody CodiceRifiutoDTO dto) {
        Stabilimento stabilimento = stabilimentoRepo.findById(dto.getStabilimentoId())
                .orElseThrow(() -> new RuntimeException("Stabilimento non trovato"));

        CodiceRifiuto c = new CodiceRifiuto();
        c.setStabilimento(stabilimento);
        c.setCodiceCer(dto.getCodiceCer().trim());
        c.setDescrizione(dto.getDescrizione());
        c.setPericoloso(dto.getPericoloso() != null ? dto.getPericoloso() : false);
        c.setStatoFisico(dto.getStatoFisico());
        c.setUnitaMisura(dto.getUnitaMisura() != null ? dto.getUnitaMisura() : "t");
        c.setCodiceGestione(dto.getCodiceGestione());
        c.setDestinatarioAbituale(dto.getDestinatarioAbituale());
        c.setNote(dto.getNote());
        c.setAttivo(true);

        return ResponseEntity.status(HttpStatus.CREATED).body(toCodiceDTO(codiceRepo.save(c)));
    }

    @PutMapping("/codici/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<CodiceRifiutoDTO> updateCodice(@PathVariable Long id, @RequestBody CodiceRifiutoDTO dto) {
        return codiceRepo.findById(id).map(c -> {
            if (dto.getCodiceCer() != null)           c.setCodiceCer(dto.getCodiceCer().trim());
            if (dto.getDescrizione() != null)         c.setDescrizione(dto.getDescrizione());
            if (dto.getPericoloso() != null)          c.setPericoloso(dto.getPericoloso());
            if (dto.getStatoFisico() != null)         c.setStatoFisico(dto.getStatoFisico());
            if (dto.getUnitaMisura() != null)         c.setUnitaMisura(dto.getUnitaMisura());
            if (dto.getCodiceGestione() != null)      c.setCodiceGestione(dto.getCodiceGestione());
            if (dto.getDestinatarioAbituale() != null) c.setDestinatarioAbituale(dto.getDestinatarioAbituale());
            if (dto.getNote() != null)                c.setNote(dto.getNote());
            if (dto.getAttivo() != null)              c.setAttivo(dto.getAttivo());
            return ResponseEntity.ok(toCodiceDTO(codiceRepo.save(c)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/codici/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCodice(@PathVariable Long id) {
        if (!codiceRepo.existsById(id)) return ResponseEntity.notFound().build();
        codiceRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Movimenti ────────────────────────────────────────────────────
    @GetMapping("/movimenti")
    public List<MovimentoRifiutoDTO> getMovimenti(
            @RequestParam(required = false) Long stabilimentoId,
            @RequestParam(required = false) Long codiceRifiutoId,
            @RequestParam(required = false) Integer anno,
            @RequestParam(required = false) Integer mese) {

        List<MovimentoRifiuto> list;
        if (stabilimentoId != null && anno != null && mese != null) {
            list = movimentoRepo.findByStabilimentoAndAnnoAndMese(stabilimentoId, anno, mese);
        } else if (stabilimentoId != null && anno != null) {
            list = movimentoRepo.findByStabilimentoAndAnno(stabilimentoId, anno);
        } else if (codiceRifiutoId != null && anno != null) {
            list = movimentoRepo.findByCodiceRifiutoIdAndAnnoOrderByMeseAscTipoMovimentoAsc(codiceRifiutoId, anno);
        } else if (codiceRifiutoId != null) {
            list = movimentoRepo.findByCodiceRifiutoIdOrderByAnnoDescMeseDescCreatedAtDesc(codiceRifiutoId);
        } else {
            list = movimentoRepo.findAll();
        }
        return list.stream().map(this::toMovimentoDTO).collect(Collectors.toList());
    }

    @GetMapping("/movimenti/{id}")
    public ResponseEntity<MovimentoRifiutoDTO> getMovimento(@PathVariable Long id) {
        return movimentoRepo.findById(id)
                .map(m -> ResponseEntity.ok(toMovimentoDTO(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/movimenti")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<MovimentoRifiutoDTO> createMovimento(@RequestBody MovimentoRifiutoDTO dto) {
        CodiceRifiuto codice = codiceRepo.findById(dto.getCodiceRifiutoId())
                .orElseThrow(() -> new RuntimeException("Codice rifiuto non trovato"));

        MovimentoRifiuto m = new MovimentoRifiuto();
        m.setCodiceRifiuto(codice);
        m.setAnno(dto.getAnno());
        m.setMese(dto.getMese());
        m.setTipoMovimento(dto.getTipoMovimento());
        m.setQuantita(dto.getQuantita());
        m.setUnitaMisura(dto.getUnitaMisura() != null ? dto.getUnitaMisura() : codice.getUnitaMisura());
        m.setCodiceOperazione(dto.getCodiceOperazione());
        m.setDestinatario(dto.getDestinatario());
        m.setTrasportatore(dto.getTrasportatore());
        m.setNumeroFir(dto.getNumeroFir());
        m.setDataOperazione(dto.getDataOperazione());
        m.setNote(dto.getNote());

        return ResponseEntity.status(HttpStatus.CREATED).body(toMovimentoDTO(movimentoRepo.save(m)));
    }

    @PutMapping("/movimenti/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<MovimentoRifiutoDTO> updateMovimento(@PathVariable Long id,
                                                               @RequestBody MovimentoRifiutoDTO dto) {
        return movimentoRepo.findById(id).map(m -> {
            if (dto.getTipoMovimento() != null)   m.setTipoMovimento(dto.getTipoMovimento());
            if (dto.getQuantita() != null)        m.setQuantita(dto.getQuantita());
            if (dto.getUnitaMisura() != null)     m.setUnitaMisura(dto.getUnitaMisura());
            if (dto.getCodiceOperazione() != null) m.setCodiceOperazione(dto.getCodiceOperazione());
            if (dto.getDestinatario() != null)    m.setDestinatario(dto.getDestinatario());
            if (dto.getTrasportatore() != null)   m.setTrasportatore(dto.getTrasportatore());
            if (dto.getNumeroFir() != null)       m.setNumeroFir(dto.getNumeroFir());
            if (dto.getDataOperazione() != null)  m.setDataOperazione(dto.getDataOperazione());
            if (dto.getNote() != null)            m.setNote(dto.getNote());
            if (dto.getMese() != null)            m.setMese(dto.getMese());
            if (dto.getAnno() != null)            m.setAnno(dto.getAnno());
            return ResponseEntity.ok(toMovimentoDTO(movimentoRepo.save(m)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/movimenti/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABILE')")
    public ResponseEntity<Void> deleteMovimento(@PathVariable Long id) {
        if (!movimentoRepo.existsById(id)) return ResponseEntity.notFound().build();
        movimentoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/anni")
    public List<Integer> getAnni(@RequestParam Long stabilimentoId) {
        return movimentoRepo.findAnniByStabilimento(stabilimentoId);
    }

    // ─── Mapping ──────────────────────────────────────────────────────
    private CodiceRifiutoDTO toCodiceDTO(CodiceRifiuto c) {
        CodiceRifiutoDTO dto = new CodiceRifiutoDTO();
        dto.setId(c.getId());
        dto.setStabilimentoId(c.getStabilimento().getId());
        dto.setStabilimentoNome(c.getStabilimento().getNome());
        dto.setCodiceCer(c.getCodiceCer());
        dto.setDescrizione(c.getDescrizione());
        dto.setPericoloso(c.getPericoloso());
        dto.setStatoFisico(c.getStatoFisico());
        dto.setUnitaMisura(c.getUnitaMisura());
        dto.setCodiceGestione(c.getCodiceGestione());
        dto.setDestinatarioAbituale(c.getDestinatarioAbituale());
        dto.setNote(c.getNote());
        dto.setAttivo(c.getAttivo());
        return dto;
    }

    private MovimentoRifiutoDTO toMovimentoDTO(MovimentoRifiuto m) {
        MovimentoRifiutoDTO dto = new MovimentoRifiutoDTO();
        dto.setId(m.getId());
        dto.setCodiceRifiutoId(m.getCodiceRifiuto().getId());
        dto.setCodiceCer(m.getCodiceRifiuto().getCodiceCer());
        dto.setDescrizioneRifiuto(m.getCodiceRifiuto().getDescrizione());
        dto.setPericoloso(m.getCodiceRifiuto().getPericoloso());
        dto.setAnno(m.getAnno());
        dto.setMese(m.getMese());
        dto.setTipoMovimento(m.getTipoMovimento());
        dto.setQuantita(m.getQuantita());
        dto.setUnitaMisura(m.getUnitaMisura());
        dto.setCodiceOperazione(m.getCodiceOperazione());
        dto.setDestinatario(m.getDestinatario());
        dto.setTrasportatore(m.getTrasportatore());
        dto.setNumeroFir(m.getNumeroFir());
        dto.setDataOperazione(m.getDataOperazione());
        dto.setNote(m.getNote());
        return dto;
    }
}
