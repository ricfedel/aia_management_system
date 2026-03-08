package it.grandimolini.aia.dto;

import lombok.Data;
import java.util.List;

/**
 * Richiesta di conferma dell'estrazione: l'operatore ha revisionato
 * le entità proposte, le ha eventualmente modificate, e ora chiede
 * al sistema di crearle definitivamente.
 */
@Data
public class ConfermaEstrazioneRequest {

    /** ID del documento a cui si riferisce la conferma */
    private Long documentoId;

    /** Metadati confermati/corretti dall'operatore */
    private MetadatiConfermati metadati;

    /** Scadenze confermate (solo quelle con selezionata=true) */
    private List<ScadenzaConfermata> scadenze;

    /** Prescrizioni confermate */
    private List<PrescrizioneConfermata> prescrizioni;

    /** ID del processo BPM da far avanzare al completamento (opzionale) */
    private Long processoId;
    private Long taskId;

    /** Note dell'operatore sulla revisione */
    private String noteRevisione;

    // ─── Nested ──────────────────────────────────────────────────────────────

    @Data
    public static class MetadatiConfermati {
        private String oggetto;
        private String enteEmittente;
        private String numeroProtocollo;
        private String dataRicezione;
    }

    @Data
    public static class ScadenzaConfermata {
        private String titolo;
        private String descrizione;
        private String dataScadenza;   // ISO "YYYY-MM-DD"
        private String tipo;
        private Long stabilimentoId;
    }

    @Data
    public static class PrescrizioneConfermata {
        private String codice;
        private String descrizione;
        private String tipo;
        private Long stabilimentoId;
    }
}
