package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Monitoraggio.TipoMonitoraggio;
import it.grandimolini.aia.model.Monitoraggio.FrequenzaMonitoraggio;
import it.grandimolini.aia.model.RapportoProva.ConformitaGlobale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatoCampionamentoDTO {

    private Long monitoraggioId;
    private String codice;
    private String descrizione;
    private TipoMonitoraggio tipoMonitoraggio;
    private FrequenzaMonitoraggio frequenza;
    private Long stabilimentoId;
    private String stabilimentoNome;

    // Ultimo rapporto di prova
    private LocalDate dataUltimoCampionamento;
    private String numeroUltimoRapporto;
    private ConformitaGlobale conformitaUltimo;

    // Prossima scadenza dal campo Monitoraggio.prossimaScadenza
    private LocalDate prossimaScadenza;
    private Integer giorniAllaScadenza;

    // Semaforo: VERDE, GIALLO, ROSSO
    private String statoColore;
    private String statoDescrizione;
}
