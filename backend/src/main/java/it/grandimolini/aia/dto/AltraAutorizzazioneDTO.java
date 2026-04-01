package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.AltraAutorizzazione.TipoAltraAutorizzazione;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AltraAutorizzazioneDTO {
    private Long id;
    private Long stabilimentoId;
    private String stabilimentoNome;
    private TipoAltraAutorizzazione tipo;
    private String numeroAutorizzazione;
    private String enteCompetente;
    private LocalDate dataRilascio;
    private LocalDate dataScadenza;
    private String descrizione;
}
