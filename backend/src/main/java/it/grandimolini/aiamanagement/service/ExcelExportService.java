package it.grandimolini.aiamanagement.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import it.grandimolini.aia.model.DatiAmbientali;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExcelExportService {

    /**
     * Export Prescrizioni to Excel
     */
    public byte[] exportPrescrizioni(List<Prescrizione> prescrizioni) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Prescrizioni");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "Codice", "Descrizione", "Matrice Ambientale", "Stato",
                "Priorità", "Stabilimento", "Data Scadenza", "Note"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            for (Prescrizione p : prescrizioni) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getCodice());
                row.createCell(2).setCellValue(p.getDescrizione());
                row.createCell(3).setCellValue(p.getMatriceAmbientale() != null ? p.getMatriceAmbientale().name() : "");
                row.createCell(4).setCellValue(p.getStato() != null ? p.getStato().name() : "");
                row.createCell(5).setCellValue(p.getPriorita() != null ? p.getPriorita().name() : "");
                row.createCell(6).setCellValue(p.getStabilimento() != null ? p.getStabilimento().getNome() : "");

                if (p.getDataScadenza() != null) {
                    Cell cell = row.createCell(7);
                    cell.setCellValue(convertToDate(p.getDataScadenza()));
                    cell.setCellStyle(dateStyle);
                } else {
                    row.createCell(7).setCellValue("");
                }

                row.createCell(8).setCellValue(p.getNote() != null ? p.getNote() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export Scadenze to Excel
     */
    public byte[] exportScadenze(List<Scadenza> scadenze) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Scadenze");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "Titolo", "Descrizione", "Data Scadenza", "Tipo Scadenza",
                "Stato", "Priorità", "Giorni Preavviso", "Email Notifica", "Stabilimento"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            for (Scadenza s : scadenze) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getTitolo());
                row.createCell(2).setCellValue(s.getDescrizione() != null ? s.getDescrizione() : "");

                if (s.getDataScadenza() != null) {
                    Cell cell = row.createCell(3);
                    cell.setCellValue(convertToDate(s.getDataScadenza()));
                    cell.setCellStyle(dateStyle);
                } else {
                    row.createCell(3).setCellValue("");
                }

                row.createCell(4).setCellValue(s.getTipoScadenza() != null ? s.getTipoScadenza().name() : "");
                row.createCell(5).setCellValue(s.getStato() != null ? s.getStato().name() : "");
                row.createCell(6).setCellValue(s.getPriorita() != null ? s.getPriorita().name() : "");
                row.createCell(7).setCellValue(s.getGiorniPreavviso() != null ? s.getGiorniPreavviso() : 0);
                row.createCell(8).setCellValue(s.getEmailNotifica() != null ? s.getEmailNotifica() : "");
                row.createCell(9).setCellValue(s.getStabilimento() != null ? s.getStabilimento().getNome() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export Dati Ambientali to Excel
     */
    public byte[] exportDatiAmbientali(List<DatiAmbientali> datiList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dati Ambientali");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "Parametro", "Valore Misurato", "Unità Misura", "Limite Autorizzato",
                "Stato Conformità", "Data Campionamento", "Metodo Analisi", "Laboratorio", "Stabilimento"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            for (DatiAmbientali d : datiList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(d.getId());
                row.createCell(1).setCellValue(d.getParametro());
                row.createCell(2).setCellValue(d.getValoreMisurato() != null ? d.getValoreMisurato() : 0.0);
                row.createCell(3).setCellValue(d.getUnitaMisura() != null ? d.getUnitaMisura() : "");
                row.createCell(4).setCellValue(d.getLimiteAutorizzato() != null ? d.getLimiteAutorizzato() : 0.0);
                row.createCell(5).setCellValue(d.getStatoConformita() != null ? d.getStatoConformita().name() : "");

                if (d.getDataCampionamento() != null) {
                    Cell cell = row.createCell(6);
                    cell.setCellValue(convertToDate(d.getDataCampionamento()));
                    cell.setCellStyle(dateStyle);
                } else {
                    row.createCell(6).setCellValue("");
                }

//                row.createCell(7).setCellValue(d.getMetodoAnalisi() != null ? d.getMetodoAnalisi() : "");
                row.createCell(8).setCellValue(d.getLaboratorio() != null ? d.getLaboratorio() : "");
//                row.createCell(9).setCellValue(d.getStabilimento() != null ? d.getStabilimento().getNome() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export Utenti to Excel
     */
    public byte[] exportUtenti(List<User> utenti) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Utenti");

            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "ID", "Username", "Nome", "Cognome", "Email", "Ruolo", "Attivo"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            for (User u : utenti) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(u.getId());
                row.createCell(1).setCellValue(u.getUsername());
//                row.createCell(2).setCellValue(u.getNome());
//                row.createCell(3).setCellValue(u.getCognome());
                row.createCell(4).setCellValue(u.getEmail());
                row.createCell(5).setCellValue(u.getRuolo() != null ? u.getRuolo().name() : "");
//                row.createCell(6).setCellValue(u.isAttivo() ? "Sì" : "No");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ========== HELPER METHODS ==========

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Set background color
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Set border
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Set font
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }

    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
