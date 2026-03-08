package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Rapporto di Prova (RdP) emesso da un laboratorio accreditato ACCREDIA.
 *
 * Un singolo RdP copre N parametri analitici (es. pH, BOD5, COD, SST, idrocarburi…)
 * relativi a un campione prelevato su un punto di monitoraggio specifico.
 * Ogni riga analitica è rappresentata da {@link RigaRapportoProva}.
 *
 * Laboratori tipici: Chelab S.r.l. (LAB N° 0051 L), Agrolab Italia (LAB N° 0147 L).
 */
@Getter
@Setter
@ToString(exclude = {"righe"})
@Entity
@Table(name = "rapporti_prova")
public class RapportoProva {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RapportoProva)) return false;
        RapportoProva that = (RapportoProva) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relazioni ──────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    /** Punto di monitoraggio campionato (es. SF2, S1, S2, E1…) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitoraggio_id")
    private Monitoraggio monitoraggio;

    // ── Identificativi laboratorio ─────────────────────────────────────────────

    /** Numero di accettazione campione (es. 24.233533.0001 – Chelab) */
    @Column(name = "numero_accettazione", length = 100)
    private String numeroAccettazione;

    /** Numero ufficiale del Rapporto di Prova (es. 24/000213869) */
    @Column(name = "numero_rapporto", length = 100, nullable = false)
    private String numeroRapporto;

    /** Ragione sociale del laboratorio (es. "Chelab S.r.l.") */
    @Column(name = "laboratorio_nome", length = 200)
    private String laboratorioNome;

    /** Numero accreditamento ACCREDIA (es. "LAB N° 0051 L") */
    @Column(name = "laboratorio_lab_n", length = 50)
    private String laboratorioLabN;

    // ── Date campionamento e analisi ───────────────────────────────────────────

    @Column(name = "data_campionamento", nullable = false)
    private LocalDate dataCampionamento;

    @Column(name = "ora_campionamento")
    private LocalTime oraCampionamento;

    @Column(name = "data_ricevimento")
    private LocalDate dataRicevimento;

    @Column(name = "data_emissione")
    private LocalDate dataEmissione;

    // ── Dati campione ──────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "matrice")
    private MatriceCampione matrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "campionato_da")
    private CampinatoDa campinatoDa;

    /** Provenienza/indirizzo del punto di prelievo (es. "Promolog Srl Via Zurlare 21 Coriano VR") */
    @Column(name = "provenienza", length = 300)
    private String provenienza;

    /** Descrizione estesa del campione (es. "Acque meteoriche ore 10:00 26/03/2024 SF2") */
    @Column(name = "descrizione_campione", length = 500)
    private String descrizioneCampione;

    /** Numero verbale di campionamento (usato da Agrolab, es. AM240035/01/02) */
    @Column(name = "verbale_campionamento", length = 100)
    private String verbaleCampionamento;

    /** Numero catena di custodia (usato da Agrolab, es. 24/1315) */
    @Column(name = "catena_custodia", length = 100)
    private String catenaCustodia;

    // ── Esito globale ──────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "conformita_globale")
    private ConformitaGlobale conformitaGlobale;

    @Column(name = "note_conformita", columnDefinition = "TEXT")
    private String noteConformita;

    /** Responsabile chimico firmatario (es. "Dott.ssa Barbara Scantamburlo") */
    @Column(name = "responsabile_chimico", length = 200)
    private String responsabileChimico;

    // ── File allegato ──────────────────────────────────────────────────────────

    /** Path del PDF del rapporto di prova archiviato nel file storage */
    @Column(name = "file_path", length = 500)
    private String filePath;

    // ── Audit ──────────────────────────────────────────────────────────────────

    @Column(name = "creato_da", length = 100)
    private String creatoDa;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Righe analitiche ───────────────────────────────────────────────────────

    @OneToMany(mappedBy = "rapportoProva", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rigaNumero ASC")
    private List<RigaRapportoProva> righe = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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

    public enum MatriceCampione {
        ACQUA_SCARICO,
        ACQUA_METEORICA,
        ACQUA_SOTTERRANEA,
        ARIA_EMISSIONE,
        ARIA_AMBIENTE,
        SUOLO,
        SEDIMENTO,
        ALTRO
    }

    public enum CampinatoDa {
        CLIENTE,
        TECNICO_LAB,
        TECNICO_ARPAV,
        TECNICO_ESTERNO
    }

    public enum ConformitaGlobale {
        CONFORME,
        NON_CONFORME,
        PARZIALE,
        SOTTO_LOQ,
        IN_VALUTAZIONE
    }
}
