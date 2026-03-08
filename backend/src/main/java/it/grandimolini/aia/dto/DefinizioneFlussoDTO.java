package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.DefinizioneFlusso;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DefinizioneFlussoDTO {

    private Long id;
    private String nome;
    private String descrizione;
    private String bpmnXml;
    private Integer versione;
    private Boolean attiva;
    private Boolean sistema;
    private String creatoDa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Anteprima degli step estratti dal parser (null se non richiesta) */
    private List<StepPreview> steps;

    public static DefinizioneFlussoDTO fromEntity(DefinizioneFlusso e) {
        DefinizioneFlussoDTO dto = new DefinizioneFlussoDTO();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setDescrizione(e.getDescrizione());
        dto.setBpmnXml(e.getBpmnXml());
        dto.setVersione(e.getVersione());
        dto.setAttiva(e.getAttiva());
        dto.setSistema(e.getSistema() != null && e.getSistema());
        dto.setCreatoDa(e.getCreatoDa());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    /** Anteprima leggera di uno step BPMN parsato */
    @Data
    public static class StepPreview {
        private String id;
        private String nome;
        private String tipoTask;   // USER_TASK | SERVICE_TASK | GATEWAY
        private int ordine;
    }

    // ─── Request DTO per la creazione/aggiornamento ─────────────────────────

    @Data
    public static class SaveRequest {
        private String nome;
        private String descrizione;
        private String bpmnXml;
    }
}
