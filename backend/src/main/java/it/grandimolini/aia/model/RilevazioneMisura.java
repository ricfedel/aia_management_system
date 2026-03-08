package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Singola rilevazione/misura per un parametro di un punto di monitoraggio.
 * La conformità viene calcolata automaticamente confrontando il valore misurato
 * con il limite definito nel ParametroMonitoraggio associato.
 */
@Data
@Entity
@Table(name = "rilevazioni_misura")
public class RilevazioneMisura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parametro_monitoraggio_id", nullable = false)
    private ParametroMonitoraggio parametroMonitoraggio;

    @Column(name = "data_campionamento", nullable = false)
    private LocalDate dataCampionamento;

    @Column(name = "valore_misurato", nullable = false)
    private Double valoreMisurato;

    @Column(name = "unita_misura", length = 20)
    private String unitaMisura;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_conformita")
    private StatoConformita statoConformita;

    /** Numero rapporto di prova del laboratorio (es. "24/000213869") */
    @Column(name = "rapporto_prova", length = 100)
    private String rapportoProva;

    /** Numero di accettazione campione (es. "24.233533.0001") */
    @Column(name = "numero_accettazione", length = 100)
    private String numeroAccettazione;

    /** Laboratorio che ha eseguito l'analisi (es. "Chelab S.r.l.") */
    @Column(name = "laboratorio", length = 200)
    private String laboratorio;

    /**
     * Collegamento al Rapporto di Prova strutturato (entità completa con tutte le righe).
     * Se presente, questa RilevazioneMisura è una vista/sommario di una specifica
     * {@link RigaRapportoProva} contenuta nel rapporto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rapporto_prova_id")
    private RapportoProva rapportoProvaEntity;

    // ── Metadati campionamento ─────────────────────────────────────────────────

    /** Ora di prelievo del campione */
    @Column(name = "ora_campionamento")
    private java.time.LocalTime oraCampionamento;

    @Column(name = "data_ricevimento_campione")
    private java.time.LocalDate dataRicevimentoCampione;

    @Column(name = "data_emissione_rapporto")
    private java.time.LocalDate dataEmissioneRapporto;

    @Enumerated(EnumType.STRING)
    @Column(name = "campionato_da")
    private CampinatoDa campinatoDa;

    /** Numero verbale di campionamento (es. "AM240035/01/02") */
    @Column(name = "verbale_campionamento", length = 100)
    private String verbaleCampionamento;

    /** Numero catena di custodia (es. "24/1315") */
    @Column(name = "catena_custodia", length = 100)
    private String catenaCustodia;

    // ── Dati analitici estesi ─────────────────────────────────────────────────

    /** Incertezza estesa della misura (es. ±0.08 per pH = 7.10) */
    @Column(name = "incertezza")
    private Double incertezza;

    /** Limite di quantificazione (LOQ) del metodo */
    @Column(name = "loq")
    private Double loq;

    /** Riferimento normativo per il limite (es. "DL 152/06 TAB3 SUP") */
    @Column(name = "valore_riferimento_normativo", length = 200)
    private String valoreRiferimentoNormativo;

    // ── Dati specifici emissioni atmosfera ────────────────────────────────────

    /** Flusso di massa annuale (per emissioni aria, es. 41.325 kg/anno) */
    @Column(name = "flusso_massa")
    private Double flussoMassa;

    /** Unità di misura del flusso di massa (default "kg/anno") */
    @Column(name = "um_flusso", length = 30)
    private String umFlusso;

    // ── Dati specifici scarichi idrici ────────────────────────────────────────

    /**
     * Carico inquinante giornaliero calcolato automaticamente.
     * Formula: valoreMisurato [mg/l] × portataScaricoM3d [m³/gg] / 1_000
     * Unità risultante: kg/giorno.
     * Valorizzato dal service prima del salvataggio quando la portata è disponibile.
     */
    @Column(name = "carico_inquinante_kg_d")
    private Double caricoInquinanteKgd;

    // ── Conformità calcolata ───────────────────────────────────────────────────

    /**
     * Discostamento percentuale dal limite: (valore_misurato / limite) * 100.
     * Calcolato automaticamente; >100% = non conforme.
     */
    @Column(name = "discostamento_percentuale")
    private Double discostamentoPercentuale;

    // ── Acustica (per RilevazioneMisura di tipo RUMORE) ───────────────────────

    /** Periodo (DIURNO / NOTTURNO) – usato per misure acustiche */
    @Enumerated(EnumType.STRING)
    @Column(name = "periodo_acustico")
    private PeriodoAcustico periodoAcustico;

    /** Posizione di misura (es. "E1", "R2") – usato per misure acustiche */
    @Column(name = "posizione_misura", length = 50)
    private String posizioneMisura;

    /** Tipo componente acustica (GLOBALE / TONALE / IMPULSIVO) */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente_acustica")
    private TipoComponenteAcustica tipoComponenteAcustica;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calcolaConformita();
    }

    @PreUpdate
    protected void onUpdate() {
        calcolaConformita();
    }

    /**
     * Calcola conformità: confronta valoreMisurato con il limite del parametro.
     * - CONFORME:     valore ≤ 80% del limite
     * - ATTENZIONE:   80% < valore ≤ 100% del limite (fascia di pre-allarme)
     * - NON_CONFORME: valore > limite
     * Se il parametro non ha un limite definito, rimane null.
     */
    public void calcolaConformita() {
        if (valoreMisurato == null) return;
        Double limite = parametroMonitoraggio != null
                ? parametroMonitoraggio.getLimiteValore()
                : null;
        if (limite == null || limite == 0) {
            statoConformita = null;
            discostamentoPercentuale = null;
            return;
        }
        discostamentoPercentuale = (valoreMisurato / limite) * 100.0;
        if (valoreMisurato <= limite * 0.80) {
            statoConformita = StatoConformita.CONFORME;
        } else if (valoreMisurato <= limite) {
            statoConformita = StatoConformita.ATTENZIONE;
        } else {
            statoConformita = StatoConformita.NON_CONFORME;
        }
    }

    // ── Calcoli automatici (chiamati dal service prima del salvataggio) ───────

    /**
     * Calcola e imposta il flusso di massa annuale per emissioni in atmosfera.
     *
     * Formula: flussoMassa [kg/anno] = valoreMisurato [mg/Nm³]
     *          × portataNomc3h [Nm³/h] × oreGiorno [h/gg] × giorniAnno [gg/anno]
     *          / 1_000_000
     *
     * Non viene chiamato in @PrePersist per evitare problemi di lazy-load;
     * il service deve chiamare questo metodo PRIMA di salvare l'entità.
     *
     * @param portataNomc3h  Portata volumetrica del camino in Nm³/h (da AnagraficaCamino)
     * @param oreGiorno      Ore di funzionamento giornaliero (da Monitoraggio)
     * @param giorniAnno     Giorni di funzionamento annui (da Monitoraggio)
     */
    public void calcolaFlussoMassa(Double portataNomc3h, Integer oreGiorno, Integer giorniAnno) {
        if (valoreMisurato == null || portataNomc3h == null
                || oreGiorno == null || giorniAnno == null
                || portataNomc3h == 0) {
            return; // Non sovrascrivere un valore già inserito manualmente
        }
        flussoMassa = (valoreMisurato * portataNomc3h * oreGiorno * giorniAnno) / 1_000_000.0;
        umFlusso = "kg/anno";
    }

    /**
     * Calcola e imposta il carico inquinante giornaliero per scarichi idrici.
     *
     * Formula: caricoInquinanteKgd [kg/gg] = valoreMisurato [mg/l]
     *          × portataScaricoM3d [m³/gg] / 1_000
     *
     * Non viene chiamato in @PrePersist per evitare problemi di lazy-load;
     * il service deve chiamare questo metodo PRIMA di salvare l'entità.
     *
     * @param portataScaricoM3d Portata del punto di scarico in m³/giorno (da Monitoraggio)
     */
    public void calcolaCarico(Double portataScaricoM3d) {
        if (valoreMisurato == null || portataScaricoM3d == null || portataScaricoM3d == 0) {
            return;
        }
        caricoInquinanteKgd = (valoreMisurato * portataScaricoM3d) / 1_000.0;
    }

    public enum StatoConformita {
        CONFORME,
        ATTENZIONE,
        NON_CONFORME
    }

    public enum CampinatoDa {
        CLIENTE,
        TECNICO_LAB,
        TECNICO_ARPAV,
        TECNICO_ESTERNO
    }

    public enum PeriodoAcustico {
        DIURNO,
        NOTTURNO
    }

    public enum TipoComponenteAcustica {
        GLOBALE,
        TONALE,
        IMPULSIVO,
        RESIDUO
    }
}
