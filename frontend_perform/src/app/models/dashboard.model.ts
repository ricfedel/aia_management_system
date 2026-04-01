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
  tipoScadenza: string;
  priorita: string;
  stato: string;
  responsabile?: string;
}

export interface StatoCampionamento {
  monitoraggioId: number;
  codice: string;
  descrizione: string;
  tipoMonitoraggio: string;
  frequenza: string;
  stabilimentoId: number;
  stabilimentoNome: string;
  dataUltimoCampionamento?: string;
  numeroUltimoRapporto?: string;
  conformitaUltimo?: string;
  prossimaScadenza?: string;
  giorniAllaScadenza?: number;
  statoColore: 'VERDE' | 'GIALLO' | 'ROSSO';
  statoDescrizione: string;
}

export interface KpiAmbientale {
  stabilimentoId: number;
  stabilimentoNome: string;
  annoRiferimento: number;
  consumoIdrico: number;
  consumoEletrico: number;
  consumoGas: number;
  produzioneTotale: number;
  consumoIdricoSpecifico: number;
  consumoEletricoSpecifico: number;
  consumoGasSpecifico: number;
  rifiutiTotali: number;
  rifiutiPericolosi: number;
  limiteRifiutiTotali: number;
  limiteRifiutiPericolosi: number;
  rifiutiTotaliOk: boolean;
  rifiutiPericolosiOk: boolean;
}

export interface ConformitaTrend {
  anno: number;
  mese: number;
  totaleMisurazioni: number;
  misurazioniConformi: number;
  misurazioniNonConformi: number;
  percentualeConformita: number;
}
