package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.RelazioneAnnualeDTO;
import it.grandimolini.aia.service.RelazioneAnnualeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/relazione-annuale")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RelazioneAnnualeController {

    private final RelazioneAnnualeService service;

    /**
     * Restituisce i dati aggregati in formato JSON (anteprima).
     * GET /api/relazione-annuale/preview?stabilimentoId=1&anno=2025
     */
    @GetMapping("/preview")
    public ResponseEntity<RelazioneAnnualeDTO> preview(
            @RequestParam Long stabilimentoId,
            @RequestParam int  anno) {
        RelazioneAnnualeDTO dto = service.buildPreview(stabilimentoId, anno);
        return ResponseEntity.ok(dto);
    }

    /**
     * Genera e scarica il file .docx della Relazione Annuale AIA.
     * GET /api/relazione-annuale/docx?stabilimentoId=1&anno=2025
     */
    @GetMapping("/docx")
    public ResponseEntity<byte[]> downloadDocx(
            @RequestParam Long stabilimentoId,
            @RequestParam int  anno) throws IOException {

        byte[] docx = service.generateDocx(stabilimentoId, anno);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "Relazione_Annuale_AIA_" + anno + ".docx");

        return ResponseEntity.ok().headers(headers).body(docx);
    }
}
