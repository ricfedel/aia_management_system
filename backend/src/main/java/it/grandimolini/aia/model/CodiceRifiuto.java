package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Anagrafica dei codici CER (Catalogo Europeo Rifiuti) usati da uno stabilimento.
 * Ogni stabilimento ha il proprio catalogo di rifiuti prodotti/gestiti.
 */
@Getter
@Setter
@ToString(exclude = {"movimenti"})
@Entity
@Table(name = "codici_rifiuto")
public class CodiceRifiuto {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodiceRifiuto)) return false;
        CodiceRifiuto that = (CodiceRifiuto) o;
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
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    /** Codice CER es. "19 12 12", "15 01 01" */
    @Column(name = "codice_cer", nullable = false, length = 20)
    private String codiceCer;

    @Column(nullable = false, length = 500)
    private String descrizione;

    /** Rifiuto pericoloso (asterisco CER) */
    @Column(nullable = false)
    private Boolean pericoloso = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_fisico")
    private StatoFisicoRifiuto statoFisico;

    /** Unità di misura predefinita: t, kg, m³, l */
    @Column(name = "unita_misura", length = 10)
    private String unitaMisura = "t";

    /** Principale codice di recupero/smaltimento associato (R5, D15…) */
    @Column(name = "codice_gestione", length = 10)
    private String codiceGestione;

    /** Destinatario abituale */
    @Column(name = "destinatario_abituale", length = 300)
    private String destinatarioAbituale;

    @Column(name = "note", length = 500)
    private String note;

    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "codiceRifiuto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimentoRifiuto> movimenti = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum StatoFisicoRifiuto {
        SOLIDO,
        LIQUIDO,
        FANGOSO,
        GASSOSO,
        ALTRO
    }
}
