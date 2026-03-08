package it.grandimolini.aia.controller;

import it.grandimolini.aia.service.Allegato2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/allegato2")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class Allegato2Controller {

    private final Allegato2Service service;

    /**
     * Genera e scarica l'Allegato 2 – Sintesi PMC in formato Excel (.xlsx).
     * GET /api/allegato2/xlsx?stabilimentoId=1&anno=2025
     */
    @GetMapping("/xlsx")
    public ResponseEntity<byte[]> downloadAllegato2(
            @RequestParam Long stabilimentoId,
            @RequestParam int  anno) throws IOException {

        byte[] xlsx = service.generate(stabilimentoId, anno);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "Allegato2_PMC_" + anno + ".xlsx");

        return ResponseEntity.ok().headers(headers).body(xlsx);
    }
}
