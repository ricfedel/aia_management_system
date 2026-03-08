package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Controllo operativo di processo (Tabelle 2.1.1, 2.1.3, 2.1.4 PMC).
 *
 * Registra i controlli gestionali sui sistemi di abbattimento e depurazione:
 * - Tabella 2.1.1: fasi critiche del processo produttivo
 * - Tabella 2.1.3: parametri di esercizio sistemi abbattimento polveri (es. ΔP filtri a maniche)
 * - Tabella 2.1.4: parametri di esercizio sistemi depurazione acque (es. portata, livello Imhoff)
 *
 * Nota: per i camini E66/E91-E96 il PMC richiede controllo mensile del ΔP (deprimometri)
 * in sostituzione dell'analisi analitica annuale. Questa è una ControlloProcesso
 * con parametro "Delta P" e frequenza MENSILE.
 */
@Data
@Entity
@Table(name = "controlli_processo")
public class ControlloProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    /** Punto di monitoraggio associato (nullable per tab 2.1.1 fasi critiche) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitoraggio_id")
    private Monitoraggio monitoraggio;

    // ── Classificazione ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tabella", nullable = false)
    private TipoTabella tipoTabella;

    /** Fase del processo produttivo (es. "Prepulitura", "Filtratura aria compressa") */
    @Column(name = "fase_processo", length = 200)
    private String faseProcesso;

    /** Sistema controllato (es. "Filtro a maniche E66", "Impianto Imhoff SF1") */
    @Column(name = "sistema", length = 200)
    private String sistema;

    // ── Misura ────────────────────────────────────────────────────────────────

    /** Parametro controllato (es. "Delta P", "portata effluente", "pH ingresso") */
    @Column(name = "parametro_controllo", length = 150, nullable = false)
    private String parametroControllo;

    @Column(name = "unita_misura", length = 30)
    private String unitaMisura;

    /** Valore rilevato durante il controllo */
    @Column(name = "risultato_controllo")
    private Double risultatoControllo;

    /** Rappresentazione testuale del risultato (per valori non numerici) */
    @Column(name = "risultato_testo", length = 100)
    private String risultatoTesto;

    /** Valore di soglia di allarme (es. ΔP < 100 Pa = intasamento filtro) */
    @Column(name = "soglia_allarme")
    private Double sogliaAllarme;

    // ── Esito ─────────────────────────────────────────────────────────────────

    @Column(name = "data_controllo", nullable = false)
    private LocalDate dataControllo;

    @Column(name = "conforme")
    private Boolean conforme;

    @Column(name = "commenti", columnDefinition = "TEXT")
    private String commenti;

    // ── Operatore ─────────────────────────────────────────────────────────────

    @Column(name = "operatore", length = 100)
    private String operatore;

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

    public enum TipoTabella {
        FASE_CRITICA,           // Tab 2.1.1 – fasi critiche processo produttivo
        SISTEMA_ABBATTIMENTO,   // Tab 2.1.3 – parametri sistemi abbattimento polveri
        SISTEMA_DEPURAZIONE     // Tab 2.1.4 – parametri sistemi depurazione acque
    }
}
