package it.grandimolini.aia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StabilimentoDTO {
    private Long id;
    private String nome;
    private String citta;
    private String indirizzo;
    private String numeroAIA;
    private LocalDate dataRilascioAIA;
    private LocalDate dataScadenzaAIA;
    private String enteCompetente;
    private String responsabileAmbientale;
    private String email;
    private String telefono;
    private Boolean attivo;
}
