package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro mensile di produzione e consumi per uno stabilimento.
 * Ogni record copre un singolo mese (anno + mese) per un singolo stabilimento.
 */
@Data
@Entity
@Table(
    name = "registri_mensili",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_registro_stabilimento_anno_mese",
        columnNames = {"stabilimento_id", "anno", "mese"}
    )
)
public class RegistroMensile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stabilimento_id", nullable = false)
    private Stabilimento stabilimento;

    @Column(nullable = false)
    private Integer anno;

    /** 1 = gennaio … 12 = dicembre */
    @Column(nullable = false)
    private Integer mese;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false)
    private StatoRegistro stato = StatoRegistro.BOZZA;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "compilato_da", length = 200)
    private String compilatoDa;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "registroMensile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoceProduzione> voci = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum StatoRegistro {
        BOZZA,
        INVIATO,
        APPROVATO,
        RETTIFICATO
    }
}
