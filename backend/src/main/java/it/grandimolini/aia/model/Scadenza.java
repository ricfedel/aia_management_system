package it.grandimolini.aia.model;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "scadenze")
public class Scadenza {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scadenza)) return false;
        Scadenza that = (Scadenza) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stabilimento_id")
    private Stabilimento stabilimento;

    @ManyToOne
    @JoinColumn(name = "prescrizione_id")
    private Prescrizione prescrizione;

    @ManyToOne
    @JoinColumn(name = "monitoraggio_id")
    private Monitoraggio monitoraggio;

    @Column(nullable = false)
    private String titolo;

    @Column(length = 1000)
    private String descrizione;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_scadenza")
    private TipoScadenza tipoScadenza;

    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    @Enumerated(EnumType.STRING)
    private StatoScadenza stato;

    @Enumerated(EnumType.STRING)
    private Priorita priorita;

    @Column(name = "responsabile")
    private String responsabile;

    @Column(name = "email_notifica")
    private String emailNotifica;

    @Column(name = "giorni_preavviso")
    private Integer giorniPreavviso = 20;

    @Column(name = "data_completamento")
    private LocalDate dataCompletamento;

    @Column(name = "note", length = 1000)
    private String note;

    /** Data entro la quale bisogna attivarsi (colonna Excel omonima) */
    @Column(name = "data_prevista_attivazione")
    private LocalDate dataPrevistaAttivazione;

    /** Riferimento normativo/PMC (es. "PMC", "Art. 2 Altre prescrizioni") */
    @Column(name = "riferimento", length = 500)
    private String riferimento;

    /** Sigla stabilimento di origine dall'import (es. LI, VE) — tracciabilità */
    @Column(name = "sito_origine", length = 10)
    private String sitoOrigine;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (stato == null) {
            stato = StatoScadenza.PENDING;
        }
    }

    public enum TipoScadenza {
        MONITORAGGIO_PMC,        // Campionamento autocontrollo (aria, acqua, acustico)
        RELAZIONE_ANNUALE,       // Invio relazione annuale AIA / PMC
        INTEGRAZIONE_ENTE,       // Risposta a richiesta di integrazione
        RINNOVO_AIA,             // Rinnovo / riesame AIA
        COMUNICAZIONE,           // Comunicazione PEC generica agli enti
        ADEMPIMENTO_PRESCRIZIONE,// Intervento tecnico prescritto dall'AIA
        MANUTENZIONE,            // Manutenzione impianti (es. depuratori, filtri)
        PAGAMENTO,               // Contributi/tariffe (es. ARPAV, CSQA)
        FORMAZIONE,              // Formazione personale
        ALTRO
    }

    public enum StatoScadenza {
        PENDING,
        IN_CORSO,
        COMPLETATA,
        SCADUTA
    }

    public enum Priorita {
        BASSA, MEDIA, ALTA, URGENTE
    }
}
