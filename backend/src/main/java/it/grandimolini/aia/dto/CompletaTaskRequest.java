package it.grandimolini.aia.dto;

import lombok.Data;

@Data
public class CompletaTaskRequest {

    /** Esito del task: APPROVATO, RIFIUTATO, RICHIEDE_INTEGRAZIONE */
    private String esito;

    /** Commento libero dell'operatore */
    private String commento;

    /** Dati del form compilato (JSON serializzato) */
    private String formDataJson;
}
