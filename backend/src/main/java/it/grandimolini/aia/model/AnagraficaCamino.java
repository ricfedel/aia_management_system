package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Anagrafica tecnica dei camini (punti di emissione in atmosfera).
 * Dati provenienti dalla Tabella A "Quadro riassuntivo delle emissioni in atmosfera"
 * allegata al provvedimento di AIA di ciascun stabilimento.
 */
@Data
@Entity
@Table(name = "anagrafica_camini",
       uniqueConstraints = @UniqueConstraint(columnNames = {"stabilimento_id", "sigla"}))
public class AnagraficaCamino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    /** Sigla del punto emissivo (es. E1, E2, …, E97) */
    @Column(nullable = false, length = 20)
    private String sigla;

    /** Fase di processo (TRASPORTO, PREPULITURA, PULITURA, MACINAZIONE, MOVIMENTAZIONE, MANUTENZIONE, CONTROLLO_QUALITA) */
    @Enumerated(EnumType.STRING)
    @Column(name = "fase_processo")
    private FaseProcesso faseProcesso;

    /** Descrizione dell'origine dell'emissione (es. "trasportatore torre", "vibroblok", "molino a duro") */
    @Column(length = 200)
    private String origine;

    /** Portata volumetrica in condizioni normali [Nm³/h] */
    @Column(name = "portata_nm3h")
    private Double portataNomc3h;

    /** Sezione del condotto [m²] */
    @Column(name = "sezione_m2")
    private Double sezioneM2;

    /** Velocità dei fumi [m/s] */
    @Column(name = "velocita_ms")
    private Double velocitaMs;

    /**
     * Temperatura dei fumi [°C].
     * NULL indica temperatura ambiente (campo temperaturaAmbiente = true).
     */
    @Column(name = "temperatura_c")
    private Double temperaturaC;

    /** True quando la temperatura è "ambiente" (non determinata numericamente) */
    @Column(name = "temperatura_ambiente", nullable = false)
    private Boolean temperaturaAmbiente = true;

    /** Altezza del camino [m] */
    @Column(name = "altezza_m")
    private Double altezzaM;

    /** Durata di funzionamento [h/giorno] */
    @Column(name = "durata_h_giorno")
    private Integer durataHGiorno;

    /** Durata di funzionamento [giorni/anno] */
    @Column(name = "durata_g_anno")
    private Integer durataGAnno;

    /** Sistema di abbattimento (es. "filtro a maniche", "turbodecantatore", "ciclone") */
    @Column(name = "impianto_abbattimento", length = 200)
    private String impiantoAbbattimento;

    /** Note aggiuntive (es. "controllo mensile dei deprimometri", "realizzazione prevista entro 2025") */
    @Column(length = 500)
    private String note;

    /** True se il camino è attivo/autorizzato e in esercizio */
    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum FaseProcesso {
        TRASPORTO,
        PREPULITURA,
        PULITURA,
        MACINAZIONE,
        MOVIMENTAZIONE,
        MANUTENZIONE,
        CONTROLLO_QUALITA,
        ALTRO
    }
}
