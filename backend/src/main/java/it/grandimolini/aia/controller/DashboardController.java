package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.ConformitaTrendDTO;
import it.grandimolini.aia.dto.DashboardStatsDTO;
import it.grandimolini.aia.dto.ScadenzaImminenteDTO;
import it.grandimolini.aia.dto.StabilimentoStatsDTO;
import it.grandimolini.aia.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Ottiene statistiche generali dashboard
     * Accessibile a tutti gli utenti autenticati
     * I dati sono filtrati in base ai permessi dell'utente
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Ottiene statistiche per uno specifico stabilimento
     * Richiede accesso allo stabilimento
     */
    @GetMapping("/stabilimento/{stabilimentoId}/stats")
    @PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
    public ResponseEntity<StabilimentoStatsDTO> getStabilimentoStats(@PathVariable Long stabilimentoId) {
        StabilimentoStatsDTO stats = dashboardService.getStabilimentoStats(stabilimentoId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Ottiene lista scadenze imminenti (prossimi N giorni)
     * Accessibile a tutti gli utenti autenticati
     * I dati sono filtrati in base ai permessi dell'utente
     */
    @GetMapping("/scadenze-imminenti")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScadenzaImminenteDTO>> getScadenzeImminenti(
            @RequestParam(defaultValue = "30") int giorni) {

        List<ScadenzaImminenteDTO> scadenze = dashboardService.getScadenzeImminenti(giorni);
        return ResponseEntity.ok(scadenze);
    }

    /**
     * Ottiene trend conformità ambientale degli ultimi N mesi
     * Accessibile a tutti gli utenti autenticati
     * I dati sono filtrati in base ai permessi dell'utente
     */
    @GetMapping("/conformita-trend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConformitaTrendDTO>> getConformitaTrend(
            @RequestParam(defaultValue = "12") int mesi) {

        List<ConformitaTrendDTO> trend = dashboardService.getConformitaTrend(mesi);
        return ResponseEntity.ok(trend);
    }
}
