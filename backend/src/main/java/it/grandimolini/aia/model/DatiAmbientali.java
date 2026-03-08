package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dati_ambientali")
public class DatiAmbientali {

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
