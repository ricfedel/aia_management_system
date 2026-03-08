package it.grandimolini.aia.model;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * Indicatore di performance ambientale (Tabella 3.1 PMC).
 *
 * Contiene gli indicatori di performance calcolati annualmente come rapporto
 * tra un consumo (energia, acqua, polveri, rifiuti…) e una produzione
 * (tonnellate di grano macinato, farina prodotta, ore di funzionamento…).
 *
 * Esempi dal PMC Promolog 2024:
 * - Consumo specifico energia: 92.103 kWh/t (energia rete / grano macinato)
 * - Polveri su grano: 0.025 kg/t
 * - Acqua bagnatura su farina: 0.012 l/kg
 * - Rifiuti su grano: 0.003 kg/t
 * - % rifiuti avviati a recupero: 98.5 %
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "indicatori_performance")
public class IndicatorePerformance {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndicatorePerformance)) return false;
        IndicatorePerformance that = (IndicatorePerformance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    @Column(nullable = false)
    private Integer anno;

    // ── Classificazione ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoIndicatore indicatore;

    /** Descrizione estesa (es. "Consumo specifico di energia elettrica da rete") */
    @Column(length = 300)
    private String descrizione;

    // ── Valore ────────────────────────────────────────────────────────────────

    @Column(nullable = false)
    private Double valore;

    /** Unità di misura (es. "kWh/t", "MWh/t", "kg/t", "l/t", "m3/t", "%", "TEP") */
    @Column(name = "unita_misura", length = 30)
    private String unitaMisura;

    /** Valore anno precedente (per confronto) */
    @Column(name = "valore_anno_precedente")
    private Double valoreAnnoPrecedente;

    // ── Calcolo automatico ────────────────────────────────────────────────────

    /**
     * Se true, il valore è calcolato automaticamente dal sistema
     * usando i dati di RegistroMensile, VoceProduzione, MovimentoRifiuto, RilevazioneMisura.
     */
    @Column(name = "calcolato_automaticamente")
    private Boolean calcolatoAutomaticamente = false;

    /**
     * Formula di calcolo (per documentazione e audit trail).
     * Es. "sum(energia_elettrica_kwh) / sum(grano_macinato_t)"
     */
    @Column(name = "formula", length = 300)
    private String formula;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // ── Audit ──────────────────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum TipoIndicatore {
        CONSUMO_SPEC_ENERGIA_RETE,          // kWh/t grano
        CONSUMO_SPEC_ENERGIA_TOTALE,        // kWh/t grano (rete + fotovoltaico)
        CONSUMO_SPEC_GAS_NATURALE,          // Nm3/t produzione
        CONSUMO_SPEC_ACQUA,                 // m3/t grano
        CONSUMO_SPEC_ACQUA_BAGNATURA,       // l/kg farina
        POLVERI_SU_GRANO,                   // kg polveri emesse / t grano
        RIFIUTI_SU_GRANO,                   // kg rifiuti / t grano
        PERC_RIFIUTI_RECUPERO,              // % rifiuti avviati a recupero
        CONSUMO_TEP,                        // Tonnellate equivalenti petrolio totali
        CONSUMO_SPEC_TEP,                   // TEP / t produzione
        EFFICIENZA_IMPIANTO_ABBATTIMENTO,   // % efficienza filtri (polveri in/out)
        FATTORE_EMISSIONE_POLVERI,          // kg polveri / t grano per punto di emissione
        ALTRO
    }
}
