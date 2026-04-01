package it.grandimolini.aia.model;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

/**
 * Template di processo BPMN definito dall'utente e persistito nel database.
 * Contiene l'XML BPMN 2.0 che viene interpretato a runtime dal BpmnParserService
 * per costruire la sequenza di task di ogni istanza di processo.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "definizioni_flusso")
public class DefinizioneFlusso {
    // equals/hashCode basati solo sull'id: evita LazyInitializationException
    // quando le entità vengono usate in HashSet fuori dalla sessione Hibernate.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefinizioneFlusso)) return false;
        DefinizioneFlusso that = (DefinizioneFlusso) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome leggibile del flusso (es. "Rinnovo AIA Impianto A") */
    @Column(nullable = false, length = 200)
    private String nome;

    /** Descrizione dello scopo del flusso */
    @Column(length = 1000)
    private String descrizione;

    /** XML BPMN 2.0 completo, disegnato con l'editor bpmn-js */
    @Column(name = "bpmn_xml", columnDefinition = "TEXT", nullable = false)
    private String bpmnXml;

    /** Versione incrementale del flusso */
    @Column(name = "versione", nullable = false)
    private Integer versione = 1;

    /** Se false, il flusso non è selezionabile per nuove istanze (soft-delete) */
    @Column(name = "attiva", nullable = false)
    private Boolean attiva = true;

    /** Se true, il flusso è predefinito di sistema: visibile ma non modificabile/cancellabile */
    @Column(name = "sistema", nullable = false)
    private Boolean sistema = false;

    /** Utente che ha creato la definizione */
    @Column(name = "creato_da", length = 100)
    private String creatoDa;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt  = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
