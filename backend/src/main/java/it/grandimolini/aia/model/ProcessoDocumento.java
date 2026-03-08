package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Istanza di processo BPM associata a un documento.
 * Modella il ciclo di vita di un documento dall'arrivo alla creazione
 * delle entità operative (scadenze, prescrizioni) nel sistema AIA.
 */
@Data
@Entity
@Table(name = "processi_documento")
public class ProcessoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Chiave di processo leggibile (es. PROC-2026-00042) */
    @Column(name = "codice_processo", unique = true, nullable = false, length = 30)
    private String codiceProcesso;

    /** Tipo di processo BPMN avviato */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_processo", nullable = false)
    private TipoProcesso tipoProcesso;

    /** Stato corrente dell'istanza di processo */
    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false)
    private StatoProcesso stato = StatoProcesso.AVVIATO;

    /** Documento che ha originato il processo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    /** Stabilimento di riferimento */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stabilimento_id")
    private Stabilimento stabilimento;

    /**
     * Definizione di flusso custom usata per creare questo processo.
     * Null se il processo è stato avviato con un tipo predefinito hardcoded.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definizione_flusso_id")
    private DefinizioneFlusso definizioneFlusso;

    /** Task corrente attivo nel processo */
    @Column(name = "task_corrente", length = 100)
    private String taskCorrente;

    /** Utente che ha avviato il processo */
    @Column(name = "avviato_da", length = 100)
    private String avviatoDa;

    /** Utente attualmente assegnatario del task corrente */
    @Column(name = "assegnato_a", length = 100)
    private String assegnatoA;

    /** Note libere sul processo */
    @Column(name = "note", length = 2000)
    private String note;

    /** Variabili di processo serializzate in JSON */
    @Column(name = "variabili_json", length = 4000, columnDefinition = "TEXT")
    private String variabiliJson;

    @Column(name = "data_avvio", nullable = false)
    private LocalDateTime dataAvvio;

    @Column(name = "data_completamento")
    private LocalDateTime dataCompletamento;

    @Column(name = "data_aggiornamento")
    private LocalDateTime dataAggiornamento;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataCreazione ASC")
    private List<TaskProcesso> tasks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataAvvio = LocalDateTime.now();
        dataAggiornamento = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAggiornamento = LocalDateTime.now();
    }

    // ─── Enumerations ────────────────────────────────────────────────────────

    public enum TipoProcesso {
        LAVORAZIONE_DOCUMENTO,
        RINNOVO_AIA,
        NON_CONFORMITA,
        INTEGRAZIONE_ENTE
    }

    public enum StatoProcesso {
        AVVIATO,
        IN_CORSO,
        IN_ATTESA,
        COMPLETATO,
        ANNULLATO,
        SOSPESO
    }
}
