package it.grandimolini.aia.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Risultato dell'analisi OCR / AI di un documento.
 * Contiene il testo estratto e le entità proposte dal parser
 * che l'operatore deve confermare o correggere.
 */
@Data
public class PropostaEstrazione {

    /** ID del documento analizzato */
    private Long documentoId;

    /** Nome del file analizzato */
    private String nomeFile;

    /** Metodo usato per l'estrazione */
    private MetodoEstrazione metodo;

    /** Testo grezzo estratto dal documento (PDF text layer o OCR) */
    private String testoEstratto;

    /** Confidenza dell'estrazione 0.0–1.0 */
    private Double confidenza;

    /** Eventuali avvisi o limitazioni dell'analisi */
    private String avvisi;

    // ─── Metadati estratti ────────────────────────────────────────────────────

    private MetadatiDocumento metadati = new MetadatiDocumento();

    // ─── Entità proposte ─────────────────────────────────────────────────────

    /** Scadenze identificate nel testo */
    private List<ScadenzaProposta> scadenzeProposte = new ArrayList<>();

    /** Prescrizioni identificate nel testo */
    private List<PrescrizioneProposta> prescrizioniProposte = new ArrayList<>();

    // ─── Nested DTOs ─────────────────────────────────────────────────────────

    @Data
    public static class MetadatiDocumento {
        private String oggetto;
        private String enteEmittente;
        private String numeroProtocollo;
        private String dataRicezione;    // ISO date string "YYYY-MM-DD"
        private String tipoProvvedimento;
        private String riferimentoAIA;
    }

    @Data
    public static class ScadenzaProposta {
        private String tempId;           // ID temporaneo per tracciamento UI
        private String titolo;
        private String descrizione;
        private String dataScadenza;     // ISO date string
        private String tipo;             // es. MONITORAGGIO, REPORTING, COMUNICAZIONE
        private String testoOrigine;     // frase del documento da cui è stata estratta
        private Integer giorni;          // se espresso in giorni (es. "entro 30 giorni")
        private Double confidenza;
        private boolean selezionata = true;
    }

    @Data
    public static class PrescrizioneProposta {
        private String tempId;
        private String codice;           // es. "A.3", "Punto 4.2"
        private String descrizione;
        private String tipo;             // EMISSIONI_ATMOSFERICHE, SCARICHI_IDRICI, ...
        private String testoOrigine;
        private Double confidenza;
        private boolean selezionata = true;
    }

    public enum MetodoEstrazione {
        PDFBOX_TEXT,   // Testo embedded nel PDF
        AI_EXTRACTION, // Chiamata a modello AI
        FALLBACK_REGEX // Solo regex, senza testo completo
    }
}
