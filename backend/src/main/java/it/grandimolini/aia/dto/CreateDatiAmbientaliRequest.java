package it.grandimolini.aia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDatiAmbientaliRequest {

    @NotNull(message = "Il monitoraggio è obbligatorio")
    private Long monitoraggioId;

    @NotNull(message = "La data di campionamento è obbligatoria")
    private LocalDate dataCampionamento;

    @NotBlank(message = "Il parametro è obbligatorio")
    @Size(max = 255, message = "Il parametro non può superare 255 caratteri")
    private String parametro;

    private Double valoreMisurato;

    @Size(max = 50, message = "L'unità di misura non può superare 50 caratteri")
    private String unitaMisura;

    private Double limiteAutorizzato;

    @Size(max = 255, message = "Il rapporto di prova non può superare 255 caratteri")
    private String rapportoProva;

    @Size(max = 255, message = "Il laboratorio non può superare 255 caratteri")
    private String laboratorio;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri")
    private String note;
}
