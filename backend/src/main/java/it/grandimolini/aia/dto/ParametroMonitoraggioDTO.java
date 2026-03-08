package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Monitoraggio.FrequenzaMonitoraggio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ParametroMonitoraggioDTO {
    private Long id;
    private Long monitoraggioId;
    private String nome;
    private String codice;
    private String unitaMisura;
    private Double limiteValore;
    private String limiteUnita;
    private String limiteRiferimento;
    private FrequenzaMonitoraggio frequenza;
    private String metodoAnalisi;
    private String note;
    private Boolean attivo;
    private LocalDateTime createdAt;
}
