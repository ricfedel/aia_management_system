package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Scadenza.Priorita;
import it.grandimolini.aia.model.Scadenza.StatoScadenza;
import it.grandimolini.aia.model.Scadenza.TipoScadenza;
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
public class ScadenzaDTO {
    private Long id;
    private Long stabilimentoId;
    private String stabilimentoNome;
    private Long prescrizioneId;
    private Long monitoraggioId;
    private String titolo;
    private String descrizione;
    private TipoScadenza tipoScadenza;
    private LocalDate dataScadenza;
    private StatoScadenza stato;
    private Priorita priorita;
    private String responsabile;
    private String emailNotifica;
    private Integer giorniPreavviso;
    private LocalDate dataCompletamento;
    private String note;
    private LocalDate dataPrevistaAttivazione;
    private String riferimento;
    private String sitoOrigine;
    private LocalDateTime createdAt;
}
