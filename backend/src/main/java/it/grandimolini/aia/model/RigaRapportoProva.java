package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * Singola riga analitica di un {@link RapportoProva}.
 *
 * Corrisponde a un parametro misurato (es. pH, BOD5, COD, SST, idrocarburi totali…)
 * con il proprio valore, incertezza, LOQ, limite normativo e stato di conformità.
 */
@Data
@Entity
@Table(name = "righe_rapporto_prova")
public class RigaRapportoProva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relazione ─────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rapporto_prova_id", nullable = false)
    private RapportoProva rapportoProva;

    /** Ordine della riga nel rapporto originale */
    @Column(name = "riga_numero")
    private Integer rigaNumero;

    // ── Parametro ─────────────────────────────────────────────────────────────

    /** Nome del parametro (es. "pH", "BOD5", "Materiali in sospensione") */
    @Column(nullable = false, length = 200)
    private String parametro;

    // ── Valori misurati ────────────────────────────────────────────────────────

    /**
     * Valore+incertezza come stringa originale (es. "7,10±0,08", "<5", ">1000").
     * Conserva la rappresentazione esatta del laboratorio.
     */
    @Column(name = "valore_testo", length = 100)
    private String valoreTesto;

    /** Valore numerico estratto (null se sotto LOQ o non quantificabile) */
    @Column(name = "valore_numerico")
    private Double valoreNumerico;

    /** Incertezza estesa (es. 0.08 per pH = 7.10±0.08) */
    @Column(name = "incertezza")
    private Double incertezza;

    /** Unità di misura (es. "mg/l", "upH", "UFC/100ml", "µg/l", "mg/l(comeO2)") */
    @Column(name = "unita_misura", length = 50)
    private String unitaMisura;

    // ── Limiti normativi ───────────────────────────────────────────────────────

    /** Riferimento normativo (es. "DL 152/06 TAB3 SUP", "AIA Decreto 19082/2024") */
    @Column(name = "valore_riferimento", length = 200)
    private String valoreRiferimento;

    /** Limite come stringa originale (es. "<=80", "[5.5-9.5]", "<=40") */
    @Column(name = "limite_testo", length = 100)
    private String limiteTesto;

    /** Limite numerico (valore massimo; null se non applicabile o intervallo) */
    @Column(name = "limite_numerico")
    private Double limiteNumerico;

    /** Limite minimo (per parametri con intervallo, es. pH min = 5.5) */
    @Column(name = "limite_minimo")
    private Double limiteMinimo;

    // ── LOQ / RL ──────────────────────────────────────────────────────────────

    /** Limite di quantificazione (LOQ) del metodo (es. 5.0 mg/l per SST) */
    @Column(name = "loq")
    private Double loq;

    /** Limite di rilevazione specifico del campione (RL = Reporting Limit) */
    @Column(name = "rl")
    private Double rl;

    // ── Metodo e date analisi ──────────────────────────────────────────────────

    /** Metodo analitico (es. "APAT CNR IRSA 2090 B Man 29 2003") */
    @Column(name = "metodo_analisi", length = 300)
    private String metodoAnalisi;

    @Column(name = "data_inizio_analisi")
    private LocalDate dataInizioAnalisi;

    @Column(name = "data_fine_analisi")
    private LocalDate dataFineAnalisi;

    /** Unità operativa del laboratorio che ha eseguito l'analisi (es. "02") */
    @Column(name = "unita_operativa", length = 50)
    private String unitaOperativa;

    // ── Conformità e discostamento ─────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_conformita")
    private StatoConformita statoConformita;

    /**
     * Discostamento percentuale dal limite: (valore_numerico / limite_numerico) * 100.
     * Calcolato automaticamente al salvataggio.
     * - Valore ≤ 100% = conforme
     * - Valore > 100% = non conforme
     * - Null se sotto LOQ o limite non numerico
     */
    @Column(name = "discostamento_percentuale")
    private Double discostamentoPercentuale;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    @PreUpdate
    protected void calcolaConformitaEDiscostamento() {
        // Calcola discostamento % e stato conformità automaticamente
        if (valoreNumerico == null) {
            // Valore sotto LOQ: non quantificabile, presunto conforme
            if (valoreTesto != null && (valoreTesto.startsWith("<") || valoreTesto.startsWith("n.r"))) {
                statoConformita = StatoConformita.SOTTO_LOQ;
                discostamentoPercentuale = null;
            }
            return;
        }

        if (limiteNumerico != null && limiteNumerico > 0) {
            discostamentoPercentuale = (valoreNumerico / limiteNumerico) * 100.0;
            if (discostamentoPercentuale <= 80.0) {
                statoConformita = StatoConformita.CONFORME;
            } else if (discostamentoPercentuale <= 100.0) {
                statoConformita = StatoConformita.ATTENZIONE;
            } else {
                statoConformita = StatoConformita.NON_CONFORME;
            }
        } else if (limiteMinimo != null && limiteNumerico != null) {
            // Parametro con intervallo (es. pH 5.5-9.5)
            if (valoreNumerico >= limiteMinimo && valoreNumerico <= limiteNumerico) {
                statoConformita = StatoConformita.CONFORME;
            } else {
                statoConformita = StatoConformita.NON_CONFORME;
            }
            discostamentoPercentuale = null;
        } else {
            // Limite non definito: non normato
            statoConformita = StatoConformita.NON_NORMATO;
            discostamentoPercentuale = null;
        }
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum StatoConformita {
        CONFORME,
        ATTENZIONE,
        NON_CONFORME,
        SOTTO_LOQ,
        NON_NORMATO
    }
}
