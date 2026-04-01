package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Monitoraggio.FrequenzaMonitoraggio;
import it.grandimolini.aia.model.Monitoraggio.TipoMonitoraggio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MonitoraggioDTO {
    private Long id;
    private Long stabilimentoId;
    private String stabilimentoNome;
    private String codice;
    private String descrizione;
    private TipoMonitoraggio tipoMonitoraggio;
    private String puntoEmissione;
    private FrequenzaMonitoraggio frequenza;
    private LocalDate prossimaScadenza;
    private String laboratorio;
    private String metodica;
    private String normativaRiferimento;
    private String matricola;
    private Boolean attivo;
    private LocalDateTime createdAt;
    private List<ParametroMonitoraggioDTO> parametri;

    /** ID dell'anagrafica camino associata (solo per EMISSIONI_ATMOSFERA) */
    private Long anagraficaCaminoId;

    /** Snapshot dei dati tecnici principali del camino, per evitare un secondo fetch nel frontend */
    private AnagraficaCaminoDTO anagraficaCamino;
}
