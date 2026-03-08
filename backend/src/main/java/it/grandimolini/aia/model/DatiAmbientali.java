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
@Table(name = "dati_ambientali")
public class DatiAmbientali {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatiAmbientali)) return false;
        DatiAmbientali that = (DatiAmbientali) o;
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
    @JoinColumn(name = "monitoraggio_id", nullable = false)
    private Monitoraggio monitoraggio;

    @Column(name = "data_campionamento", nullable = false)
    private LocalDate dataCampionamento;

    @Column(nullable = false)
    private String parametro;

    @Column(name = "valore_misurato")
    private Double valoreMisurato;

    @Column(name = "unita_misura")
    private String unitaMisura;

    @Column(name = "limite_autorizzato")
    private Double limiteAutorizzato;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_conformita")
    private StatoConformita statoConformita;

    @Column(name = "rapporto_prova")
    private String rapportoProva;

    @Column(name = "laboratorio")
    private String laboratorio;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calcolaConformita();
    }

    @PreUpdate
    protected void onUpdate() {
        calcolaConformita();
    }

    private void calcolaConformita() {
        if (valoreMisurato != null && limiteAutorizzato != null) {
            if (valoreMisurato <= limiteAutorizzato * 0.8) {
                statoConformita = StatoConformita.CONFORME;
            } else if (valoreMisurato <= limiteAutorizzato) {
                statoConformita = StatoConformita.ATTENZIONE;
            } else {
                statoConformita = StatoConformita.NON_CONFORME;
            }
        }
    }

    public enum StatoConformita {
        CONFORME,        // Verde
        ATTENZIONE,      // Giallo
        NON_CONFORME     // Rosso
    }
}
