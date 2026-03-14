package it.grandimolini.aia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"prescrizioni", "monitoraggi", "utenti"})
@Entity
@Table(name = "stabilimenti")
public class Stabilimento {

    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stabilimento)) return false;
        Stabilimento that = (Stabilimento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String citta;

    private String indirizzo;

    @Column(name = "numero_aia")
    private String numeroAIA;

    @Column(name = "data_rilascio_aia")
    private LocalDate dataRilascioAIA;

    @Column(name = "data_scadenza_aia")
    private LocalDate dataScadenzaAIA;

    @Column(name = "ente_competente")
    private String enteCompetente;

    @Column(name = "responsabile_ambientale")
    private String responsabileAmbientale;

    private String email;

    private String telefono;

    private String fax;

    /** Indirizzo PEC dello stabilimento (es. "promolog@legalmail.it") */
    @Column(name = "indirizzo_pec", length = 200)
    private String indirizzoPec;

    /**
     * Nome del compilatore del PMC annuale
     * (es. "Mauro Pasetto" – può differire dal responsabile AIA).
     */
    @Column(name = "compilatore_pmc", length = 200)
    private String compilatorePmc;

    /**
     * Codice cliente presso i laboratori di analisi accreditati.
     * Formato CSV per più laboratori: "Chelab:0008347/008,Agrolab:65633"
     */
    @Column(name = "codice_intestatario_lab", length = 300)
    private String codiceIntestatarioLab;

    /** Sigla breve usata nell'import Excel (es. LI, VE, CO, CD) */
    @Column(length = 10)
    private String sigla;

    @Column(nullable = false)
    private Boolean attivo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "stabilimento", cascade = CascadeType.ALL)
    private List<Prescrizione> prescrizioni = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "stabilimento", cascade = CascadeType.ALL)
    private List<Monitoraggio> monitoraggi = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "stabilimenti")
    private Set<User> utenti = new HashSet<>();
}
