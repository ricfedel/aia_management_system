export interface DashboardStats {
  totalStabilimenti: number;
  stabilimentiAttivi: number;
  totalPrescrizioni: number;
  prescrizioniPerStato: { [key: string]: number };
  scadenzeImminenti: number;
  scadenzeScadute: number;
  scadenzeCompletate: number;
  totalDatiAmbientali: number;
  datiNonConformi: number;
  datiInAttenzione: number;
  percentualeConformita: number;
}

export interface StabilimentoStats {
  stabilimentoId: number;
  nomeStabilimento: string;
  totalPrescrizioni: number;
  prescrizioniAperte: number;
  prescrizioniChiuse: number;
  prossimeScadenze: number;
  totalDatiAmbientali: number;
  percentualeConformita: number;
}

export interface ScadenzaImminente {
  id: number;
  titolo: string;
  descrizione?: string;
  stabilimentoNome: string;
  dataScadenza: string;
  giorniRimanenti: number;
  priorita: string;
  stato: string;
}

export interface ConformitaTrend {
  anno: number;
  mese: number;
  totaleMisurazioni: number;
  misurazioniConformi: number;
  misurazioniNonConformi: number;
  percentualeConformita: number;
}
