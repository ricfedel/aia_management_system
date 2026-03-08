package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.AnagraficaCamino.FaseProcesso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnagraficaCaminoDTO {

    private Long id;
    private Long stabilimentoId;
    private String stabilimentoNome;

    private String sigla;
    private FaseProcesso faseProcesso;
    private String origine;

    private Double portataNomc3h;
    private Double sezioneM2;
    private Double velocitaMs;
    private Double temperaturaC;
    private Boolean temperaturaAmbiente;
    private Double altezzaM;
    private Integer durataHGiorno;
    private Integer durataGAnno;

    private String impiantoAbbattimento;
    private String note;
    private Boolean attivo;
    private LocalDateTime createdAt;
}
