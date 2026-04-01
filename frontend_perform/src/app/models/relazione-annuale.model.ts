export interface PrescrRow {
  id?:           number;
  numero?:       string;
  descrizione?:  string;
  tipo?:         string;
  stato?:        string;
  dataScadenza?: string;
}

export interface MonRow {
  id?:             number;
  codice?:         string;
  descrizione?:    string;
  tipo?:           string;
  numParametri?:   number;
  frequenza?:      string;
  parametriNomi?:  string[];
}

export interface ConformRow {
  monitoraggioCodice?: string;
  parametroNome?:      string;
  unita?:              string;
  valoreMisurato?:     number;
  limiteValore?:       number;
  percLimite?:         number;
  stato?:              string;
  dataUltimaRilev?:    string;
}

export interface RifiutoRow {
  codiceCer?:   string;
  descrizione?: string;
  pericoloso?:  boolean;
  qProdotta?:   number;
  qSmaltita?:   number;
  qRecuperata?: number;
  qCeduta?:     number;
  unita?:       string;
}

export interface ProdRow {
  categoria?:              string;
  descrizione?:            string;
  codice?:                 string;
  totaleAnno?:             number;
  totaleAnnoPrecedente?:   number;
  variazione?:             number;
  unita?:                  string;
}

export interface ComRow {
  id?:           number;
  tipo?:         string;
  ente?:         string;
  oggetto?:      string;
  dataInvio?:    string;
  stato?:        string;
  hasRiscontro?: boolean;
}

export interface RelazioneAnnualeDTO {
  stabilimentoId?:       number;
  stabilimentoNome?:     string;
  stabilimentoIndirizzo?: string;
  stabilimentoCodiceAIA?: string;
  anno?:                 number;
  dataGenerazione?:      string;

  // Prescrizioni
  totalePrescrizioni?:     number;
  prescrizioniScadute?:    number;
  prescrizioniInScadenza?: number;
  prescrizioni?:           PrescrRow[];

  // Monitoraggio
  totalePuntiMonitoraggio?: number;
  puntiMonitoraggio?:       MonRow[];

  // Conformità
  totaleRilevazioni?:  number;
  rilevConformi?:      number;
  rilevAttenzione?:    number;
  rilevNonConformi?:   number;
  percConformita?:     number;
  topNonConformi?:     ConformRow[];

  // Rifiuti
  rifiutiRiepilogo?: RifiutoRow[];

  // Produzione
  produzioneRiepilogo?: ProdRow[];
  mesiConDati?:         string[];

  // Comunicazioni
  totaleComunicazioni?:        number;
  comunicazioniInviate?:       number;
  comunicazioniConRiscontro?:  number;
  comunicazioni?:              ComRow[];
}
