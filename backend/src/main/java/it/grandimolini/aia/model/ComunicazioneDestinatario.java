package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Destinatario di una comunicazione PEC.
 *
 * Sostituisce il campo CSV {@code destinatariIds} in {@link ComunicazioneEnte},
 * consentendo di tracciare per ciascun destinatario:
 * - il ruolo (TO / CC / BCC)
 * - lo stato di consegna PEC individuale
 * - la ricevuta di consegna specifica
 *
 * Esempio: la PEC di notifica campionamento SF2 viene inviata a
 *   - Provincia VR (TO) → ricevuta di consegna
 *   - ARPAV Sede (TO)   → ricevuta di consegna
 *   - Comune Cerea (CC) → ricevuta di consegna
 * ognuno con il proprio stato e file ricevuta.
 */
@Data
@Entity
@Table(name = "comunicazioni_destinatari",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_comunicazione_destinatario",
                columnNames = {"comunicazione_id", "recapito_ente_id"}))
public class ComunicazioneDestinatario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relazioni principali ───────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicazione_id", nullable = false)
    private ComunicazioneEnte comunicazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recapito_ente_id", nullable = false)
    private RecapitoEnte recapitoEnte;

    // ── Ruolo nella comunicazione ─────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuoloDestinatario ruolo = RuoloDestinatario.TO;

    // ── Tracciamento consegna PEC ─────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_consegna")
    private StatoConsegna statoConsegna = StatoConsegna.IN_ATTESA;

    /** Message-ID della ricevuta di accettazione PEC per questo destinatario */
    @Column(name = "pec_message_id_accettazione", length = 200)
    private String pecMessageIdAccettazione;

    /** Message-ID della ricevuta di consegna PEC per questo destinatario */
    @Column(name = "pec_message_id_consegna", length = 200)
    private String pecMessageIdConsegna;

    /** Path del file PDF ricevuta di accettazione specifico per questo destinatario */
    @Column(name = "file_ricevuta_accettazione", length = 500)
    private String fileRicevutaAccettazione;

    /** Path del file PDF ricevuta di consegna specifico per questo destinatario */
    @Column(name = "file_ricevuta_consegna", length = 500)
    private String fileRicevutaConsegna;

    /** Timestamp consegna confermata dalla ricevuta PEC */
    @Column(name = "data_consegna_confermata")
    private LocalDateTime dataConsegnaConfermata;

    /** Note specifiche per questo destinatario (es. "Mancata consegna – casella piena") */
    @Column(length = 500)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum RuoloDestinatario {
        TO,   // Destinatario principale
        CC,   // Copia conoscenza
        BCC   // Copia conoscenza nascosta
    }

    public enum StatoConsegna {
        IN_ATTESA,         // PEC non ancora inviata o in transito
        ACCETTATA,         // Ricevuta di accettazione ricevuta dal gestore PEC
        CONSEGNATA,        // Ricevuta di consegna ricevuta (casella destinatario)
        MANCATA_CONSEGNA,  // Errore consegna (casella piena, inesistente ecc.)
        RIFIUTATA          // Messaggio rifiutato dal destinatario
    }
}
