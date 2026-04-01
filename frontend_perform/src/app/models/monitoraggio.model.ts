export interface ParametroMonitoraggio {
  id?: number;
  monitoraggioId?: number;
  nome: string;
  codice?: string;
  unitaMisura?: string;
  limiteValore?: number;
  limiteUnita?: string;
  limiteRiferimento?: string;
  frequenza?: FrequenzaMonitoraggio;
  metodoAnalisi?: string;
  note?: string;
  attivo?: boolean;
  createdAt?: string;
}

export interface AnagraficaCaminoSummary {
  id: number;
  sigla: string;
  faseProcesso?: string;
  origine?: string;
  portataNomc3h?: number;
  sezioneM2?: number;
  velocitaMs?: number;
  temperaturaC?: number;
  temperaturaAmbiente?: boolean;
  altezzaM?: number;
  durataHGiorno?: number;
  durataGAnno?: number;
  impiantoAbbattimento?: string;
  note?: string;
}

export interface Monitoraggio {
  id?: number;
  stabilimentoId?: number;
  stabilimentoNome?: string;
  codice: string;
  descrizione: string;
  tipoMonitoraggio?: TipoMonitoraggio;
  puntoEmissione?: string;
  frequenza?: FrequenzaMonitoraggio;
  prossimaScadenza?: string;
  laboratorio?: string;
  metodica?: string;
  normativaRiferimento?: string;
  matricola?: string;
  attivo?: boolean;
  createdAt?: string;
  parametri?: ParametroMonitoraggio[];
  /** ID dell'anagrafica camino collegata (solo EMISSIONI_ATMOSFERA) */
  anagraficaCaminoId?: number;
  /** Dati tecnici del camino, popolati dal backend nel toDTO */
  anagraficaCamino?: AnagraficaCaminoSummary;
}

export enum TipoMonitoraggio {
  EMISSIONI_ATMOSFERA = 'EMISSIONI_ATMOSFERA',
  SCARICHI_IDRICI = 'SCARICHI_IDRICI',
  ACQUE_METEORICHE = 'ACQUE_METEORICHE',
  FALDA = 'FALDA',
  PIEZOMETRO = 'PIEZOMETRO',
  RUMORE = 'RUMORE',
  SUOLO = 'SUOLO',
  ODORI = 'ODORI'
}

export enum FrequenzaMonitoraggio {
  GIORNALIERA = 'GIORNALIERA',
  SETTIMANALE = 'SETTIMANALE',
  MENSILE = 'MENSILE',
  BIMESTRALE = 'BIMESTRALE',
  TRIMESTRALE = 'TRIMESTRALE',
  SEMESTRALE = 'SEMESTRALE',
  ANNUALE = 'ANNUALE',
  BIENNALE = 'BIENNALE',
  TRIENNALE = 'TRIENNALE'
}

export const TIPO_LABEL: Record<TipoMonitoraggio, string> = {
  [TipoMonitoraggio.EMISSIONI_ATMOSFERA]: 'Emissioni in Atmosfera',
  [TipoMonitoraggio.SCARICHI_IDRICI]:     'Scarichi Idrici',
  [TipoMonitoraggio.ACQUE_METEORICHE]:    'Acque Meteoriche',
  [TipoMonitoraggio.FALDA]:              'Falda',
  [TipoMonitoraggio.PIEZOMETRO]:         'Piezometro',
  [TipoMonitoraggio.RUMORE]:             'Rumore',
  [TipoMonitoraggio.SUOLO]:              'Suolo',
  [TipoMonitoraggio.ODORI]:              'Odori',
};

export const TIPO_ICON: Record<TipoMonitoraggio, string> = {
  [TipoMonitoraggio.EMISSIONI_ATMOSFERA]: '💨',
  [TipoMonitoraggio.SCARICHI_IDRICI]:     '💧',
  [TipoMonitoraggio.ACQUE_METEORICHE]:    '🌧️',
  [TipoMonitoraggio.FALDA]:              '🌊',
  [TipoMonitoraggio.PIEZOMETRO]:         '📏',
  [TipoMonitoraggio.RUMORE]:             '🔊',
  [TipoMonitoraggio.SUOLO]:              '🌱',
  [TipoMonitoraggio.ODORI]:              '🍃',
};
