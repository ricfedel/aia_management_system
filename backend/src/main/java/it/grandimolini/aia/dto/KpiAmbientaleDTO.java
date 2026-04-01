package it.grandimolini.aia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiAmbientaleDTO {

    private Long stabilimentoId;
    private String stabilimentoNome;
    private Integer annoRiferimento;

    // Consumi assoluti (anno corrente)
    private Double consumoIdrico;          // m³
    private Double consumoEletrico;        // kWh
    private Double consumoGas;            // Nm³
    private Double produzioneTotale;       // t (materia prima lavorata)

    // Consumi specifici (per tonnellata lavorata)
    private Double consumoIdricoSpecifico;    // m³/t
    private Double consumoEletricoSpecifico;  // kWh/t
    private Double consumoGasSpecifico;       // Nm³/t

    // Rifiuti (anno corrente, movimenti PRODUZIONE + DEPOSITO_TEMPORANEO)
    private Double rifiutiTotali;          // t o m³ (unità miste → kg sommati)
    private Double rifiutiPericolosi;      // t o m³

    // Limiti normativi deposito temporaneo
    private Double limiteRifiutiTotali;    // 30 m³ (fisso normativa)
    private Double limiteRifiutiPericolosi;// 10 m³ (fisso normativa)

    // Semafori (true = in limite, false = superato)
    private Boolean rifiutiTotaliOk;
    private Boolean rifiutiPericolosiOk;
}
