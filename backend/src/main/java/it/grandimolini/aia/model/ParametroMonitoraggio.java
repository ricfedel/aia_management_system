package it.grandimolini.aia.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Parametro misurato su un punto di monitoraggio (es. BOD5 su Scarico S1).
 * Contiene il limite autorizzato, la frequenza e il metodo analitico
 * come da Piano di Monitoraggio e Controllo (PMC).
 */
@Data
@Entity
@Table(name = "parametri_monitoraggio")
public class ParametroMonitoraggio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitoraggio_id", nullable = false)
    private Monitoraggio monitoraggio;

    /** Nome del parametro (es. "pH", "BOD5", "COD", "Azoto ammoniacale", "Conta E.Coli") */
    @Column(nullable = false, length = 150)
    private String nome;

    /** Codice breve per export (es. "BOD5", "COD", "NH4") */
    @Column(length = 50)
    private String codice;

    /** Unità di misura (es. "mg/l", "upH", "UFC/100ml", "µg/l") */
    @Column(name = "unita_misura", length = 30)
    private String unitaMisura;

    /** Valore limite autorizzato (numerico; null se non applicabile) */
    @Column(name = "limite_valore")
    private Double limiteValore;

    /** Unità del limite (di solito = unitaMisura, ma può differire) */
    @Column(name = "limite_unita", length = 30)
    private String limiteUnita;

    /** Normativa/articolo di riferimento per il limite (es. "Tab.3 All.5 D.lgs 152/06") */
    @Column(name = "limite_riferimento", length = 200)
    private String limiteRiferimento;

    /** Frequenza del monitoraggio per questo parametro */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequenza")
    private Monitoraggio.FrequenzaMonitoraggio frequenza;

    /** Metodo analitico (es. "APAT CNR IRSA 2090 MAN 29 2003") */
    @Column(name = "metodo_analisi", length = 300)
    private String metodoAnalisi;

    /** Note aggiuntive (es. "solo nei mesi apr-set", "consigliato") */
    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
