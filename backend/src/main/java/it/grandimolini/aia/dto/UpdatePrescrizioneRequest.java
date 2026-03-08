package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Prescrizione.MatriceAmbientale;
import it.grandimolini.aia.model.Prescrizione.Priorita;
import it.grandimolini.aia.model.Prescrizione.StatoPrescrizione;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePrescrizioneRequest {

    @Size(max = 100, message = "Il codice non può superare 100 caratteri")
    private String codice;

    @Size(max = 1000, message = "La descrizione non può superare 1000 caratteri")
    private String descrizione;

    private MatriceAmbientale matriceAmbientale;

    private StatoPrescrizione stato;

    private LocalDate dataEmissione;

    private LocalDate dataScadenza;

    @Size(max = 255, message = "L'ente emittente non può superare 255 caratteri")
    private String enteEmittente;

    @Size(max = 500, message = "Il riferimento normativo non può superare 500 caratteri")
    private String riferimentoNormativo;

    private Priorita priorita;

    @Size(max = 2000, message = "Le note non possono superare 2000 caratteri")
    private String note;

    private LocalDate dataChiusura;
}
