export interface Scadenza {
  id?: number;
  stabilimento?: any;
  prescrizione?: any;
  monitoraggio?: any;
  titolo: string;
  descrizione?: string;
  tipoScadenza: TipoScadenza;
  dataScadenza: string;
  stato: StatoScadenza;
  priorita?: Priorita;
  responsabile?: string;
  emailNotifica?: string;
  giorniPreavviso?: number;
  dataCompletamento?: string;
  note?: string;
  dataPrevistaAttivazione?: string;
  riferimento?: string;
  sitoOrigine?: string;
}

export enum TipoScadenza {
  MONITORAGGIO_PMC = 'MONITORAGGIO_PMC',
  RELAZIONE_ANNUALE = 'RELAZIONE_ANNUALE',
  INTEGRAZIONE_ENTE = 'INTEGRAZIONE_ENTE',
  RINNOVO_AIA = 'RINNOVO_AIA',
  COMUNICAZIONE = 'COMUNICAZIONE',
  ALTRO = 'ALTRO'
}

export enum StatoScadenza {
  PENDING = 'PENDING',
  IN_CORSO = 'IN_CORSO',
  COMPLETATA = 'COMPLETATA',
  SCADUTA = 'SCADUTA'
}

export enum Priorita {
  BASSA = 'BASSA',
  MEDIA = 'MEDIA',
  ALTA = 'ALTA',
  URGENTE = 'URGENTE'
}
