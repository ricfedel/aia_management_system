package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.RilevazioneMisura;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RilevazioneMisuraDTO {
    private Long id;
    private Long parametroMonitoraggioId;
    private String parametroNome;
    private String parametroCodice;
    private String parametroUnitaMisura;
    private Double parametroLimiteValore;
    private String parametroLimiteRiferimento;
    // Punto di monitoraggio
    private Long monitoraggioId;
    private String monitoraggioCodice;
    private String monitoraggioDescrizione;
    private String monitoraggioTipo;
    // Stabilimento
    private Long stabilimentoId;
    private String stabilimentoNome;
    // Misura
    private LocalDate dataCampionamento;
    private Double valoreMisurato;
    private String unitaMisura;
    private RilevazioneMisura.StatoConformita statoConformita;
    private String rapportoProva;
    private String laboratorio;
    private String note;
    private LocalDateTime createdAt;
}
