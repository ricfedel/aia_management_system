package it.grandimolini.aia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Risultato dell'import (o preview) di scadenze da file Excel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportScadenzeResult {

    /** Righe parsate dal file Excel (con stato di match) */
    private List<RigaImport> righe;

    /** Numero di righe create con successo (solo dopo import effettivo) */
    private int create;

    /** Numero di righe saltate (data non valida, sito non mappato, ecc.) */
    private int saltate;

    /** Siti unici trovati nel file (per l'UI di mapping) */
    private List<String> sitiTrovati;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RigaImport {
        private int rigaExcel;         // numero riga nell'Excel (1-based)
        private String sito;           // es. "LI"
        private Long stabilimentoId;   // null se non mappato
        private String stabilimentoNome;
        private LocalDate dataScadenza;
        private LocalDate dataPrevistaAttivazione;
        private LocalDate dataAdempimento;
        private String riferimento;
        private String causale;        // titolo della scadenza
        private String documentiCorrelati;
        private String riscontroEnte;
        private String altro;
        private String tipoScadenzaRilevato; // tipo auto-rilevato
        private String errore;         // messaggio se riga non importabile
        private boolean selezionata;   // true per default se valida
    }
}
