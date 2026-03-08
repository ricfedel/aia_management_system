package it.grandimolini.aia.service;

import it.grandimolini.aia.model.*;
import it.grandimolini.aia.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Genera l'Allegato 2 AIA – Sintesi Piano di Monitoraggio e Controllo (PMC).
 *
 * Struttura del workbook:
 *   Foglio 1 "Copertina"             – dati stabilimento + anno
 *   Foglio 2 "Punti di Monitoraggio" – elenco punti attivi con frequenza
 *   Foglio 3 "Parametri e Limiti"    – un parametro per riga con limite e normativa
 *   Foglio 4 "Rilevazioni Anno"      – tutte le rilevazioni dell'anno con stato
 *   Foglio 5 "Riepilogo Conformità"  – conteggi e % per punto/parametro
 */
@Service
@RequiredArgsConstructor
public class Allegato2Service {

    private final StabilimentoRepository      stabilimentoRepo;
    private final MonitoraggioRepository      monitoraggioRepo;
    private final RilevazioneMisuraRepository  rilevRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ──────────────────────────────────────────────────────────────────────
    public byte[] generate(Long stabilimentoId, int anno) throws IOException {

        Stabilimento stab = stabilimentoRepo.findById(stabilimentoId)
                .orElseThrow(() -> new RuntimeException("Stabilimento non trovato: " + stabilimentoId));

        List<Monitoraggio> punti = monitoraggioRepo.findByStabilimentoId(stabilimentoId)
                .stream().filter(m -> Boolean.TRUE.equals(m.getAttivo())).toList();

        LocalDate from = LocalDate.of(anno, 1, 1);
        LocalDate to   = LocalDate.of(anno, 12, 31);
        List<RilevazioneMisura> rilevazioni = rilevRepo.findByStabilimentoAndPeriodo(stabilimentoId, from, to);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Stili condivisi
            Styles st = new Styles(wb);

            buildCopertina(wb, st, stab, anno);
            buildPuntiMonitoraggio(wb, st, punti);
            buildParametriLimiti(wb, st, punti);
            buildRilevazioni(wb, st, rilevazioni, anno);
            buildRiepilogoConformita(wb, st, punti, rilevazioni);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // FOGLIO 1 – COPERTINA
    // ══════════════════════════════════════════════════════════════════════
    private void buildCopertina(XSSFWorkbook wb, Styles st, Stabilimento stab, int anno) {
        Sheet sheet = wb.createSheet("Copertina");
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 12000);

        int r = 0;
        Row title = sheet.createRow(r++);
        title.setHeightInPoints(30);
        Cell c0 = title.createCell(0);
        c0.setCellValue("ALLEGATO 2 – SINTESI PIANO DI MONITORAGGIO E CONTROLLO (PMC)");
        c0.setCellStyle(st.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        r++; // riga vuota

        String[][] info = {
            {"Azienda",              "Grandi Molini Italiani S.p.A."},
            {"Stabilimento",         stab.getNome()},
            {"Indirizzo",            nvl(stab.getIndirizzo()) + (stab.getCitta() != null ? ", " + stab.getCitta() : "")},
            {"Numero AIA",           nvl(stab.getNumeroAIA())},
            {"Data rilascio AIA",    stab.getDataRilascioAIA() != null ? stab.getDataRilascioAIA().format(FMT) : "—"},
            {"Anno di riferimento",  String.valueOf(anno)},
            {"Data generazione",     LocalDate.now().format(FMT)},
            {"Responsabile amb.",    nvl(stab.getResponsabileAmbientale())},
        };

        for (String[] row : info) {
            Row xr = sheet.createRow(r++);
            Cell label = xr.createCell(0);
            label.setCellValue(row[0]);
            label.setCellStyle(st.headerBlue);
            Cell val = xr.createCell(1);
            val.setCellValue(row[1]);
            val.setCellStyle(st.data);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // FOGLIO 2 – PUNTI DI MONITORAGGIO
    // ══════════════════════════════════════════════════════════════════════
    private void buildPuntiMonitoraggio(XSSFWorkbook wb, Styles st, List<Monitoraggio> punti) {
        Sheet sheet = wb.createSheet("Punti di Monitoraggio");

        String[] headers = {
            "N°", "Codice", "Descrizione", "Tipo", "Punto Emissione",
            "Frequenza", "Prossima Scadenza", "Laboratorio", "Metodica",
            "Normativa Rif.", "Matricola", "N° Parametri"
        };
        writeHeaderRow(sheet, st, headers);

        int[] widths = {1500, 3000, 8000, 5000, 4000, 4000, 4500, 5000, 4000, 5000, 3000, 3500};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

        int r = 1;
        for (Monitoraggio m : punti) {
            Row row = sheet.createRow(r);
            setCell(row, 0, r,                                                 st.data);
            setCell(row, 1, nvl(m.getCodice()),                                st.dataCenter);
            setCell(row, 2, nvl(m.getDescrizione()),                           st.data);
            setCell(row, 3, m.getTipoMonitoraggio() != null ? tipoLabel(m.getTipoMonitoraggio()) : "", st.data);
            setCell(row, 4, nvl(m.getPuntoEmissione()),                        st.dataCenter);
            setCell(row, 5, m.getFrequenza() != null ? freqLabel(m.getFrequenza()) : "", st.dataCenter);
            setCell(row, 6, m.getProssimaScadenza() != null ? m.getProssimaScadenza().format(FMT) : "—", st.dataCenter);
            setCell(row, 7, nvl(m.getLaboratorio()),                           st.data);
            setCell(row, 8, nvl(m.getMetodica()),                              st.data);
            setCell(row, 9, nvl(m.getNormativaRiferimento()),                  st.data);
            setCell(row, 10, nvl(m.getMatricola()),                            st.dataCenter);
            setCell(row, 11, m.getParametri() != null ? m.getParametri().size() : 0, st.dataCenter);
            r++;
        }

        // Aggiungi filtro automatico
        if (!punti.isEmpty()) {
            sheet.setAutoFilter(new CellRangeAddress(0, r - 1, 0, headers.length - 1));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // FOGLIO 3 – PARAMETRI E LIMITI
    // ══════════════════════════════════════════════════════════════════════
    private void buildParametriLimiti(XSSFWorkbook wb, Styles st, List<Monitoraggio> punti) {
        Sheet sheet = wb.createSheet("Parametri e Limiti");

        String[] headers = {
            "N°", "Codice Punto", "Tipo Punto", "Codice Param.",
            "Nome Parametro", "Unità Misura", "Limite Valore", "Unità Limite",
            "Normativa Limite", "Note"
        };
        writeHeaderRow(sheet, st, headers);

        int[] widths = {1500, 3500, 5000, 3500, 7000, 3500, 3500, 3500, 6000, 5000};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

        int r = 1;
        for (Monitoraggio m : punti) {
            if (m.getParametri() == null || m.getParametri().isEmpty()) continue;
            for (ParametroMonitoraggio p : m.getParametri()) {
                Row row = sheet.createRow(r);
                setCell(row, 0, r,                                             st.data);
                setCell(row, 1, nvl(m.getCodice()),                            st.dataCenter);
                setCell(row, 2, m.getTipoMonitoraggio() != null ? tipoLabel(m.getTipoMonitoraggio()) : "", st.data);
                setCell(row, 3, nvl(p.getCodice()),                            st.dataCenter);
                setCell(row, 4, nvl(p.getNome()),                              st.data);
                setCell(row, 5, nvl(p.getUnitaMisura()),                       st.dataCenter);
                if (p.getLimiteValore() != null) {
                    setNumCell(row, 6, p.getLimiteValore(),                    st.dataRight);
                } else {
                    setCell(row, 6, "—",                                       st.dataCenter);
                }
                String unitaLim = p.getLimiteUnita() != null ? p.getLimiteUnita() : nvl(p.getUnitaMisura());
                setCell(row, 7, unitaLim,                                      st.dataCenter);
                setCell(row, 8, nvl(p.getLimiteRiferimento()),                 st.data);
                setCell(row, 9, nvl(p.getNote()),                              st.data);
                r++;
            }
        }

        if (r > 1) {
            sheet.setAutoFilter(new CellRangeAddress(0, r - 1, 0, headers.length - 1));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // FOGLIO 4 – RILEVAZIONI ANNO
    // ══════════════════════════════════════════════════════════════════════
    private void buildRilevazioni(XSSFWorkbook wb, Styles st, List<RilevazioneMisura> rilevazioni, int anno) {
        Sheet sheet = wb.createSheet("Rilevazioni " + anno);

        String[] headers = {
            "N°", "Data", "Punto Monit.", "Tipo Punto",
            "Codice Param.", "Parametro", "Valore Mis.", "Unità",
            "Limite", "% Limite", "Stato Conformità",
            "Laboratorio", "Metodo", "Rapporto Prova", "Note"
        };
        writeHeaderRow(sheet, st, headers);

        int[] widths = {1500, 3200, 3500, 5000, 3000, 6000, 3500, 2800, 3000, 2800, 5000, 4500, 3500, 3500, 4000};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

        // Ordina per data
        List<RilevazioneMisura> sorted = rilevazioni.stream()
                .sorted(Comparator.comparing(r -> r.getDataCampionamento() != null
                        ? r.getDataCampionamento() : LocalDate.MIN))
                .collect(Collectors.toList());

        int r = 1;
        for (RilevazioneMisura rilev : sorted) {
            ParametroMonitoraggio param = rilev.getParametroMonitoraggio();
            Monitoraggio mon = param != null ? param.getMonitoraggio() : null;

            Row row = sheet.createRow(r);
            setCell(row, 0,  r,                                                st.data);
            setCell(row, 1,  rilev.getDataCampionamento() != null ? rilev.getDataCampionamento().format(FMT) : "—", st.dataCenter);
            setCell(row, 2,  mon  != null ? nvl(mon.getCodice())  : "—",       st.dataCenter);
            setCell(row, 3,  mon  != null && mon.getTipoMonitoraggio() != null ? tipoLabel(mon.getTipoMonitoraggio()) : "", st.data);
            setCell(row, 4,  param != null ? nvl(param.getCodice()) : "—",     st.dataCenter);
            setCell(row, 5,  param != null ? nvl(param.getNome())   : "—",     st.data);
            setNumCell(row, 6, rilev.getValoreMisurato(),                       st.dataRight);
            setCell(row, 7,  nvl(rilev.getUnitaMisura()),                       st.dataCenter);

            Double lim = param != null ? param.getLimiteValore() : null;
            if (lim != null) {
                setNumCell(row, 8, lim,                                         st.dataRight);
                double perc = lim != 0 ? (rilev.getValoreMisurato() / lim) * 100 : 0;
                setNumCell(row, 9, Math.round(perc * 10) / 10.0,               getPercStyle(st, perc));
            } else {
                setCell(row, 8, "—",  st.dataCenter);
                setCell(row, 9, "—",  st.dataCenter);
            }

            // Stato con colore
            String stato = rilev.getStatoConformita() != null ? rilev.getStatoConformita().name() : "—";
            CellStyle statoStyle = getStatoStyle(st, rilev.getStatoConformita());
            Cell statoCell = row.createCell(10);
            statoCell.setCellValue(stato);
            statoCell.setCellStyle(statoStyle);

            setCell(row, 11, nvl(rilev.getLaboratorio()),                       st.data);
            setCell(row, 12, nvl(rilev.getRapportoProva()),                     st.data);
            setCell(row, 13, "",                                                st.data);  // colonna Metodo non disponibile
            setCell(row, 14, nvl(rilev.getNote()),                              st.data);
            r++;
        }

        if (r > 1) {
            sheet.setAutoFilter(new CellRangeAddress(0, r - 1, 0, headers.length - 1));
            sheet.createFreezePane(0, 1);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // FOGLIO 5 – RIEPILOGO CONFORMITÀ
    // ══════════════════════════════════════════════════════════════════════
    private void buildRiepilogoConformita(XSSFWorkbook wb, Styles st,
                                          List<Monitoraggio> punti,
                                          List<RilevazioneMisura> rilevazioni) {
        Sheet sheet = wb.createSheet("Riepilogo Conformità");

        String[] headers = {
            "Punto", "Tipo", "Codice Param.", "Parametro",
            "Unità", "Limite", "Tot. Rilev.",
            "Conformi", "Attenzione", "Non Conf.",
            "% Conformi", "% Attenzione", "% Non Conf.",
            "Stato Globale"
        };
        writeHeaderRow(sheet, st, headers);

        int[] widths = {3500, 5000, 3000, 6500, 2800, 3000,
                        2800, 2800, 3000, 2800, 3200, 3500, 3200, 4000};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

        // Raggruppa rilevazioni per parametroMonitoraggioId
        Map<Long, List<RilevazioneMisura>> byParam = rilevazioni.stream()
                .filter(r -> r.getParametroMonitoraggio() != null)
                .collect(Collectors.groupingBy(r -> r.getParametroMonitoraggio().getId()));

        int r = 1;
        for (Monitoraggio m : punti) {
            if (m.getParametri() == null || m.getParametri().isEmpty()) continue;
            for (ParametroMonitoraggio p : m.getParametri()) {
                List<RilevazioneMisura> rl = byParam.getOrDefault(p.getId(), List.of());
                long tot   = rl.size();
                long conf  = rl.stream().filter(x -> x.getStatoConformita() == RilevazioneMisura.StatoConformita.CONFORME).count();
                long att   = rl.stream().filter(x -> x.getStatoConformita() == RilevazioneMisura.StatoConformita.ATTENZIONE).count();
                long nc    = rl.stream().filter(x -> x.getStatoConformita() == RilevazioneMisura.StatoConformita.NON_CONFORME).count();
                double pConf = tot > 0 ? Math.round(conf  * 1000.0 / tot) / 10.0 : 0;
                double pAtt  = tot > 0 ? Math.round(att   * 1000.0 / tot) / 10.0 : 0;
                double pNc   = tot > 0 ? Math.round(nc    * 1000.0 / tot) / 10.0 : 0;

                // Stato globale peggiore
                RilevazioneMisura.StatoConformita statoGlobale = null;
                if (nc > 0) statoGlobale = RilevazioneMisura.StatoConformita.NON_CONFORME;
                else if (att > 0) statoGlobale = RilevazioneMisura.StatoConformita.ATTENZIONE;
                else if (conf > 0) statoGlobale = RilevazioneMisura.StatoConformita.CONFORME;

                Row row = sheet.createRow(r);
                setCell(row, 0,  nvl(m.getCodice()),                            st.dataCenter);
                setCell(row, 1,  m.getTipoMonitoraggio() != null ? tipoLabel(m.getTipoMonitoraggio()) : "", st.data);
                setCell(row, 2,  nvl(p.getCodice()),                            st.dataCenter);
                setCell(row, 3,  nvl(p.getNome()),                              st.data);
                setCell(row, 4,  nvl(p.getUnitaMisura()),                       st.dataCenter);
                if (p.getLimiteValore() != null) {
                    setNumCell(row, 5, p.getLimiteValore(),                     st.dataRight);
                } else {
                    setCell(row, 5, "—",                                        st.dataCenter);
                }
                setNumCell(row, 6, (double) tot,                                st.dataCenter);
                setNumCell(row, 7, (double) conf,                               st.dataCenter);
                setNumCell(row, 8, (double) att,                                tot > 0 && att > 0 ? st.cellAtt : st.dataCenter);
                setNumCell(row, 9, (double) nc,                                 tot > 0 && nc  > 0 ? st.cellNc  : st.dataCenter);
                setNumCell(row, 10, pConf,                                      st.dataCenter);
                setNumCell(row, 11, pAtt,                                       tot > 0 && att > 0 ? st.cellAtt : st.dataCenter);
                setNumCell(row, 12, pNc,                                        tot > 0 && nc  > 0 ? st.cellNc  : st.dataCenter);

                String sgLabel = statoGlobale != null ? statoGlobale.name() : (tot == 0 ? "NESSUNA RILEV." : "—");
                CellStyle sgStyle = getStatoStyle(st, statoGlobale);
                Cell sgCell = row.createCell(13);
                sgCell.setCellValue(sgLabel);
                sgCell.setCellStyle(sgStyle);

                r++;
            }
        }

        if (r > 1) {
            sheet.setAutoFilter(new CellRangeAddress(0, r - 1, 0, headers.length - 1));
            sheet.createFreezePane(0, 1);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // STILI
    // ══════════════════════════════════════════════════════════════════════

    private static class Styles {
        final CellStyle title;
        final CellStyle headerBlue;
        final CellStyle headerGreen;
        final CellStyle data;
        final CellStyle dataCenter;
        final CellStyle dataRight;
        final CellStyle cellOk;
        final CellStyle cellAtt;
        final CellStyle cellNc;
        final CellStyle cellNd;

        Styles(XSSFWorkbook wb) {
            // Titolo copertina
            title = wb.createCellStyle();
            Font ft = wb.createFont();
            ft.setBold(true); ft.setFontHeightInPoints((short) 14);
            ft.setColor(IndexedColors.WHITE.getIndex());
            title.setFont(ft);
            title.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            title.setAlignment(HorizontalAlignment.CENTER);
            title.setVerticalAlignment(VerticalAlignment.CENTER);
            title.setWrapText(true);

            // Header intestazione colonne
            headerBlue = wb.createCellStyle();
            Font fh = wb.createFont(); fh.setBold(true);
            fh.setColor(IndexedColors.WHITE.getIndex());
            headerBlue.setFont(fh);
            headerBlue.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerBlue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(headerBlue);

            headerGreen = wb.createCellStyle();
            Font fg2 = wb.createFont(); fg2.setBold(true);
            fg2.setColor(IndexedColors.WHITE.getIndex());
            headerGreen.setFont(fg2);
            headerGreen.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(headerGreen);

            // Dati standard
            data = wb.createCellStyle();
            setBorder(data);
            data.setWrapText(false);

            dataCenter = wb.createCellStyle();
            setBorder(dataCenter);
            dataCenter.setAlignment(HorizontalAlignment.CENTER);

            dataRight = wb.createCellStyle();
            setBorder(dataRight);
            dataRight.setAlignment(HorizontalAlignment.RIGHT);

            // Conformi (verde chiaro)
            cellOk = wb.createCellStyle();
            setBorder(cellOk);
            cellOk.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            cellOk.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellOk.setAlignment(HorizontalAlignment.CENTER);
            Font fok = wb.createFont(); fok.setBold(true); fok.setColor(IndexedColors.DARK_GREEN.getIndex());
            cellOk.setFont(fok);

            // Attenzione (arancione chiaro)
            cellAtt = wb.createCellStyle();
            setBorder(cellAtt);
            cellAtt.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            cellAtt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellAtt.setAlignment(HorizontalAlignment.CENTER);
            Font fatt = wb.createFont(); fatt.setBold(true); fatt.setColor(IndexedColors.ORANGE.getIndex());
            cellAtt.setFont(fatt);

            // Non conforme (rosso chiaro)
            cellNc = wb.createCellStyle();
            setBorder(cellNc);
            cellNc.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            cellNc.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellNc.setAlignment(HorizontalAlignment.CENTER);
            Font fnc = wb.createFont(); fnc.setBold(true); fnc.setColor(IndexedColors.DARK_RED.getIndex());
            cellNc.setFont(fnc);

            // Nessuna rilevazione (grigio)
            cellNd = wb.createCellStyle();
            setBorder(cellNd);
            cellNd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cellNd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellNd.setAlignment(HorizontalAlignment.CENTER);
        }

        private static void setBorder(CellStyle s) {
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private void writeHeaderRow(Sheet sheet, Styles st, String[] headers) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(18);
        for (int i = 0; i < headers.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(st.headerBlue);
        }
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }

    private void setCell(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void setNumCell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private CellStyle getPercStyle(Styles st, double perc) {
        if (perc > 100) return st.cellNc;
        if (perc >  80) return st.cellAtt;
        return st.dataRight;
    }

    private CellStyle getStatoStyle(Styles st, RilevazioneMisura.StatoConformita stato) {
        if (stato == null) return st.cellNd;
        return switch (stato) {
            case CONFORME     -> st.cellOk;
            case ATTENZIONE   -> st.cellAtt;
            case NON_CONFORME -> st.cellNc;
        };
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    private static String tipoLabel(Monitoraggio.TipoMonitoraggio t) {
        return switch (t) {
            case EMISSIONI_ATMOSFERA -> "Emissioni in Atmosfera";
            case SCARICHI_IDRICI     -> "Scarichi Idrici";
            case ACQUE_METEORICHE    -> "Acque Meteoriche";
            case FALDA               -> "Falda";
            case PIEZOMETRO          -> "Piezometro";
            case RUMORE              -> "Rumore";
            case SUOLO               -> "Suolo";
            case ODORI               -> "Odori";
        };
    }

    private static String freqLabel(Monitoraggio.FrequenzaMonitoraggio f) {
        return switch (f) {
            case GIORNALIERA -> "Giornaliera";
            case SETTIMANALE -> "Settimanale";
            case MENSILE     -> "Mensile";
            case BIMESTRALE  -> "Bimestrale";
            case TRIMESTRALE -> "Trimestrale";
            case SEMESTRALE  -> "Semestrale";
            case ANNUALE     -> "Annuale";
            case BIENNALE    -> "Biennale";
            case TRIENNALE   -> "Triennale";
        };
    }
}
