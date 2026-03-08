package it.grandimolini.aia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StabilimentoStatsDTO {

    private Long stabilimentoId;
    private String stabilimentoNome;

    // Prescrizioni
    private Long prescrizioniAperte;
    private Long prescrizioniChiuse;
    private Long prescrizioniUrgenti;

    // Scadenze
    private Long scadenzeImminenti;
    private Long scadenzeScadute;

    // Dati ambientali
    private Long totalDatiAmbientali;
    private Long datiNonConformi;
    private Double percentualeConformita;

    // Monitoraggi
    private Long totalMonitoraggi;
    private Long monitoraggiAttivi;
}
