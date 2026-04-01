export enum StatoFisicoRifiuto {
  SOLIDO   = 'SOLIDO',
  LIQUIDO  = 'LIQUIDO',
  FANGOSO  = 'FANGOSO',
  GASSOSO  = 'GASSOSO',
  ALTRO    = 'ALTRO'
}

export enum TipoMovimento {
  PRODUZIONE          = 'PRODUZIONE',
  SMALTIMENTO         = 'SMALTIMENTO',
  RECUPERO            = 'RECUPERO',
  CESSIONE_TERZI      = 'CESSIONE_TERZI',
  DEPOSITO_TEMPORANEO = 'DEPOSITO_TEMPORANEO'
}

export interface CodiceRifiuto {
  id?: number;
  stabilimentoId: number;
  stabilimentoNome?: string;
  codiceCer: string;
  descrizione: string;
  pericoloso?: boolean;
  statoFisico?: StatoFisicoRifiuto;
  unitaMisura?: string;
  codiceGestione?: string;
  destinatarioAbituale?: string;
  note?: string;
  attivo?: boolean;
}

export interface MovimentoRifiuto {
  id?: number;
  codiceRifiutoId: number;
  codiceCer?: string;
  descrizioneRifiuto?: string;
  pericoloso?: boolean;
  anno: number;
  mese?: number;
  tipoMovimento: TipoMovimento;
  quantita: number;
  unitaMisura?: string;
  codiceOperazione?: string;
  destinatario?: string;
  trasportatore?: string;
  numeroFir?: string;
  dataOperazione?: string;
  note?: string;
}

export const TIPO_MOVIMENTO_LABEL: Record<TipoMovimento, string> = {
  [TipoMovimento.PRODUZIONE]:          'Produzione',
  [TipoMovimento.SMALTIMENTO]:         'Smaltimento',
  [TipoMovimento.RECUPERO]:            'Recupero',
  [TipoMovimento.CESSIONE_TERZI]:      'Cessione a Terzi',
  [TipoMovimento.DEPOSITO_TEMPORANEO]: 'Deposito Temporaneo',
};

export const TIPO_MOVIMENTO_ICON: Record<TipoMovimento, string> = {
  [TipoMovimento.PRODUZIONE]:          '🏭',
  [TipoMovimento.SMALTIMENTO]:         '🗑️',
  [TipoMovimento.RECUPERO]:            '♻️',
  [TipoMovimento.CESSIONE_TERZI]:      '🚛',
  [TipoMovimento.DEPOSITO_TEMPORANEO]: '📦',
};

export const TIPO_MOVIMENTO_CLASS: Record<TipoMovimento, string> = {
  [TipoMovimento.PRODUZIONE]:          'tipo-produzione',
  [TipoMovimento.SMALTIMENTO]:         'tipo-smaltimento',
  [TipoMovimento.RECUPERO]:            'tipo-recupero',
  [TipoMovimento.CESSIONE_TERZI]:      'tipo-cessione',
  [TipoMovimento.DEPOSITO_TEMPORANEO]: 'tipo-deposito',
};

export const MESI_LABEL = [
  'Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
  'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'
];

/** Codici CER comuni per mulini cerealicoli (pre-caricabili come template) */
export const CER_TEMPLATE: Omit<CodiceRifiuto, 'id' | 'stabilimentoId'>[] = [
  { codiceCer: '02 07 04', descrizione: 'Scarti inutilizzabili per il consumo o la trasformazione (farine fuori spec)', pericoloso: false, unitaMisura: 't', statoFisico: StatoFisicoRifiuto.SOLIDO },
  { codiceCer: '15 01 01', descrizione: 'Imballaggi in carta e cartone', pericoloso: false, unitaMisura: 't', statoFisico: StatoFisicoRifiuto.SOLIDO },
  { codiceCer: '15 01 02', descrizione: 'Imballaggi in plastica', pericoloso: false, unitaMisura: 't', statoFisico: StatoFisicoRifiuto.SOLIDO },
  { codiceCer: '15 01 06', descrizione: 'Imballaggi in materiali misti', pericoloso: false, unitaMisura: 't', statoFisico: StatoFisicoRifiuto.SOLIDO },
  { codiceCer: '16 01 07*', descrizione: 'Filtri olio (pericoloso)', pericoloso: true, unitaMisura: 'kg', statoFisico: StatoFisicoRifiuto.SOLIDO },
  { codiceCer: '13 02 05*', descrizione: 'Oli minerali per motori non clorurati (pericoloso)', pericoloso: true, unitaMisura: 'l', statoFisico: StatoFisicoRifiuto.LIQUIDO },
  { codiceCer: '17 04 05', descrizione: 'Ferro e acciaio (rottami)', pericoloso: false, unitaMisura: 't', statoFisico: StatoFisicoRifiuto.SOLIDO },
  { codiceCer: '20 03 01', descrizione: 'Rifiuti urbani misti', pericoloso: false, unitaMisura: 't', statoFisico: StatoFisicoRifiuto.SOLIDO },
  { codiceCer: '19 09 02', descrizione: 'Fanghi prodotti dal trattamento delle acque', pericoloso: false, unitaMisura: 't', statoFisico: StatoFisicoRifiuto.FANGOSO },
];
