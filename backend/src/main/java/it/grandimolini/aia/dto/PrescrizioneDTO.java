package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Prescrizione.MatriceAmbientale;
import it.grandimolini.aia.model.Prescrizione.Priorita;
import it.grandimolini.aia.model.Prescrizione.StatoPrescrizione;
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
public class PrescrizioneDTO {
    private Long id;
    private Long stabilimentoId;
    private String stabilimentoNome;
    private String codice;
    private String descrizione;
    private MatriceAmbientale matriceAmbientale;
    private StatoPrescrizione stato;
    private LocalDate dataEmissione;
    private LocalDate dataScadenza;
    private String enteEmittente;
    private String riferimentoNormativo;
    private Priorita priorita;
    private String note;
    private LocalDate dataChiusura;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
