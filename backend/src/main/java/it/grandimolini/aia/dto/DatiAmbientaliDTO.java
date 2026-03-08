package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.DatiAmbientali.StatoConformita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatiAmbientaliDTO {
    private Long id;
    private Long monitoraggioId;
    private LocalDate dataCampionamento;
    private String parametro;
    private Double valoreMisurato;
    private String unitaMisura;
    private Double limiteAutorizzato;
    private StatoConformita statoConformita;
    private String rapportoProva;
    private String laboratorio;
    private String note;
    private LocalDateTime createdAt;
}
