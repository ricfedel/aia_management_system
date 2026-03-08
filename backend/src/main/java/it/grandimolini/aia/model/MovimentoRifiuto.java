package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Singolo movimento di rifiuto: produzione, smaltimento, recupero, cessione terzi.
 * Legato a un CodiceRifiuto e identificato da anno/mese.
 */
@Data
@Entity
@Table(name = "movimenti_rifiuto")
public class MovimentoRifiuto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "codice_rifiuto_id", nullable = false)
    private CodiceRifiuto codiceRifiuto;

    @Column(nullable = false)
    private Integer anno;

    /** 1-12, null per movimenti annuali/straordinari */
    private Integer mese;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimento", nullable = false)
    private TipoMovimento tipoMovimento;

    @Column(nullable = false)
    private Double quantita;

    @Column(name = "unita_misura", length = 10)
    private String unitaMisura;

    /** Codice operazione: R1-R13 (recupero) o D1-D15 (smaltimento) */
    @Column(name = "codice_operazione", length = 10)
    private String codiceOperazione;

    /** Ragione sociale destinatario */
    @Column(name = "destinatario", length = 300)
    private String destinatario;

    /** Ragione sociale trasportatore */
    @Column(name = "trasportatore", length = 300)
    private String trasportatore;

    /** Numero FIR (Formulario Identificazione Rifiuto) */
    @Column(name = "numero_fir", length = 50)
    private String numeroFir;

    /** Data effettiva operazione (es. data ritiro) */
    @Column(name = "data_operazione")
    private LocalDate dataOperazione;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TipoMovimento {
        PRODUZIONE,          // quantità prodotta nel mese
        SMALTIMENTO,         // avviato a smaltimento (D-codes)
        RECUPERO,            // avviato a recupero (R-codes)
        CESSIONE_TERZI,      // ceduto a terzi
        DEPOSITO_TEMPORANEO  // in deposito temporaneo a fine periodo
    }
}
