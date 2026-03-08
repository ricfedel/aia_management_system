package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Scadenza.Priorita;
import it.grandimolini.aia.model.Scadenza.StatoScadenza;
import it.grandimolini.aia.model.Scadenza.TipoScadenza;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScadenzaImminenteDTO {

    private Long id;
    private String titolo;
    private LocalDate dataScadenza;
    private Integer giorniRimanenti;
    private TipoScadenza tipoScadenza;
    private StatoScadenza stato;
    private Priorita priorita;
    private String stabilimentoNome;
    private String responsabile;
}
