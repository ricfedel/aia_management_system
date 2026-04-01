package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.ProcessoDocumento;
import it.grandimolini.aia.model.TaskProcesso;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProcessoDocumentoDTO {

    private Long id;
    private String codiceProcesso;
    private ProcessoDocumento.TipoProcesso tipoProcesso;
    private ProcessoDocumento.StatoProcesso stato;
    private String taskCorrente;
    private String avviatoDa;
    private String assegnatoA;
    private String note;
    private LocalDateTime dataAvvio;
    private LocalDateTime dataCompletamento;
    private LocalDateTime dataAggiornamento;

    // Definizione flusso custom (null se processo predefinito)
    private Long definizioneFlussoId;
    private String definizioneFlussoNome;

    // Info documento collegato
    private Long documentoId;
    private String documentoNome;
    private String documentoTipo;

    // Info stabilimento
    private Long stabilimentoId;
    private String stabilimentoNome;

    // Task list
    private List<TaskProcessoDTO> tasks;

    public static ProcessoDocumentoDTO fromEntity(ProcessoDocumento p) {
        ProcessoDocumentoDTO dto = new ProcessoDocumentoDTO();
        dto.setId(p.getId());
        dto.setCodiceProcesso(p.getCodiceProcesso());
        dto.setTipoProcesso(p.getTipoProcesso());
        dto.setStato(p.getStato());
        dto.setTaskCorrente(p.getTaskCorrente());
        dto.setAvviatoDa(p.getAvviatoDa());
        dto.setAssegnatoA(p.getAssegnatoA());
        dto.setNote(p.getNote());
        dto.setDataAvvio(p.getDataAvvio());
        dto.setDataCompletamento(p.getDataCompletamento());
        dto.setDataAggiornamento(p.getDataAggiornamento());

        if (p.getDefinizioneFlusso() != null) {
            dto.setDefinizioneFlussoId(p.getDefinizioneFlusso().getId());
            dto.setDefinizioneFlussoNome(p.getDefinizioneFlusso().getNome());
        }

        if (p.getDocumento() != null) {
            dto.setDocumentoId(p.getDocumento().getId());
            dto.setDocumentoNome(p.getDocumento().getNome());
            dto.setDocumentoTipo(p.getDocumento().getTipoDocumento() != null
                    ? p.getDocumento().getTipoDocumento().name() : null);
        }

        if (p.getStabilimento() != null) {
            dto.setStabilimentoId(p.getStabilimento().getId());
            dto.setStabilimentoNome(p.getStabilimento().getNome());
        }

        if (p.getTasks() != null) {
            dto.setTasks(p.getTasks().stream()
                    .map(TaskProcessoDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Data
    public static class TaskProcessoDTO {
        private Long id;
        private String taskIdBpmn;
        private String nomeTask;
        private TaskProcesso.TipoTask tipoTask;
        private TaskProcesso.StatoTask statoTask;
        private String assegnatoA;
        private String completatoDa;
        private String commento;
        private String esito;
        private LocalDateTime dataCreazione;
        private LocalDateTime dataCompletamento;
        private LocalDateTime dataScadenza;

        public static TaskProcessoDTO fromEntity(TaskProcesso t) {
            TaskProcessoDTO dto = new TaskProcessoDTO();
            dto.setId(t.getId());
            dto.setTaskIdBpmn(t.getTaskIdBpmn());
            dto.setNomeTask(t.getNomeTask());
            dto.setTipoTask(t.getTipoTask());
            dto.setStatoTask(t.getStatoTask());
            dto.setAssegnatoA(t.getAssegnatoA());
            dto.setCompletatoDa(t.getCompletatoDa());
            dto.setCommento(t.getCommento());
            dto.setEsito(t.getEsito());
            dto.setDataCreazione(t.getDataCreazione());
            dto.setDataCompletamento(t.getDataCompletamento());
            dto.setDataScadenza(t.getDataScadenza());
            return dto;
        }
    }
}
