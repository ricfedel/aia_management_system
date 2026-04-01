package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.CodiceRifiuto;
import lombok.Data;

@Data
public class CodiceRifiutoDTO {
    private Long id;
    private Long stabilimentoId;
    private String stabilimentoNome;
    private String codiceCer;
    private String descrizione;
    private Boolean pericoloso;
    private CodiceRifiuto.StatoFisicoRifiuto statoFisico;
    private String unitaMisura;
    private String codiceGestione;
    private String destinatarioAbituale;
    private String note;
    private Boolean attivo;
}
