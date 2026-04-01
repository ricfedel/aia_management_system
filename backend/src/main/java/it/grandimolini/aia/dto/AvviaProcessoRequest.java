package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.ProcessoDocumento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvviaProcessoRequest {

    @NotNull
    private Long documentoId;

    /**
     * Tipo processo predefinito. Obbligatorio se definizioneFlussoId è null.
     * Ignorato se definizioneFlussoId è valorizzato.
     */
    private ProcessoDocumento.TipoProcesso tipoProcesso;

    /**
     * ID della DefinizioneFlusso custom (disegnata con l'editor BPMN).
     * Se valorizzato, tipoProcesso viene ignorato e gli step sono estratti
     * dinamicamente dal BPMN XML.
     */
    private Long definizioneFlussoId;

    private Long stabilimentoId;

    private String assegnatoA;

    private String note;
}
