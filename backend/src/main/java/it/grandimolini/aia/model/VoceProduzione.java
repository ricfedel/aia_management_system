package it.grandimolini.aia.model;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * Singola voce di produzione/consumo all'interno di un RegistroMensile.
 * Copre materie prime, consumi idrici (per contatore), consumi energetici.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "voci_produzione")
public class VoceProduzione {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoceProduzione)) return false;
        VoceProduzione that = (VoceProduzione) o;
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
    @JoinColumn(name = "registro_mensile_id", nullable = false)
    private RegistroMensile registroMensile;

    /** Categoria della voce */
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaVoce categoria;

    /**
     * Descrizione specifica:
     * - Materie prime: "Grano Tenero", "Grano Duro", "Mais", "Orzo", "Altri Cereali"
     * - Acqua: codice contatore "M1", "M2", "M3" + descrizione
     * - Energia: "F1 - Punta", "F2 - Intermedia", "F3 - Fuori Punta", "Autoprodotta"
     * - Gas, Gasolio, Produzione: descrizione libera
     */
    @Column(nullable = false, length = 200)
    private String descrizione;

    /** Codice breve (es. "M1", "F1", "GT") — opzionale */
    @Column(length = 20)
    private String codice;

    /** Quantità consumata/prodotta */
    @Column(name = "quantita")
    private Double quantita;

    /** Unità di misura: t, m³, kWh, Nm³, l, MWh */
    @Column(name = "unita_misura", length = 20)
    private String unitaMisura;

    /** Quantità dell'anno precedente (per confronto %) */
    @Column(name = "quantita_anno_precedente")
    private Double quantitaAnnoPrecedente;

    /**
     * Tonnellate equivalenti petrolio (solo per voci energetiche).
     * Calcolato come: quantita_kWh * 0.0000861 (per energia elettrica)
     * o quantita_Nm3 * 0.000822 (per gas naturale).
     */
    @Column(name = "tep")
    private Double tep;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum CategoriaVoce {
        MATERIA_PRIMA,
        PRODUZIONE_OUTPUT,
        PRODOTTO_FINITO,       // Farina sfusa, semola prodotta (tab 1.1.2 PMC)
        ACQUA,
        ENERGIA_ELETTRICA,
        ENERGIA_RINNOVABILE,   // Energia fotovoltaica autoprodotta
        GAS_NATURALE,
        GASOLIO,
        ALTRO_MATERIALE,       // Carta, film, pallet, materiali ausiliari (tab 1.1.2)
        ALTRO
    }
}
