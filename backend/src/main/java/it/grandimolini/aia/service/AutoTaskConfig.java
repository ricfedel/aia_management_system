package it.grandimolini.aia.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Configurazione unificata per tutti i tipi di step automatici.
 * Serializzata come JSON nel campo autoTaskConfig di TaskProcesso.
 *
 * tipo:
 *   API_CALL  - chiamata HTTP generica (url, method, body, headers)
 *   EMAIL     - invio email (emailTo, emailSubject, emailBody)
 *   TIMER     - attesa (delay: "30m", "2h", "1d")
 *   SCRIPT    - azione interna (action, params JSON)
 *   WEBHOOK   - POST con HMAC signature (url, webhookSecret, body)
 *   GENERA_PDF- genera PDF riepilogo processo (template: RIEPILOGO_PROCESSO)
 *
 * outputVar (opzionale, per API_CALL e WEBHOOK):
 *   Se valorizzato, la response viene parsata (se JSON) e salvata in
 *   ProcessoDocumento.variabiliJson sotto questa chiave.
 *   Utile per esporre il risultato all'API esterna o dargli un nome semantico.
 *   Nota: la comunicazione inter-step è già disponibile tramite buildStepContext()
 *   con la sintassi ${Task_NomeStep.campo} senza bisogno di outputVar.
 *
 * Esempio:
 *   { "tipo": "API_CALL", "url": "/api/rilevazioni/calcola", "outputVar": "risultatoCalcolo" }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AutoTaskConfig(
    String tipo,
    // API_CALL / WEBHOOK
    String url,
    String method,
    String body,
    String headers,
    String webhookSecret,
    // EMAIL
    String emailTo,
    String emailSubject,
    String emailBody,
    // TIMER
    String delay,
    // SCRIPT
    String action,
    String params,
    // GENERA_PDF
    String template,
    // OUTPUT (API_CALL / WEBHOOK)
    // Se valorizzato, la response viene persistita in ProcessoDocumento.variabiliJson sotto questa chiave
    String outputVar
) {}
