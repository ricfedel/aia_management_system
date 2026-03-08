package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "comunicazioni_ente")
public class ComunicazioneEnte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    // ── Tipo e stato ─────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoComunicazione tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoComunicazione stato = StatoComunicazione.BOZZA;

    // ── Ente destinatario / mittente ──────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnteEsterno ente;

    @Column(length = 200)
    private String enteUfficio;   // es. "Settore Ambiente", "ARPA Lombardia Sede"

    @Column(length = 200)
    private String enteReferente; // nome del funzionario

    // ── Dati trasmissione ─────────────────────────────────────────────────
    @Column(length = 500, nullable = false)
    private String oggetto;

    private LocalDate dataInvio;

    @Column(length = 100)
    private String numeroPecInvio;   // id del messaggio PEC inviato

    @Column(length = 100)
    private String protocolloInterno; // numero di protocollo interno

    @Column(length = 100)
    private String protocolloEnte;    // n. protocollo assegnato dall'ente

    @Column(columnDefinition = "TEXT")
    private String contenuto;         // corpo / descrizione estesa

    @Column(columnDefinition = "TEXT")
    private String note;

    // ── Allegati (lista di percorsi/nomi file, CSV semplice) ───────────────
    @Column(columnDefinition = "TEXT")
    private String allegati;          // "file1.pdf,file2.xlsx,..."

    // ── Riscontro / risposta ──────────────────────────────────────────────
    private Boolean hasRiscontro = false;

    private LocalDate dataRiscontro;

    @Column(length = 100)
    private String protocolloRiscontro;

    @Column(columnDefinition = "TEXT")
    private String noteRiscontro;

    @Column(length = 200)
    private String allegatiRiscontro;  // file di risposta

    // ── Prescrizione di riferimento (opzionale) ───────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescrizione_id")
    private Prescrizione prescrizione;

    // ── Punto di monitoraggio che ha originato la comunicazione ───────────────
    /**
     * Punto di monitoraggio che ha generato questa comunicazione
     * (es. SF2 per la notifica PEC di campionamento acque meteoriche).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitoraggio_id")
    private Monitoraggio monitoraggio;

    // ── Gestione PEC (ricevute accettazione/consegna) ─────────────────────────

    /** Identificativo messaggio PEC dalla ricevuta di accettazione */
    @Column(name = "pec_message_id_accettazione", length = 200)
    private String pecMessageIdAccettazione;

    /** Identificativo messaggio PEC dalla ricevuta di consegna */
    @Column(name = "pec_message_id_consegna", length = 200)
    private String pecMessageIdConsegna;

    /** Path file PDF ricevuta di accettazione PEC */
    @Column(name = "file_ricevuta_accettazione", length = 500)
    private String fileRicevutaAccettazione;

    /** Path file PDF ricevuta di consegna PEC */
    @Column(name = "file_ricevuta_consegna", length = 500)
    private String fileRicevutaConsegna;

    /**
     * Destinatari multipli della comunicazione.
     * Formato CSV di ID di {@link RecapitoEnte} (es. "1,2,3").
     * @deprecated Usare la relazione normalizzata {@link #destinatari} tramite
     *             {@link ComunicazioneDestinatario}. Questo campo viene mantenuto
     *             per retro-compatibilità e verrà rimosso nella prossima release.
     */
    @Deprecated
    @Column(name = "destinatari_ids", length = 500)
    private String destinatariIds;

    /**
     * Destinatari strutturati della comunicazione, con ruolo (TO/CC/BCC) e
     * tracciamento dello stato di consegna PEC per ciascuno.
     * Sostituisce il campo {@code destinatariIds}.
     */
    @OneToMany(mappedBy = "comunicazione", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComunicazioneDestinatario> destinatari = new ArrayList<>();

    // ── Audit ──────────────────────────────────────────────────────────────
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Enums innestati ────────────────────────────────────────────────────
    public enum TipoComunicazione {
        TRASMISSIONE_DATI_PMC,
        RELAZIONE_ANNUALE_AIA,
        NOTIFICA_CAMPIONAMENTO,       // Comunicazione PEC prima del campionamento autocontrollo
        TRASMISSIONE_RAPPORTO_PROVA,  // Invio RdP a enti (se richiesto)
        RICHIESTA_INFORMAZIONI,
        RISPOSTA_A_RICHIESTA,
        COMUNICAZIONE_NON_CONFORMITA,
        DOMANDA_RINNOVO_AIA,
        DOMANDA_MODIFICA_AIA,
        DIFFIDA,
        PRESCRIZIONE_AGGIUNTIVA,
        AUTORIZZAZIONE_RICEVUTA,
        COMUNICAZIONE_VARIAZIONE,
        ALTRO
    }

    public enum StatoComunicazione {
        BOZZA,
        INVIATA,
        CONSEGNATA_PEC,
        RISPOSTA_RICEVUTA,
        ARCHIVIATA
    }

    public enum EnteEsterno {
        ARPA,
        COMUNE,
        PROVINCIA,
        REGIONE,
        MINISTERO_AMBIENTE,
        PREFETTURA,
        ISPETTORATO_LAVORO,
        ASL,
        ALTRO
    }
}
