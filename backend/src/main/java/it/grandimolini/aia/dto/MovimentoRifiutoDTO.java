package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.MovimentoRifiuto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MovimentoRifiutoDTO {
    private Long id;
    private Long codiceRifiutoId;
    private String codiceCer;
    private String descrizioneRifiuto;
    private Boolean pericoloso;
    private Integer anno;
    private Integer mese;
    private MovimentoRifiuto.TipoMovimento tipoMovimento;
    private Double quantita;
    private String unitaMisura;
    private String codiceOperazione;
    private String destinatario;
    private String trasportatore;
    private String numeroFir;
    private LocalDate dataOperazione;
    private String note;
}
