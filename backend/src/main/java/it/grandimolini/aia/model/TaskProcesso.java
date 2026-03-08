package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Task (step) di un processo BPM.
 * Corrisponde a uno UserTask o ServiceTask nel modello BPMN.
 */
@Data
@Entity
@Table(name = "task_processo")
public class TaskProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoDocumento processo;

    /** Identificatore del nodo nel diagramma BPMN (es. "Task_RevisioneMetadati") */
    @Column(name = "task_id_bpmn", nullable = false, length = 100)
    private String taskIdBpmn;

    /** Nome leggibile del task */
    @Column(name = "nome_task", nullable = false, length = 200)
    private String nomeTask;

    /** Tipo di task */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_task", nullable = false)
    private TipoTask tipoTask;

    /** Stato corrente del task */
    @Enumerated(EnumType.STRING)
    @Column(name = "stato_task", nullable = false)
    private StatoTask statoTask = StatoTask.CREATO;

    /** Utente a cui è assegnato il task */
    @Column(name = "assegnato_a", length = 100)
    private String assegnatoA;

    /** Utente che ha completato il task */
    @Column(name = "completato_da", length = 100)
    private String completatoDa;

    /** Commento inserito al completamento */
    @Column(name = "commento", length = 2000)
    private String commento;

    /** Esito del task (APPROVATO / RIFIUTATO / etc.) */
    @Column(name = "esito", length = 50)
    private String esito;

    /** Dati del form compilato (JSON) */
    @Column(name = "form_data_json", columnDefinition = "TEXT")
    private String formDataJson;

    // ─── Campi service task ───────────────────────────────────────────────────

    /** Configurazione API call (JSON serializzato: url, method, body, headers) */
    @Column(name = "service_call_config", columnDefinition = "TEXT")
    private String serviceCallConfig;

    /** HTTP status code restituito dalla chiamata API */
    @Column(name = "api_status_code")
    private Integer apiStatusCode;

    /** Corpo della risposta HTTP (max 4KB) */
    @Column(name = "api_risposta", columnDefinition = "TEXT")
    private String apiRisposta;

    /** Messaggio di errore se la chiamata API è fallita */
    @Column(name = "api_errore", length = 1000)
    private String apiErrore;

    /** Configurazione AutoTask unificata (JSON) — per i nuovi tipi di step */
    @Column(name = "auto_task_config", columnDefinition = "TEXT")
    private String autoTaskConfig;

    /** Data/ora programmata per step TIMER */
    @Column(name = "data_esecuzione_programmata")
    private LocalDateTime dataEsecuzioneProgrammata;

    // ─── Campi gateway AND (parallelo) e sub-processo ────────────────────────

    /**
     * ID del gateway AND di fork a cui appartiene questo task.
     * Quando non null, il task fa parte di un ramo parallelo.
     * Il completamento richiede che tutti i task con lo stesso parallelGroupId siano completati.
     */
    @Column(name = "parallel_group_id", length = 100)
    private String parallelGroupId;

    /**
     * ID del gateway AND di join a cui convergono i rami paralleli.
     * Quando tutti i task del gruppo sono completati, il BPM engine riparte da qui.
     */
    @Column(name = "join_gateway_id", length = 100)
    private String joinGatewayId;

    /**
     * Per task di tipo CALL_ACTIVITY: nome/id della DefinizioneFlusso figlia.
     * Corrisponde all'attributo calledElement nel BPMN.
     */
    @Column(name = "called_element", length = 200)
    private String calledElement;

    /**
     * Per task di tipo CALL_ACTIVITY: id del processo figlio avviato.
     * Quando il sotto-processo completa, questo task viene chiuso automaticamente.
     */
    @Column(name = "child_processo_id")
    private Long childProcessoId;

    @Column(name = "data_creazione", nullable = false)
    private LocalDateTime dataCreazione;

    @Column(name = "data_completamento")
    private LocalDateTime dataCompletamento;

    @Column(name = "data_scadenza")
    private LocalDateTime dataScadenza;

    @PrePersist
    protected void onCreate() {
        dataCreazione = LocalDateTime.now();
    }

    // ─── Enumerations ────────────────────────────────────────────────────────

    public enum TipoTask {
        USER_TASK,          // Richiede intervento umano
        SERVICE_TASK,       // Eseguito automaticamente
        GATEWAY,            // Punto di decisione (legacy)
        CALL_ACTIVITY       // Riferimento a sotto-processo (callActivity BPMN)
    }

    public enum StatoTask {
        CREATO,
        IN_CORSO,
        COMPLETATO,
        SALTATO,
        ANNULLATO
    }
}
