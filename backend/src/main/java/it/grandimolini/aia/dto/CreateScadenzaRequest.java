package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Scadenza.Priorita;
import it.grandimolini.aia.model.Scadenza.StatoScadenza;
import it.grandimolini.aia.model.Scadenza.TipoScadenza;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateScadenzaRequest {

    private Long stabilimentoId;

    private Long prescrizioneId;

    private Long monitoraggioId;

    @NotBlank(message = "Il titolo è obbligatorio")
    @Size(max = 255, message = "Il titolo non può superare 255 caratteri")
    private String titolo;

    @Size(max = 1000, message = "La descrizione non può superare 1000 caratteri")
    private String descrizione;

    private TipoScadenza tipoScadenza;

    @NotNull(message = "La data di scadenza è obbligatoria")
    private LocalDate dataScadenza;

    private StatoScadenza stato = StatoScadenza.PENDING;

    private Priorita priorita;

    @Size(max = 255, message = "Il responsabile non può superare 255 caratteri")
    private String responsabile;

    @Email(message = "Email non valida")
    @Size(max = 255, message = "L'email non può superare 255 caratteri")
    private String emailNotifica;

    private Integer giorniPreavviso = 20;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri")
    private String note;
}
