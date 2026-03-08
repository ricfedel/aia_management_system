export interface Prescrizione {
  id?: number;
  stabilimento?: any;
  codice: string;
  descrizione: string;
  matriceAmbientale: MatriceAmbientale;
  stato: StatoPrescrizione;
  dataEmissione?: string;
  dataScadenza?: string;
  enteEmittente?: string;
  riferimentoNormativo?: string;
  priorita?: Priorita;
  note?: string;
  dataChiusura?: string;
}

export enum MatriceAmbientale {
  ARIA = 'ARIA',
  ACQUA = 'ACQUA',
  FALDA = 'FALDA',
  RUMORE = 'RUMORE',
  SUOLO = 'SUOLO',
  RIFIUTI = 'RIFIUTI',
  ILLUMINAZIONE = 'ILLUMINAZIONE'
}

export enum StatoPrescrizione {
  APERTA = 'APERTA',
  IN_LAVORAZIONE = 'IN_LAVORAZIONE',
  IN_ATTESA_INTEGRAZIONE = 'IN_ATTESA_INTEGRAZIONE',
  CHIUSA = 'CHIUSA',
  SOSPESA = 'SOSPESA'
}

export enum Priorita {
  BASSA = 'BASSA',
  MEDIA = 'MEDIA',
  ALTA = 'ALTA',
  URGENTE = 'URGENTE'
}
