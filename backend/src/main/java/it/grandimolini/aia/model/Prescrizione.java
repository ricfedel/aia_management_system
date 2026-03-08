package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"documenti"})
@Entity
@Table(name = "prescrizioni")
public class Prescrizione {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prescrizione)) return false;
        Prescrizione that = (Prescrizione) o;
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

    @Column(nullable = false)
    private String codice;

    @Column(nullable = false, length = 1000)
    private String descrizione;

    @Enumerated(EnumType.STRING)
    @Column(name = "matrice_ambientale")
    private MatriceAmbientale matriceAmbientale;

    @Enumerated(EnumType.STRING)
    private StatoPrescrizione stato;

    @Column(name = "data_emissione")
    private LocalDate dataEmissione;

    @Column(name = "data_scadenza")
    private LocalDate dataScadenza;

    @Column(name = "ente_emittente")
    private String enteEmittente;

    @Column(name = "riferimento_normativo", length = 500)
    private String riferimentoNormativo;

    @Column(name = "priorita")
    @Enumerated(EnumType.STRING)
    private Priorita priorita;

    @Column(name = "note", length = 2000)
    private String note;

    @Column(name = "data_chiusura")
    private LocalDate dataChiusura;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "prescrizione", cascade = CascadeType.ALL)
    private List<Documento> documenti = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MatriceAmbientale {
        ARIA, ACQUA, FALDA, RUMORE, SUOLO, RIFIUTI, ILLUMINAZIONE
    }

    public enum StatoPrescrizione {
        APERTA, IN_LAVORAZIONE, IN_ATTESA_INTEGRAZIONE, CHIUSA, SOSPESA
    }

    public enum Priorita {
        BASSA, MEDIA, ALTA, URGENTE
    }
}
