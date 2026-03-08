package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.ImportScadenzeResult;
import it.grandimolini.aia.dto.ImportScadenzeResult.RigaImport;
import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.ScadenzaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ImportScadenzeService {

    // Indici delle colonne nel file Excel (0-based)
    private static final int COL_SITO           = 1;
    private static final int COL_DATA_SCADENZA  = 2;
    private static final int COL_DATA_ATTIV     = 3;
    private static final int COL_DATA_ADEMPIM   = 4;
    private static final int COL_RIFERIMENTO    = 5;
    private static final int COL_CAUSALE        = 6;
    private static final int COL_DOCUMENTI      = 7;
    private static final int COL_DATA_DOC       = 8;
    private static final int COL_RISCONTRO      = 9;
    private static final int COL_ALTRO          = 10;

    /**
     * Riga 0-based (Apache POI) in cui iniziano i dati nel file GMI.
     * Struttura file "Scadenze e prescrizioni di gruppo.xlsx":
     *   - Row 0 (Excel 1): "INDICE"
     *   - Row 1 (Excel 2): vuota
     *   - Row 2 (Excel 3): vuota
     *   - Row 3 (Excel 4): header (SITO INTERESSATO | DATA TERMINE SCADENZA | ...)
     *   - Row 4 (Excel 5): prima riga dati  ← PRIMA_RIGA_DATI
     */
    private static final int PRIMA_RIGA_DATI = 4;

    @Autowired
    private StabilimentoService stabilimentoService;

    @Autowired
    private ScadenzaRepository scadenzaRepository;

    /**
     * Esegue solo il parsing e restituisce la preview senza salvare nulla.
     * Il frontend usa questo per mostrare l'anteprima e permettere la selezione.
     */
    public ImportScadenzeResult preview(MultipartFile file,
                                        Map<String, Long> sitoMapping) throws Exception {
        List<RigaImport> righe = parseFile(file, sitoMapping);
        Set<String> siti = new LinkedHashSet<>();
        for (RigaImport r : righe) {
            if (r.getSito() != null && !r.getSito().isBlank()) siti.add(r.getSito());
        }
        return ImportScadenzeResult.builder()
                .righe(righe)
                .sitiTrovati(new ArrayList<>(siti))
                .create(0)
                .saltate(0)
                .build();
    }

    /**
     * Salva le righe selezionate. Riceve la lista di righe già elaborate
     * dalla preview (con stabilimentoId popolato) e le persiste.
     */
    public ImportScadenzeResult importa(List<RigaImport> righeSelezionate) {
        int create = 0;
        int saltate = 0;

        for (RigaImport riga : righeSelezionate) {
            if (!riga.isSelezionata() || riga.getErrore() != null) {
                saltate++;
                continue;
            }
            if (riga.getDataScadenza() == null) {
                saltate++;
                continue;
            }

            Scadenza s = new Scadenza();

            // Stabilimento
            if (riga.getStabilimentoId() != null) {
                stabilimentoService.findById(riga.getStabilimentoId())
                        .ifPresent(s::setStabilimento);
            }

            // Campi principali
            String titolo = nullToEmpty(riga.getCausale());
            if (titolo.isBlank()) titolo = "Scadenza " + riga.getSito() + " " + riga.getDataScadenza();
            s.setTitolo(titolo.length() > 255 ? titolo.substring(0, 255) : titolo);
            s.setDataScadenza(riga.getDataScadenza());
            s.setDataPrevistaAttivazione(riga.getDataPrevistaAttivazione());
            s.setDataCompletamento(riga.getDataAdempimento());
            s.setRiferimento(riga.getRiferimento());
            s.setSitoOrigine(riga.getSito());
            s.setTipoScadenza(rilevaTipo(riga));

            // Note = documenti correlati + riscontro ente
            StringBuilder note = new StringBuilder();
            if (notNull(riga.getDocumentiCorrelati())) note.append("Doc: ").append(riga.getDocumentiCorrelati());
            if (notNull(riga.getRiscontroEnte())) {
                if (note.length() > 0) note.append(" | ");
                note.append("Riscontro: ").append(riga.getRiscontroEnte());
            }
            if (notNull(riga.getAltro())) {
                if (note.length() > 0) note.append(" | ");
                note.append(riga.getAltro());
            }
            if (note.length() > 0) s.setNote(note.substring(0, Math.min(note.length(), 1000)));

            s.setStato(Scadenza.StatoScadenza.PENDING);
            s.setPriorita(Scadenza.Priorita.MEDIA);
            s.setGiorniPreavviso(20);

            scadenzaRepository.save(s);
            create++;
        }

        return ImportScadenzeResult.builder()
                .create(create)
                .saltate(saltate)
                .build();
    }

    // ─── parsing interno ──────────────────────────────────────────────────

    private List<RigaImport> parseFile(MultipartFile file,
                                       Map<String, Long> sitoMapping) throws Exception {
        List<RigaImport> result = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);

            for (int r = PRIMA_RIGA_DATI; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String sito = getString(row, COL_SITO);
                if (sito == null || sito.isBlank()) continue;

                String causale = getString(row, COL_CAUSALE);
                if (causale == null || causale.isBlank()) continue; // riga vuota

                LocalDate dataScadenza = getDate(row, COL_DATA_SCADENZA);

                RigaImport.RigaImportBuilder b = RigaImport.builder()
                        .rigaExcel(r + 1)
                        .sito(sito.toUpperCase().trim())
                        .dataScadenza(dataScadenza)
                        .dataPrevistaAttivazione(getDate(row, COL_DATA_ATTIV))
                        .dataAdempimento(getDate(row, COL_DATA_ADEMPIM))
                        .riferimento(getString(row, COL_RIFERIMENTO))
                        .causale(causale)
                        .documentiCorrelati(getString(row, COL_DOCUMENTI))
                        .riscontroEnte(getString(row, COL_RISCONTRO))
                        .altro(getString(row, COL_ALTRO))
                        .selezionata(true);

                // Match stabilimento
                String sitoKey = sito.toUpperCase().trim();
                if (sitoMapping != null && sitoMapping.containsKey(sitoKey)) {
                    Long stId = sitoMapping.get(sitoKey);
                    b.stabilimentoId(stId);
                    stabilimentoService.findById(stId)
                            .ifPresent(st -> b.stabilimentoNome(st.getNome()));
                } else {
                    // Prova auto-match per sigla
                    stabilimentoService.findBySigla(sitoKey).ifPresent(st -> {
                        b.stabilimentoId(st.getId());
                        b.stabilimentoNome(st.getNome());
                    });
                }

                if (dataScadenza == null) {
                    b.errore("Data scadenza non riconoscibile");
                    b.selezionata(false);
                }

                RigaImport riga = b.build();
                riga.setTipoScadenzaRilevato(rilevaTipo(riga).name());
                result.add(riga);
            }
        }
        return result;
    }

    /**
     * Rileva il tipo di scadenza dai campi riferimento e causale.
     * Le keyword sono calibrate sul formato GMI "Scadenze e prescrizioni di gruppo.xlsx"
     * e sui registri operativi PA01CO-R01 / PA01LI-R01.
     */
    private Scadenza.TipoScadenza rilevaTipo(RigaImport r) {
        String rif  = nullToEmpty(r.getRiferimento()).toLowerCase();
        String caus = nullToEmpty(r.getCausale()).toLowerCase();

        // ── Monitoraggio PMC (campionamenti e misure) ─────────────────────────
        if (rif.contains("pmc") || caus.contains("pmc")
                || caus.contains("monitoraggio")
                || caus.contains("campionamento")
                || caus.contains("scarico fognario")
                || caus.contains("acque meteoriche")
                || caus.contains("acque sotterranee")
                || caus.contains("piezometri")
                || caus.contains("emissioni in atmosfera")
                || caus.contains("camini")
                || caus.contains("e. coli")) {
            return Scadenza.TipoScadenza.MONITORAGGIO_PMC;
        }

        // ── Relazione annuale / PMC ───────────────────────────────────────────
        if (caus.contains("relazione annuale") || caus.contains("sintesi dei risultati")
                || caus.contains("trasmissione della relazione")
                || caus.contains("relazione sul monitoraggio")) {
            return Scadenza.TipoScadenza.RELAZIONE_ANNUALE;
        }

        // ── Rinnovo / riesame AIA ─────────────────────────────────────────────
        if (caus.contains("rinnovo") || caus.contains("domanda di aia")
                || caus.contains("riesame") || caus.contains("ri-presentazione")
                || caus.contains("presentazione dell'istanza")) {
            return Scadenza.TipoScadenza.RINNOVO_AIA;
        }

        // ── Adempimento prescrizione (interventi tecnici da AIA) ───────────────
        if (caus.contains("adozione") || caus.contains("installazione")
                || caus.contains("messa a regime") || caus.contains("messa in esercizio")
                || caus.contains("intervento") || caus.contains("coibentazione")
                || caus.contains("ripristino") || caus.contains("entro")
                || caus.contains("trasmettere risultati")) {
            return Scadenza.TipoScadenza.ADEMPIMENTO_PRESCRIZIONE;
        }

        // ── Manutenzione ──────────────────────────────────────────────────────
        if (caus.contains("manutenzione") || caus.contains("taratura")
                || caus.contains("ispezione") || caus.contains("verifica periodica")) {
            return Scadenza.TipoScadenza.MANUTENZIONE;
        }

        // ── Pagamento ─────────────────────────────────────────────────────────
        if (caus.contains("pagamento") || caus.contains("contributo")
                || caus.contains("tariffa") || caus.contains("quietanza")
                || caus.contains("csqa")) {
            return Scadenza.TipoScadenza.PAGAMENTO;
        }

        // ── Comunicazione generica ────────────────────────────────────────────
        if (caus.contains("comunicazione") || caus.contains("trasmissione")
                || caus.contains("invio") || caus.contains("compilazione")
                || caus.contains("dichiarazione") || caus.contains("questionario")) {
            return Scadenza.TipoScadenza.COMUNICAZIONE;
        }

        // ── Integrazione a richiesta ente ─────────────────────────────────────
        if (caus.contains("integrazione") || caus.contains("richiesta")
                || rif.contains("integrazione")) {
            return Scadenza.TipoScadenza.INTEGRAZIONE_ENTE;
        }

        return Scadenza.TipoScadenza.ALTRO;
    }

    // ─── helper lettura celle ─────────────────────────────────────────────

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default:      return null;
        }
    }

    private LocalDate getDate(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                // Rimuovi note parentetiche come "28/05/2027 (*)"
                s = s.replaceAll("\\s*\\(.*\\)", "").trim();
                if (s.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    String[] p = s.split("/");
                    return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                }
                if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    return LocalDate.parse(s);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private boolean notNull(String s) { return s != null && !s.isBlank(); }
}
