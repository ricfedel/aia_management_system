package it.grandimolini.aia.model;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * Classificazione acustica dell'area circostante lo stabilimento.
 *
 * Mappa le posizioni di misura (E1, E2, R1, R2…) con la rispettiva classe
 * acustica assegnata dal Piano di Zonizzazione Acustica Comunale (PZAC) e i
 * limiti tabellari diurni/notturni (DPCM 14/11/97, D.Lgs. 42/2017).
 *
 * In combinazione con {@link RilevazioneMisura#getPosizioneMisura()} permette
 * di valutare automaticamente la conformità delle misure acustiche rispetto
 * ai limiti di zona applicabili a quella specifica posizione.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "zone_acustiche",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_zona_acustica_stabilimento_posizione",
                columnNames = {"stabilimento_id", "posizione"}))
public class ZonaAcustica {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZonaAcustica)) return false;
        ZonaAcustica that = (ZonaAcustica) o;
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

    // ── Identificazione della posizione ───────────────────────────────────────

    /**
     * Codice posizione di misura come da relazione acustica
     * (es. "E1", "E2", "R1", "R2", "E1/R1" per posizioni coincidenti).
     */
    @Column(nullable = false, length = 20)
    private String posizione;

    /** Descrizione estesa della posizione (es. "Confine Nord-Est - Via Industria") */
    @Column(length = 300)
    private String descrizione;

    // ── Classificazione acustica ───────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "classe_acustica", nullable = false)
    private ClasseAcustica classeAcustica;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_posizione", nullable = false)
    private TipoPosizione tipoPosizione;

    // ── Limiti tabellari (dB(A)) ──────────────────────────────────────────────

    /** Limite di emissione diurno [dB(A)] – fascia oraria 06:00-22:00 */
    @Column(name = "limite_emissione_diurno")
    private Double limiteEmissioneDiurno;

    /** Limite di emissione notturno [dB(A)] – fascia oraria 22:00-06:00 */
    @Column(name = "limite_emissione_notturno")
    private Double limiteEmissioneNotturno;

    /** Limite assoluto di immissione diurno [dB(A)] */
    @Column(name = "limite_immissione_diurno")
    private Double limiteImmissioneDiurno;

    /** Limite assoluto di immissione notturno [dB(A)] */
    @Column(name = "limite_immissione_notturno")
    private Double limiteImmissioneNotturno;

    /**
     * Limite differenziale diurno [dB(A)].
     * Criterio differenziale: L_residuo - L_ambientale ≤ 5 dB(A) diurno, 3 dB(A) notturno.
     * Applicabile solo dove previsto; null se non applicabile.
     */
    @Column(name = "limite_differenziale_diurno")
    private Double limiteDifferenzialeDiurno;

    /** Limite differenziale notturno [dB(A)] */
    @Column(name = "limite_differenziale_notturno")
    private Double limiteDifferenzialeNotturno;

    // ── Riferimento normativo e piano ─────────────────────────────────────────

    /** Riferimento al piano di zonizzazione acustica comunale (es. "PZAC Comune di Cerea 2018") */
    @Column(name = "riferimento_pzac", length = 300)
    private String riferimentoPzac;

    /** Anno del provvedimento / piano di zonizzazione */
    @Column(name = "anno_pzac")
    private Integer annoPzac;

    /**
     * Indica se la posizione è soggetta a deroga per le sorgenti industriali
     * preesistenti (art. 8 L. 447/95).
     */
    @Column(name = "in_deroga")
    private Boolean inDeroga = false;

    /** Estremi del provvedimento di deroga (se presente) */
    @Column(name = "provvedimento_deroga", length = 300)
    private String provvedimentoDeroga;

    // ── Note e audit ──────────────────────────────────────────────────────────

    @Column(length = 1000)
    private String note;

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

    /**
     * Classi acustiche DPCM 14/11/97.
     * Classe I = aree particolarmente protette → Classe VI = aree industriali.
     */
    public enum ClasseAcustica {
        CLASSE_I,   // Aree particolarmente protette (ospedali, scuole, residenze di cura)
        CLASSE_II,  // Aree prevalentemente residenziali
        CLASSE_III, // Aree di tipo misto
        CLASSE_IV,  // Aree di intensa attività umana
        CLASSE_V,   // Aree prevalentemente industriali
        CLASSE_VI   // Aree esclusivamente industriali
    }

    /**
     * Tipo della posizione di misura rispetto allo stabilimento.
     */
    public enum TipoPosizione {
        CONFINE_STABILIMENTO,     // Punto E: sul confine di proprietà dell'impianto
        RICETTORE_SENSIBILE,      // Punto R: ricettore residenziale/scolastico/ospedaliero
        PUNTO_CONTROLLO,          // Punto di monitoraggio ARPAV (collocato da ente)
        SFONDO_RESIDUO            // Misurazione del rumore residuo (L_residuo)
    }
}
