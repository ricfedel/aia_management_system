package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.VoceProduzione;
import lombok.Data;

@Data
public class VoceProduzioneDTO {
    private Long id;
    private Long registroMensileId;
    private VoceProduzione.CategoriaVoce categoria;
    private String descrizione;
    private String codice;
    private Double quantita;
    private String unitaMisura;
    private Double quantitaAnnoPrecedente;
    private String note;
    private Integer sortOrder;
}
