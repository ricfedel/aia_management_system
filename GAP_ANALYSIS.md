# Gap Analysis – AIA Management System
**Basata su documenti Promolog 2024 (primo batch)**
Data analisi: 2026-03-07

---

## Documenti analizzati
| File | Tipo | Contenuto |
|---|---|---|
| ALLEGATO 3 - PMC 2024.xlsx | PMC Ufficiale | Report annuale autocontrollo (12 fogli) |
| 24/000213869 (Chelab) | Rapporto di Prova | Acque meteoriche SF2 – 26/03/2024 |
| 24/000655853 (Chelab) | Rapporto di Prova | Acque meteoriche SF2 – 16/10/2024 |
| 379093-265653 (Agrolab) | Rapporto di Prova | Scarico laboratorio – 29/08/2024 |
| EXCEL_379093.xlsx | Dati lab (xml) | Summary Agrolab: 2 campioni scarico |
| comunicazione PEC 26/03 | PEC Campionamento | Notifica a Provincia/ARPAV/Comune |
| ricevuta accettazione PEC | Ricevuta PEC | Conferma invio certificato |
| ricevuta consegna PEC | Ricevuta PEC | Conferma ricezione destinatario |
| Valutazione Impatto Acustico 2024 | Relazione tecnica | 53 pag. – 4 pos. confine + 4 ricettori |

---

## 1. Entità MANCANTI (nuove da creare)

### 1.1 `RapportoProva` ⭐ PRIORITÀ ALTA
I documenti Chelab e Agrolab hanno struttura precisa che non è mappabile su `RilevazioneMisura` (semplice) né su `Documento` (generico).

```
RapportoProva {
  id
  stabilimento_id FK
  monitoraggio_id FK          ← punto di emissione SF2, S1, S2, E1…
  numero_accettazione         ← 24.233533.0001
  numero_rapporto             ← 24/000213869
  laboratorio_nome            ← Chelab S.r.l. / Agrolab Italia
  laboratorio_lab_n           ← LAB N° 0051 L (accreditamento ACCREDIA)
  data_campionamento          ← 26/03/2024
  ora_campionamento           ← 10:00
  data_ricevimento            ← 28/03/2024
  data_emissione              ← 15/04/2024
  matrice                     ← ACQUA_SCARICO / ARIA / ACQUE_METEORICHE
  campionato_da               ← CLIENTE / TECNICO_LAB
  provenienza                 ← "Promolog Srl Via Zurlare 21 Coriano VR"
  descrizione_campione        ← "Acque meteoriche ore 10:00 26/03/2024 SF2"
  verbale_campionamento       ← AM240035/01/02  (solo Agrolab)
  catena_custodia             ← 24/1315  (solo Agrolab)
  conformita_globale          ← CONFORME / NON_CONFORME / PARZIALE
  note_conformita             ← "Tutti i parametri CONFORMI"
  responsabile_chimico        ← Dott.ssa Barbara Scantamburlo
  file_path                   ← allegato PDF
  creato_da
  created_at
  List<RigaRapportoProva>
}

RigaRapportoProva {
  id
  rapporto_prova_id FK
  parametro                   ← Materiali in sospensione, pH, BOD5, COD…
  valore_incertezza           ← "7,10±0,08"
  valore_numerico             ← 7.10
  incertezza                  ← 0.08
  unita_misura                ← mg/l, mg/l(comeO2)
  valore_riferimento          ← "DL 152/06 TAB3 SUP"
  limite_normativo            ← <=80, [5.5-9.5], <=40
  limite_numerico             ← 80.0
  loq                         ← 5.0  (Limit of Quantification)
  rl                          ← limite di quantificazione specifico campione
  metodo_analisi              ← APAT CNR IRSA 2090 B Man 29 2003
  data_inizio_analisi         ← 28/03/2024
  data_fine_analisi           ← 02/04/2024
  unita_operativa             ← 02 (Via Castellana Resana TV)
  riga_numero                 ← 1, 2, 3…
  stato_conformita            ← CONFORME / NON_CONFORME / SOTTO_LOQ / NON_NORMATO
  discostamento_percentuale   ← 19%
}
```

**Perché è separato da `RilevazioneMisura`?**
Un solo `RapportoProva` contiene N parametri (pH, COD, BOD5, SST, idrocarburi…). Ogni parametro è una `RigaRapportoProva`. Questo è il documento ufficiale accreditato ACCREDIA che va conservato integralmente.

---

### 1.2 `ManutenzioneImpianto` – Tabella 2.1.2 PMC
```
ManutenzioneImpianto {
  id
  stabilimento_id FK
  anno, mese
  macchinario                 ← "Filtro a maniche E66", "Impianto depurazione SF1"
  tipo_intervento             ← ORDINARIA / STRAORDINARIA
  data_intervento
  descrizione_intervento
  criticita_riscontrate       ← testo libero
  eseguita_da                 ← interno / ditta esterna
  note
  created_at
}
```

---

### 1.3 `ControlloProcesso` – Tabelle 2.1.1 / 2.1.3 / 2.1.4 PMC
```
ControlloProcesso {
  id
  stabilimento_id FK
  monitoraggio_id FK (nullable) ← per 2.1.3 (abbattimento) e 2.1.4 (depurazione)
  tipo_tabella                ← FASE_CRITICA / SISTEMA_ABBATTIMENTO / SISTEMA_DEPURAZIONE
  fase_processo               ← "Prepulitura", "Filtratura aria compressa"
  sistema                     ← "Filtro a maniche", "Impianto Imhoff"
  parametro_controllo         ← "Delta P", "portata effluente", "pressione"
  risultato_controllo         ← valore misurato
  unita_misura
  data_controllo
  conforme                    ← bool
  commenti
  created_at
}
```
> **Nota**: per i camini E66/E91-E96 il PMC richiede controllo mensile del ΔP (deprimometri) al posto dell'analisi analitica annuale. Questo è un `ControlloProcesso` con parametro "Delta P" mensile.

---

### 1.4 `IndicatorePerformance` – Tabella 3.1 PMC
```
IndicatorePerformance {
  id
  stabilimento_id FK
  anno
  indicatore                  ← enum: CONSUMO_SPEC_ENERGIA, POLVERI_SU_GRANO,
                                       ACQUA_BAGNATURA_SU_FARINA, RIFIUTI_SU_GRANO,
                                       CONSUMO_METANO, PERC_RIFIUTI_RECUPERO, …
  descrizione                 ← "Consumo specifico di energia"
  valore
  unita_misura                ← MWh/t, Kwh/t, l/t, Kg/t, m3/t, %
  calcolato_automaticamente   ← bool
  formula                     ← es. "energia_rete / grano_macinato"
  note
}
```

---

### 1.5 `RecapitoEnte` – Rubrica PEC degli enti
Dal documento PEC vediamo che ogni comunicazione campionamento SF2 va a:
- `ambiente.provincia.vr@pecveneto.it` (Provincia Verona – posta cert.)
- `dapvr@pec.arpav.it` (ARPAV Dipartimento Verona – posta cert.)
- `protocollo.albaredodadige@pec.it` (Comune Albaredo d'Adige – posta cert.)
- `colaboratorio@grandimolini.it` (interno – posta ord.)
- `mpasetto@grandimolini.it` (interno – posta ord.)

```
RecapitoEnte {
  id
  stabilimento_id FK
  ente_tipo                   ← PROVINCIA / ARPA / COMUNE / REGIONE / INTERNO
  ente_nome                   ← "Provincia di Verona – Settore Ambiente"
  ufficio                     ← "U.O. Servizio Tutela e Valorizzazione Ambientale"
  referente                   ← "Alessandro Iseppi"
  pec                         ← "ambiente.provincia.vr@pecveneto.it"
  email                       ← email ordinaria
  tipo_comunicazioni          ← CSV enum: AUTOCONTROLLO,RELAZIONE_ANNUALE,NC,...
  attivo                      ← bool
}
```

---

### 1.6 `AreaStoccaggio` – Tabella 2.1.5 PMC *(bassa priorità)*
```
AreaStoccaggio {
  id
  stabilimento_id FK
  denominazione               ← "Deposito temporaneo rifiuti pericolosi"
  tipo                        ← RIFIUTI_PERICOLOSI / MATERIE_PRIME / PRODOTTI_CHIMICI
  capacita_max
  unita_misura
  ultima_verifica
  esito_verifica              ← OK / NON_CONFORME
  note
}
```

---

## 2. Attributi MANCANTI su entità esistenti

### 2.1 `RilevazioneMisura` (incompleta vs rapporti di prova reali)
| Campo mancante | Esempio reale | Note |
|---|---|---|
| `numero_accettazione` | 24.233533.0001 | Identificativo accettazione lab |
| `numero_rapporto` | 24/000213869 | Numero RdP ufficiale |
| `data_ricevimento_campione` | 28/03/2024 | Data arrivo campione al lab |
| `data_emissione_rapporto` | 15/04/2024 | Data emissione RdP firmato |
| `ora_campionamento` | 10:00 | Ora prelievo |
| `campionato_da` | CLIENTE / TECNICO_LAB | Chi ha prelevato |
| `verbale_campionamento` | AM240035/01/02 | Numero verbale prelievo |
| `catena_custodia` | 24/1315 | Numero catena di custodia |
| `incertezza` | ±0.08 (per pH) | Incertezza estesa |
| `loq` | 5.0 | Limite di quantificazione |
| `valore_riferimento_normativo` | DL 152/06 TAB3 SUP | Norma per il limite |
| `flusso_massa` | 41.325 | Per emissioni aria: kg/anno |
| `um_flusso` | kg/anno | UM del flusso massa |
| `discostamento_percentuale` | 19% | (valore/limite - 1) * 100 |
| `rapporto_prova_id` FK | → RapportoProva | Collegamento al documento |

> **Alternativa**: se si crea `RapportoProva` + `RigaRapportoProva`, molti di questi campi vanno nella nuova entità e `RilevazioneMisura` diventa un link/summary.

---

### 2.2 `VoceProduzione` (PMC sezione materie prime / energia)
| Campo mancante | Esempio | Note |
|---|---|---|
| `tep` | 92.103 TEP | Tonnellate equivalenti petrolio (solo energia) |
| `CategoriaVoce.ENERGIA_RINNOVABILE` | *(enum mancante)* | Fotovoltaico |
| `CategoriaVoce.ALTRO_MATERIALE` | *(enum mancante)* | Tab 1.1.2: carta, film, pallet… |
| `CategoriaVoce.PRODOTTO_FINITO` | *(enum mancante)* | Farina sfusa, semola prodotta |
| `quantita_anno_corrente_mensile` | già presente ✓ | — |
| `codice_fonte_energia` | *(nice to have)* | Per calcolo TEP (kWh→TEP) |

---

### 2.3 `ComunicazioneEnte` (incompleta per workflow PEC campionamento)
| Campo mancante | Esempio | Note |
|---|---|---|
| `monitoraggio_id` FK | → Monitoraggio.SF2 | Il punto che ha generato la comunicazione |
| `TipoComunicazione.NOTIFICA_CAMPIONAMENTO` | *(enum mancante)* | "Campionamento autocontrollo SF2" |
| `pec_message_id_accettazione` | 31C75CE9.01DF9460… | ID PEC ricevuta accettazione |
| `pec_message_id_consegna` | opec21023… | ID PEC ricevuta consegna |
| `file_ricevuta_accettazione` | path PDF | |
| `file_ricevuta_consegna` | path PDF | |
| `destinatari_multipli` | *(struttura attuale: 1 ente)* | La PEC va a 3 enti contemporaneamente |

> **Problema strutturale**: l'entità ha un campo `ente` (singolo enum). La PEC reale ha **3 destinatari PEC + 2 CC email ordinarie** in un unico invio. Serve o un campo CSV `destinatari_ids` (FK a `RecapitoEnte`) o una tabella associativa `comunicazione_destinatari`.

---

### 2.4 `Stabilimento`
| Campo mancante | Esempio | Note |
|---|---|---|
| `indirizzo_pec` | promolog@legalmail.it | PEC mittente delle comunicazioni |
| `telefono` | 045 6629125 | — |
| `fax` | 045 7025031 | — |
| `compilatore_pmc` | "Mauro Pasetto" | Chi firma il report annuale (può differire dal referente AIA) |
| `codice_intestatario_lab` | 0008347/008 (Chelab), 65633 (Agrolab) | Codice cliente presso laboratori di analisi |

---

### 2.5 `Monitoraggio`
| Campo mancante | Esempio | Note |
|---|---|---|
| `destinazione_recapito` | "acque superficiali" / "fognatura comunale" | Per scarichi idrici |
| `tipo_scarico` | REFLUO_CIVILE / ACQUE_METEORICHE | Distinzione interna a SCARICHI_IDRICI |
| `ore_giorno` | 10 / 24 | Durata emissione h/giorno (tab 1.5.1 PMC) |
| `giorni_anno` | 250 / 365 | Durata emissione gg/anno (tab 1.5.1 PMC) |
| `portata_scarico_m3d` | m3/giorno | Per calcolo carico inquinante (kg/d) |
| `codice_pmc` | 1, 2, 3, … / SF1, SF2 | Codice numerico nel modello PMC (diverso da E1/S1 AIA) |

---

### 2.6 `Documento`
| Campo mancante | Note |
|---|---|
| `numero_accettazione_lab` | Per rapporti di prova: collegamento al campione |
| `data_campionamento` | Per rapporti di prova |
| `monitoraggio_id` FK | Collegamento al punto di monitoraggio |
| `TipoDocumento.RAPPORTO_DI_PROVA` | Se non già presente |
| `TipoDocumento.VALUTAZIONE_ACUSTICA` | — |
| `TipoDocumento.PEC_RICEVUTA` | Ricevute accettazione/consegna |

---

## 3. Funzionalità MANCANTI (feature non implementate)

### 3.1 ⭐ Workflow guidato "Campionamento + PEC + RdP" (ALTA PRIORITÀ)
Il processo reale documentato è:
```
1. Pianificare data campionamento (es. 26/03/2024 ore 10:00)
2. Redigere comunicazione PEC agli enti (Provincia+ARPAV+Comune)
3. Inviare via PEC → conservare ricevuta accettazione
4. Confermare avvenuta consegna → conservare ricevuta consegna
5. Consegnare campione al laboratorio / farsi venire il tecnico lab
6. Ricevere RdP (PDF) → caricare nel sistema
7. Registrare risultati analitici → verifica automatica conformità
8. Aggiornare PMC mensile (voce EMISSIONI IN ACQUA tab 1.6.2)
```
**Non esiste un flusso guidato che colleghi questi passi.** La `Scadenza` + `ComunicazioneEnte` + `RilevazioneMisura` esistono separatamente ma senza un workflow integrato.

---

### 3.2 ⭐ Generazione Report PMC Annuale (Allegato 3) (ALTA PRIORITÀ)
Il PMC xlsx è obbligatorio per legge (invio entro 30 aprile a Provincia, ARPAV, Comune).
- I dati di input esistono nel sistema (RegistroMensile, VoceProduzione, MovimentoRifiuto, RilevazioneMisura)
- **Manca**: funzionalità di generazione xlsx nel formato ufficiale regionale
- **Mancano**: sezioni non ancora modellate (tab 2.1.x Gestionale, tab 3.1 Indicatori)
- **Mancano**: calcoli automatici (indicatori di performance = formula su dati mensili)

---

### 3.3 Gestione strutturata Valutazione Impatto Acustico (MEDIA PRIORITÀ)
La relazione 2024 (53 pag.) ha dati strutturati:
- 4 posizioni a confine stabilimento (E1D/N, E2D/N, E3D/N, E4D/N)
- 4 posizioni presso ricettori (R1D/N, R2D/N, R3D/N, R4D/N)
- Per ogni posizione: Leq, componenti tonali, componenti impulsive
- Classificazione acustica area (classe II/III)
- Confronto con limiti diurni/notturni

Attualmente è solo un `Documento` (file upload generico). Per poterla inserire nel PMC (foglio RUMORE tab 1.7) serve struttura dati.

**Proposta**: Aggiungere a `RilevazioneMisura`:
- `periodo` (DIURNO/NOTTURNO)
- `posizione_misura` (es: "E1", "R2")
- `tipo_componente` (GLOBALE/TONALE/IMPULSIVO)

---

### 3.4 Rubrica enti con destinatari PEC preimpostati (MEDIA PRIORITÀ)
Quando si crea una `ComunicazioneEnte` di tipo `NOTIFICA_CAMPIONAMENTO` per SF2 Promolog, il sistema dovrebbe proporre automaticamente i 3 destinatari PEC istituzionali + i CC interni, senza doverli reinserire ogni volta.

**Dipende da**: creazione di `RecapitoEnte` (§ 1.5).

---

### 3.5 Calcolo automatico discostamento % dal VLE (MEDIA PRIORITÀ)
Il PMC xlsx calcola `discostamento % = (concentrazione_misurata / limite) * 100` e colora le celle in rosso se > 100%.
Nel sistema manca:
- Calcolo automatico al salvataggio della `RilevazioneMisura` / `RigaRapportoProva`
- Widget dashboard "discostamento dal limite" per i punti attivi

---

### 3.6 Import dati da Excel laboratorio (Agrolab EXCEL_379093) (BASSA PRIORITÀ)
Agrolab fornisce i risultati anche in formato XML/Excel oltre al PDF. Il file ha struttura fissa (ordine, n. campione, data, parametri, valori). Un parser potrebbe importare automaticamente i risultati, evitando inserimento manuale.

---

## 4. Riepilogo priorità

| # | Cosa | Tipo | Priorità |
|---|---|---|---|
| 1 | `RapportoProva` + `RigaRapportoProva` | Nuova entità | ⭐ ALTA |
| 2 | Workflow campionamento → PEC → RdP | Feature | ⭐ ALTA |
| 3 | Generazione PMC xlsx (Allegato 3) | Feature | ⭐ ALTA |
| 4 | `RecapitoEnte` + destinatari multipli PEC | Nuova entità | MEDIA |
| 5 | `ManutenzioneImpianto` | Nuova entità | MEDIA |
| 6 | `ControlloProcesso` (ΔP mensile E66/E91-E96) | Nuova entità | MEDIA |
| 7 | `IndicatorePerformance` + calcolo automatico | Nuova entità | MEDIA |
| 8 | Campi mancanti in `RilevazioneMisura` | Attributi | MEDIA |
| 9 | Campi mancanti in `ComunicazioneEnte` | Attributi | MEDIA |
| 10 | Campi mancanti in `Stabilimento` | Attributi | BASSA |
| 11 | `VoceProduzione` – enum mancanti + TEP | Attributi | BASSA |
| 12 | `Monitoraggio` – codice PMC / ore-giorni / portata | Attributi | BASSA |
| 13 | Calcolo discostamento % VLE | Feature | MEDIA |
| 14 | `AreaStoccaggio` | Nuova entità | BASSA |
| 15 | Import Excel lab (Agrolab XML) | Feature | BASSA |
| 16 | Struttura misure acustiche in `RilevazioneMisura` | Attributi | BASSA |

---

## 5. Note finali
- Questi sono i **primi documenti** caricati (Promolog 2024). Attesi altri batch.
- Le entità `ComunicazioneEnte`, `RegistroMensile`, `CodiceRifiuto`, `MovimentoRifiuto` esistono già e **coprono bene** le aree rifiuti, energia, materie prime e comunicazioni.
- La maggior parte dei gap riguarda la **gestione dei Rapporti di Prova** (entità principale mancante) e il **workflow PEC campionamento** (processo non guidato).
- Il **PMC annuale** è il documento più complesso: richiede dati da quasi tutte le entità del sistema.
