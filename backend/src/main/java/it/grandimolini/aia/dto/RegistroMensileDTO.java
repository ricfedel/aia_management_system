package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.RegistroMensile;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegistroMensileDTO {
    private Long id;
    private Long stabilimentoId;
    private String stabilimentoNome;
    private Integer anno;
    private Integer mese;
    private RegistroMensile.StatoRegistro stato;
    private String note;
    private String compilatoDa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VoceProduzioneDTO> voci;
}
