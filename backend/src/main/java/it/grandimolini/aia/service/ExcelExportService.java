package it.grandimolini.aia.service;

import it.grandimolini.aia.model.DatiAmbientali;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportDatiAmbientali(List<DatiAmbientali> datiList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Dati Ambientali");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Data Campionamento", "Stabilimento", "Parametro", "Valore", "Unità",
                              "Limite", "Stato Conformità", "Laboratorio", "Rapporto Prova"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (DatiAmbientali dato : datiList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dato.getDataCampionamento().format(DATE_FORMATTER));
                row.createCell(1).setCellValue(dato.getMonitoraggio().getStabilimento().getNome());
                row.createCell(2).setCellValue(dato.getParametro());
                row.createCell(3).setCellValue(dato.getValoreMisurato());
                row.createCell(4).setCellValue(dato.getUnitaMisura());
                row.createCell(5).setCellValue(dato.getLimiteAutorizzato());
                row.createCell(6).setCellValue(dato.getStatoConformita().toString());
                row.createCell(7).setCellValue(dato.getLaboratorio());
                row.createCell(8).setCellValue(dato.getRapportoProva());

                for (int i = 0; i < columns.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportPrescrizioni(List<Prescrizione> prescrizioniList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Prescrizioni");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"Codice", "Stabilimento", "Descrizione", "Matrice Ambientale",
                              "Stato", "Priorità", "Data Emissione", "Note"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Prescrizione prescrizione : prescrizioniList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(prescrizione.getCodice());
                row.createCell(1).setCellValue(prescrizione.getStabilimento().getNome());
                row.createCell(2).setCellValue(prescrizione.getDescrizione());
                row.createCell(3).setCellValue(prescrizione.getMatriceAmbientale() != null ? prescrizione.getMatriceAmbientale().toString() : "");
                row.createCell(4).setCellValue(prescrizione.getStato() != null ? prescrizione.getStato().toString() : "");
                row.createCell(5).setCellValue(prescrizione.getPriorita() != null ? prescrizione.getPriorita().toString() : "");
                row.createCell(6).setCellValue(prescrizione.getDataEmissione() != null ? prescrizione.getDataEmissione().format(DATE_FORMATTER) : "");
                row.createCell(7).setCellValue(prescrizione.getNote() != null ? prescrizione.getNote() : "");

                for (int i = 0; i < columns.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportScadenze(List<Scadenza> scadenzeList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Scadenze");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"Titolo", "Stabilimento", "Tipo Scadenza", "Data Scadenza",
                              "Priorità", "Stato", "Responsabile", "Note"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Scadenza scadenza : scadenzeList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(scadenza.getTitolo());
                row.createCell(1).setCellValue(scadenza.getStabilimento().getNome());
                row.createCell(2).setCellValue(scadenza.getTipoScadenza() != null ? scadenza.getTipoScadenza().toString() : "");
                row.createCell(3).setCellValue(scadenza.getDataScadenza().format(DATE_FORMATTER));
                row.createCell(4).setCellValue(scadenza.getPriorita() != null ? scadenza.getPriorita().toString() : "");
                row.createCell(5).setCellValue(scadenza.getStato() != null ? scadenza.getStato().toString() : "");
                row.createCell(6).setCellValue(scadenza.getResponsabile() != null ? scadenza.getResponsabile() : "");
                row.createCell(7).setCellValue(scadenza.getNote() != null ? scadenza.getNote() : "");

                for (int i = 0; i < columns.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportUtenti(List<User> utentiList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Utenti");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Username", "Nome", "Cognome", "Email", "Ruolo", "Attivo"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (User utente : utentiList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(utente.getId());
                row.createCell(1).setCellValue(utente.getUsername());
                row.createCell(2).setCellValue(utente.getFullName());
                row.createCell(3).setCellValue(utente.getFullName());
//                row.createCell(4).setCellValue(utente.getEmail());
//                row.createCell(5).setCellValue(utente.getRuolo() != null ? utente.getRuolo().name() : "");
//                row.createCell(6).setCellValue(utente.is ? "Sì" : "No");

                for (int i = 0; i < columns.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
