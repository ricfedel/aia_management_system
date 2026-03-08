package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.ComunicazioneEnte.EnteEsterno;
import it.grandimolini.aia.model.ComunicazioneEnte.StatoComunicazione;
import it.grandimolini.aia.model.ComunicazioneEnte.TipoComunicazione;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ComunicazioneEnteDTO {

    private Long   id;
    private Long   stabilimentoId;
    private String stabilimentoNome;

    // Tipo e stato
    private TipoComunicazione  tipo;
    private StatoComunicazione stato;

    // Ente
    private EnteEsterno ente;
    private String      enteUfficio;
    private String      enteReferente;

    // Trasmissione
    private String    oggetto;
    private LocalDate dataInvio;
    private String    numeroPecInvio;
    private String    protocolloInterno;
    private String    protocolloEnte;
    private String    contenuto;
    private String    note;
    private String    allegati;

    // Riscontro
    private Boolean   hasRiscontro;
    private LocalDate dataRiscontro;
    private String    protocolloRiscontro;
    private String    noteRiscontro;
    private String    allegatiRiscontro;

    // Prescrizione collegata
    private Long   prescrizioneId;
    private String prescrizioneOggetto;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String        createdBy;
}
