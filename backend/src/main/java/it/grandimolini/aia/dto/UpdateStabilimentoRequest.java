package it.grandimolini.aia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateStabilimentoRequest {

    @Size(max = 255, message = "Il nome non può superare 255 caratteri")
    private String nome;

    @Size(max = 100, message = "La città non può superare 100 caratteri")
    private String citta;

    @Size(max = 255, message = "L'indirizzo non può superare 255 caratteri")
    private String indirizzo;

    @Size(max = 100, message = "Il numero AIA non può superare 100 caratteri")
    private String numeroAIA;

    private LocalDate dataRilascioAIA;

    private LocalDate dataScadenzaAIA;

    @Size(max = 255, message = "L'ente competente non può superare 255 caratteri")
    private String enteCompetente;

    @Size(max = 255, message = "Il responsabile ambientale non può superare 255 caratteri")
    private String responsabileAmbientale;

    @Email(message = "Email non valida")
    @Size(max = 255, message = "L'email non può superare 255 caratteri")
    private String email;

    @Size(max = 20, message = "Il telefono non può superare 20 caratteri")
    private String telefono;

    private Boolean attivo;
}
