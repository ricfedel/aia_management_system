package it.grandimolini.aia.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO aggregato usato sia per la preview JSON sia come sorgente dati
 * per la generazione del documento Word della Relazione Annuale AIA.
 */
@Data
public class RelazioneAnnualeDTO {

    // ── Intestazione ──────────────────────────────────────────────────────
    private Long   stabilimentoId;
    private String stabilimentoNome;
    private String stabilimentoIndirizzo;
    private String stabilimentoCodiceAIA;
    private int    anno;
    private String dataGenerazione;   // ISO date string

    // ── Sezione 1: Prescrizioni ───────────────────────────────────────────
    private int             totalePrescrizioni;
    private int             prescrizioniScadute;
    private int             prescrizioniInScadenza;  // entro 30 gg
    private List<PrescrRow> prescrizioni;

    // ── Sezione 2: Piano di Monitoraggio (Punti PMC) ──────────────────────
    private int              totalePuntiMonitoraggio;
    private List<MonRow>     puntiMonitoraggio;

    // ── Sezione 3: Conformità ─────────────────────────────────────────────
    private long             totaleRilevazioni;
    private long             rilevConformi;
    private long             rilevAttenzione;
    private long             rilevNonConformi;
    private double           percConformita;          // %
    private List<ConformRow> topNonConformi;          // le peggiori 10

    // ── Sezione 4: Rifiuti ────────────────────────────────────────────────
    private List<RifiutoRow> rifiutiRiepilogo;        // per CER

    // ── Sezione 5: Produzione e Consumi ───────────────────────────────────
    private List<ProdRow>    produzioneRiepilogo;     // per categoria, somma 12 mesi
    private List<String>     mesiConDati;             // mesi per cui esiste registro

    // ── Sezione 6: Comunicazioni con enti ────────────────────────────────
    private int              totaleComunicazioni;
    private int              comunicazioniInviate;
    private int              comunicazioniConRiscontro;
    private List<ComRow>     comunicazioni;

    // ═══════════════════════════════════════════════════════════════════════
    // Classi interne per le righe di dettaglio
    // ═══════════════════════════════════════════════════════════════════════

    @Data
    public static class PrescrRow {
        private Long   id;
        private String numero;
        private String descrizione;
        private String tipo;
        private String stato;
        private String dataScadenza;
    }

    @Data
    public static class MonRow {
        private Long         id;
        private String       codice;
        private String       descrizione;
        private String       tipo;
        private int          numParametri;
        private String       frequenza;
        private List<String> parametriNomi;
    }

    @Data
    public static class ConformRow {
        private String  monitoraggioCodice;
        private String  parametroNome;
        private String  unita;
        private Double  valoreMisurato;
        private Double  limiteValore;
        private double  percLimite;   // valore/limite * 100
        private String  stato;
        private String  dataUltimaRilev;
    }

    @Data
    public static class RifiutoRow {
        private String codiceCer;
        private String descrizione;
        private boolean pericoloso;
        private Double  qProdotta;
        private Double  qSmaltita;
        private Double  qRecuperata;
        private Double  qCeduta;
        private String  unita;
    }

    @Data
    public static class ProdRow {
        private String  categoria;
        private String  descrizione;
        private String  codice;
        private Double  totaleAnno;
        private Double  totaleAnnoPrecedente;
        private Double  variazione;   // %
        private String  unita;
    }

    @Data
    public static class ComRow {
        private Long   id;
        private String tipo;
        private String ente;
        private String oggetto;
        private String dataInvio;
        private String stato;
        private boolean hasRiscontro;
    }
}
