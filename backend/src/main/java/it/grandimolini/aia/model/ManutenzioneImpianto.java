package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro manutenzioni impianti (Tabella 2.1.2 PMC).
 *
 * Traccia gli interventi di manutenzione ordinaria e straordinaria
 * su macchinari e impianti dello stabilimento, come previsto dal PMC.
 *
 * Esempi: filtri a maniche (E66, E91-E96), impianto depurazione SF1,
 * impianto Imhoff, contatori, deprimometri.
 */
@Data
@Entity
@Table(name = "manutenzioni_impianto")
public class ManutenzioneImpianto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    /** Punto di monitoraggio associato (nullable: non sempre ha un punto AIA) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitoraggio_id")
    private Monitoraggio monitoraggio;

    // ── Identificazione impianto ───────────────────────────────────────────────

    /**
     * Denominazione del macchinario/impianto oggetto di manutenzione.
     * (es. "Filtro a maniche E66", "Impianto depurazione SF1", "Contatore M3")
     */
    @Column(nullable = false, length = 300)
    private String macchinario;

    // ── Intervento ────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_intervento", nullable = false)
    private TipoIntervento tipoIntervento;

    @Column(name = "data_intervento", nullable = false)
    private LocalDate dataIntervento;

    @Column(name = "descrizione_intervento", columnDefinition = "TEXT", nullable = false)
    private String descrizioneIntervento;

    @Column(name = "criticita_riscontrate", columnDefinition = "TEXT")
    private String criticitaRiscontrate;

    @Enumerated(EnumType.STRING)
    @Column(name = "eseguita_da")
    private EseguitaDa eseguitaDa;

    /** Nome ditta esterna o operatore interno */
    @Column(name = "operatore", length = 200)
    private String operatore;

    // ── Prossima scadenza ─────────────────────────────────────────────────────

    @Column(name = "prossima_manutenzione")
    private LocalDate prossimaManutenzione;

    // ── Esito ─────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "esito")
    private EsitoManutenzione esito;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // ── Audit ──────────────────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum TipoIntervento {
        ORDINARIA,
        STRAORDINARIA,
        SOSTITUZIONE,
        TARATURA,
        ISPEZIONE,
        PULIZIA
    }

    public enum EseguitaDa {
        INTERNO,
        DITTA_ESTERNA,
        COSTRUTTORE,
        ARPA
    }

    public enum EsitoManutenzione {
        REGOLARE,
        ANOMALIA_RISOLTA,
        ANOMALIA_IN_CORSO,
        FUORI_SERVIZIO
    }
}
