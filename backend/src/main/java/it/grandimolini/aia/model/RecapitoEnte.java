package it.grandimolini.aia.model;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * Rubrica degli enti con destinatari PEC preimpostati per ogni stabilimento.
 *
 * Quando si crea una {@link ComunicazioneEnte} di tipo NOTIFICA_CAMPIONAMENTO,
 * il sistema propone automaticamente i recapiti registrati per lo stabilimento,
 * evitando di reinserire ogni volta i destinatari PEC istituzionali + CC interni.
 *
 * Esempio per Promolog (stabilimento_id=1):
 * - ambiente.provincia.vr@pecveneto.it  → Provincia Verona Settore Ambiente
 * - dapvr@pec.arpav.it                  → ARPAV Dipartimento Verona
 * - protocollo.albaredodadige@pec.it    → Comune Albaredo d'Adige
 * - colaboratorio@grandimolini.it       → (CC interno, email ordinaria)
 * - mpasetto@grandimolini.it            → (CC interno, email ordinaria)
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "recapiti_ente")
public class RecapitoEnte {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecapitoEnte)) return false;
        RecapitoEnte that = (RecapitoEnte) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    // ── Tipo e identificazione ente ───────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "ente_tipo", nullable = false)
    private TipoEnteRecapito enteTipo;

    /** Nome completo dell'ente (es. "Provincia di Verona – Settore Ambiente") */
    @Column(name = "ente_nome", length = 300, nullable = false)
    private String enteNome;

    /** Ufficio/struttura specifica (es. "U.O. Servizio Tutela e Valorizzazione Ambientale") */
    @Column(name = "ufficio", length = 300)
    private String ufficio;

    /** Nome del referente/funzionario (es. "Alessandro Iseppi") */
    @Column(name = "referente", length = 200)
    private String referente;

    // ── Contatti ──────────────────────────────────────────────────────────────

    /** Indirizzo PEC (es. "ambiente.provincia.vr@pecveneto.it") */
    @Column(name = "pec", length = 200)
    private String pec;

    /** Email ordinaria (per CC o enti senza PEC) */
    @Column(name = "email", length = 200)
    private String email;

    /** Telefono */
    @Column(name = "telefono", length = 30)
    private String telefono;

    // ── Configurazione comunicazioni ───────────────────────────────────────────

    /**
     * Tipi di comunicazione per cui questo recapito va incluso automaticamente.
     * Formato CSV degli enum {@link ComunicazioneEnte.TipoComunicazione}
     * (es. "NOTIFICA_CAMPIONAMENTO,TRASMISSIONE_DATI_PMC,RELAZIONE_ANNUALE_AIA")
     */
    @Column(name = "tipo_comunicazioni", length = 500)
    private String tipoComunicazioni;

    /** Se true, viene incluso come destinatario primario (TO); altrimenti CC */
    @Column(name = "destinatario_principale")
    private Boolean destinatarioPrincipale = true;

    @Column(nullable = false)
    private Boolean attivo = true;

    // ── Audit ──────────────────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum TipoEnteRecapito {
        PROVINCIA,
        ARPA,
        COMUNE,
        REGIONE,
        MINISTERO_AMBIENTE,
        PREFETTURA,
        ISPETTORATO_LAVORO,
        ASL,
        LABORATORIO_ANALISI,
        INTERNO,        // CC interni (es. colaboratorio@grandimolini.it)
        ALTRO
    }
}
