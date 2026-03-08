package it.grandimolini.aia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    // Statistiche generali
    private Long totalStabilimenti;
    private Long stabilimentiAttivi;

    // Prescrizioni
    private Long totalPrescrizioni;
    private Map<String, Long> prescrizioniPerStato;

    // Scadenze
    private Long scadenzeImminenti;
    private Long scadenzeScadute;
    private Long scadenzeCompletate;

    // Dati ambientali
    private Long totalDatiAmbientali;
    private Long datiNonConformi;
    private Long datiInAttenzione;
    private Double percentualeConformita;
}
