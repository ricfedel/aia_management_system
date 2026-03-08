package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "documenti")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescrizione_id")
    private Prescrizione prescrizione;

    @ManyToOne
    @JoinColumn(name = "stabilimento_id")
    private Stabilimento stabilimento;

    @Column(nullable = false)
    private String nome;

    @Column(name = "nome_file")
    private String nomeFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumento tipoDocumento;

    @Column(length = 1000)
    private String descrizione;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "versione")
    private Integer versione = 1;

    @Column(name = "ente_destinatario")
    private String enteDestinatario;

    private Integer anno;

    // ─── Campi DMS ────────────────────────────────────────────────────────────

    /** Stato nel ciclo di vita DMS */
    @Enumerated(EnumType.STRING)
    @Column(name = "stato_documento")
    private StatoDocumento statoDocumento = StatoDocumento.BOZZA;

    /** Ente emittente (es. ARPA Veneto, Regione Veneto) */
    @Column(name = "ente_emittente", length = 200)
    private String enteEmittente;

    /** Oggetto / titolo sintetico del documento */
    @Column(name = "oggetto", length = 500)
    private String oggetto;

    /** Numero di protocollo del documento */
    @Column(name = "numero_protocollo", length = 100)
    private String numeroProtocollo;

    /** Data di ricezione del documento fisico / PEC */
    @Column(name = "data_ricezione")
    private LocalDate dataRicezione;

    /** Tag liberi (stringa CSV separata da virgola) */
    @Column(name = "tags", length = 500)
    private String tags;

    /** Testo estratto via OCR / full-text (per ricerca) */
    @Column(name = "testo_estratto", columnDefinition = "TEXT")
    private String testoEstratto;

    /** Riferimento all'istanza di processo BPM collegata */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private ProcessoDocumento processo;

    /** ID documento padre (per versioning: la versione 2 punta alla versione 1) */
    @Column(name = "documento_padre_id")
    private Long documentoPadreId;

    /** Flag: questo documento è la versione corrente (head) della catena */
    @Column(name = "is_versione_corrente")
    private Boolean isVersioneCorrente = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

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

    // ─── Enumerations ────────────────────────────────────────────────────────

    public enum TipoDocumento {
        // ── Autorizzazioni ────────────────────────────────────────────────────
        DECRETO_AIA,            // Decreto/Determinazione di rilascio/rinnovo AIA
        PRESCRIZIONE_AIA,       // Singola prescrizione AIA

        // ── Monitoraggio e controllo ──────────────────────────────────────────
        RAPPORTO_PROVA,         // Rapporto di Prova laboratorio accreditato ACCREDIA
        DATI_LABORATORIO,       // Dati analitici in formato Excel/XML (es. Agrolab EXCEL_*)
        PMC_ANNUALE,            // Piano di Monitoraggio e Controllo annuale (Allegato 3 xlsx)
        RELAZIONE_ANNUALE,      // Relazione annuale PMC / relazione di conformità

        // ── Comunicazioni PEC ─────────────────────────────────────────────────
        COMUNICAZIONE_PEC,      // Comunicazione inviata via PEC agli enti
        PEC_RICEVUTA,           // Ricevuta accettazione o consegna PEC

        // ── Valutazioni tecniche ──────────────────────────────────────────────
        VALUTAZIONE_ACUSTICA,   // Valutazione impatto acustico
        STUDIO_TECNICO,         // Studio tecnico, analisi, perizia

        // ── Documenti operativi ───────────────────────────────────────────────
        REGISTRO_OPERATIVO,     // Registro operativo / controllo operativo (xlsm)
        FORMULARIO_RIFIUTI,     // FIR e formulari rifiuti (MUD, 231, 232)

        // ── Generici ──────────────────────────────────────────────────────────
        VERBALE,                // Verbale di ispezione / accesso
        PLANIMETRIA,
        INTEGRAZIONE,           // Integrazione a procedimento in corso
        ALTRO
    }

    public enum StatoDocumento {
        BOZZA,           // Caricato ma non ancora in lavorazione
        RICEVUTO,        // Ricevuto e protocollato
        IN_LAVORAZIONE,  // Processo BPM avviato, task in corso
        IN_REVISIONE,    // In attesa di revisione / approvazione
        APPROVATO,       // Revisionato e approvato
        ARCHIVIATO       // Iter completato, archiviato definitivamente
    }
}
