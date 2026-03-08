package it.grandimolini.aia.controller;

import it.grandimolini.aia.service.ExcelExportService;
import it.grandimolini.aia.service.RelazioneAnnualeService;
import it.grandimolini.aia.service.DatiAmbientaliService;
import it.grandimolini.aia.service.PrescrizioneService;
import it.grandimolini.aia.service.ScadenzaService;
import it.grandimolini.aia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private RelazioneAnnualeService relazioneAnnualeService;

    @Autowired
    private DatiAmbientaliService datiAmbientaliService;

    @Autowired
    private PrescrizioneService prescrizioneService;

    @Autowired
    private ScadenzaService scadenzaService;

    @Autowired
    private UserService userService;

    @GetMapping("/dati-ambientali/excel")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<byte[]> exportDatiAmbientaliExcel() throws IOException {
        byte[] excelFile = excelExportService.exportDatiAmbientali(datiAmbientaliService.findAll());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "dati-ambientali.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    @GetMapping("/prescrizioni/excel")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<byte[]> exportPrescrizioniExcel() throws IOException {
        byte[] excelFile = excelExportService.exportPrescrizioni(prescrizioneService.findAll());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "prescrizioni.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    @GetMapping("/scadenze/excel")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<byte[]> exportScadenzeExcel() throws IOException {
        byte[] excelFile = excelExportService.exportScadenze(scadenzaService.findAll());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "scadenze.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    @GetMapping("/dati-ambientali/stabilimento/{stabilimentoId}/anno/{anno}/excel")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<byte[]> exportDatiByStabilimentoExcel(
            @PathVariable Long stabilimentoId,
            @PathVariable int anno) throws IOException {
        byte[] excelFile = excelExportService.exportDatiAmbientali(
                datiAmbientaliService.findByStabilimentoAndAnno(stabilimentoId, anno));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "dati-ambientali-stabilimento-" + stabilimentoId + "-" + anno + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    @GetMapping("/relazione-annuale/{stabilimentoId}/anno/{anno}")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<byte[]> exportRelazioneAnnuale(
            @PathVariable Long stabilimentoId,
            @PathVariable int anno) throws IOException {
        byte[] docxFile = relazioneAnnualeService.generateDocx(stabilimentoId, anno);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "relazione-annuale-AIA-" + anno + ".docx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(docxFile);
    }

    @GetMapping("/utenti/excel")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<byte[]> exportUtentiExcel() throws IOException {
        byte[] excelFile = excelExportService.exportUtenti(userService.findAll());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "utenti.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }
}
