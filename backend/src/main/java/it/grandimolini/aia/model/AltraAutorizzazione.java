package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "altre_autorizzazioni")
public class AltraAutorizzazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAltraAutorizzazione tipo;

    @Column(name = "numero_autorizzazione", length = 200)
    private String numeroAutorizzazione;

    @Column(name = "ente_competente", length = 255)
    private String enteCompetente;

    @Column(name = "data_rilascio")
    private LocalDate dataRilascio;

    @Column(name = "data_scadenza")
    private LocalDate dataScadenza;

    @Column(length = 1000)
    private String descrizione;

    public enum TipoAltraAutorizzazione {
        SCARICO_ACQUE, BONIFICHE, EMISSIONI_ATMOSFERA, RIFIUTI, ACUSTICA, ALTRO
    }
}
