package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString(exclude = {"datiAmbientali", "parametri"})
@Entity
@Table(name = "monitoraggi")
public class Monitoraggio {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Monitoraggio)) return false;
        Monitoraggio that = (Monitoraggio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    @Column(nullable = false)
    private String codice;

    @Column(nullable = false)
    private String descrizione;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_monitoraggio")
    private TipoMonitoraggio tipoMonitoraggio;

    @Column(name = "punto_emissione")
    private String puntoEmissione;

    @Enumerated(EnumType.STRING)
    private FrequenzaMonitoraggio frequenza;

    @Column(name = "prossima_scadenza")
    private LocalDate prossimaScadenza;

    @Column(name = "laboratorio")
    private String laboratorio;

    @Column(name = "metodica")
    private String metodica;

    /** Normativa di riferimento per i limiti (es. "D.lgs 152/06", "AIA Decreto 19082/2024") */
    @Column(name = "normativa_riferimento", length = 300)
    private String normativaRiferimento;

    /** Matricola/codice contatore (usato per scarichi e acque) */
    @Column(name = "matricola", length = 100)
    private String matricola;

    // ── Dati tecnici emissione (tab 1.5.1 PMC) ────────────────────────────────

    /**
     * Codice numerico del punto nel modello PMC (diverso dal codice AIA E1/S1).
     * Es. nel foglio Excel PMC: "SF1", "SF2" per acque meteoriche,
     * "1", "2", "3"… per emissioni convogliate.
     */
    @Column(name = "codice_pmc", length = 50)
    private String codicePmc;

    /** Ore di funzionamento giornaliero (h/giorno) – tab 1.5.1 PMC */
    @Column(name = "ore_giorno")
    private Integer oreGiorno;

    /** Giorni di funzionamento annuo (gg/anno) – tab 1.5.1 PMC */
    @Column(name = "giorni_anno")
    private Integer giorniAnno;

    /** Portata scarico (m3/giorno) – per scarichi idrici, calcolo carico inquinante */
    @Column(name = "portata_scarico_m3d")
    private Double portataScaricoM3d;

    /**
     * Destinazione recapito finale dello scarico idrico.
     * Es. "acque superficiali – Torrente Alpone", "fognatura comunale", "vasca di raccolta"
     */
    @Column(name = "destinazione_recapito", length = 200)
    private String destinazioneRecapito;

    /**
     * Tipo di scarico idrico (sottoclasse di SCARICHI_IDRICI).
     * Permette di distinguere reflui civili da acque meteoriche.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_scarico")
    private TipoScarico tipoScarico;

    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "monitoraggio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatiAmbientali> datiAmbientali = new ArrayList<>();

    /** Parametri con limiti specifici per questo punto di monitoraggio */
    @OneToMany(mappedBy = "monitoraggio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParametroMonitoraggio> parametri = new ArrayList<>();

    /**
     * Collegamento opzionale all'anagrafica tecnica del camino.
     * Valorizzato solo per i punti di tipo EMISSIONI_ATMOSFERA.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "anagrafica_camino_id")
    private AnagraficaCamino anagraficaCamino;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TipoMonitoraggio {
        EMISSIONI_ATMOSFERA,
        SCARICHI_IDRICI,
        ACQUE_METEORICHE,
        FALDA,
        PIEZOMETRO,
        RUMORE,
        SUOLO,
        ODORI
    }

    public enum FrequenzaMonitoraggio {
        GIORNALIERA,
        SETTIMANALE,
        MENSILE,
        BIMESTRALE,
        TRIMESTRALE,
        SEMESTRALE,
        ANNUALE,
        BIENNALE,
        TRIENNALE
    }

    public enum TipoScarico {
        REFLUO_CIVILE,          // Acque reflue domestiche (impianto depurazione palazzina)
        ACQUE_METEORICHE,       // Acque di prima pioggia / acque meteoriche di dilavamento
        ACQUE_DI_PROCESSO,      // Acque di raffreddamento o processo
        ACQUE_MISTE             // Misto civile + meteoriche
    }
}
