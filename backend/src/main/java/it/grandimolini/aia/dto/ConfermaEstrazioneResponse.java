package it.grandimolini.aia.dto;

import lombok.Data;
import java.util.List;

/**
 * Risposta alla conferma: riporta quante entità sono state create
 * e i relativi ID per il feedback all'operatore.
 */
@Data
public class ConfermaEstrazioneResponse {

    private boolean successo;
    private String messaggio;

    private int scadenzeCreate;
    private int prescrizioniCreate;
    private List<Long> idScadenzeCreate;
    private List<Long> idPrescrizioniCreate;

    /** Stato del documento aggiornato */
    private String nuovoStatoDocumento;

    /** Processo BPM avanzato (se presente) */
    private ProcessoDocumentoDTO processoAggiornato;
}
