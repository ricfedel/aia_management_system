export enum CategoriaVoce {
  MATERIA_PRIMA     = 'MATERIA_PRIMA',
  PRODUZIONE_OUTPUT = 'PRODUZIONE_OUTPUT',
  ACQUA             = 'ACQUA',
  ENERGIA_ELETTRICA = 'ENERGIA_ELETTRICA',
  GAS_NATURALE      = 'GAS_NATURALE',
  GASOLIO           = 'GASOLIO',
  ALTRO             = 'ALTRO'
}

export enum StatoRegistro {
  BOZZA      = 'BOZZA',
  INVIATO    = 'INVIATO',
  APPROVATO  = 'APPROVATO',
  RETTIFICATO = 'RETTIFICATO'
}

export interface VoceProduzione {
  id?: number;
  registroMensileId?: number;
  categoria: CategoriaVoce;
  descrizione: string;
  codice?: string;
  quantita?: number;
  unitaMisura?: string;
  quantitaAnnoPrecedente?: number;
  note?: string;
  sortOrder?: number;
}

export interface RegistroMensile {
  id?: number;
  stabilimentoId: number;
  stabilimentoNome?: string;
  anno: number;
  mese: number;
  stato?: StatoRegistro;
  note?: string;
  compilatoDa?: string;
  createdAt?: string;
  updatedAt?: string;
  voci?: VoceProduzione[];
}

export const CATEGORIA_LABEL: Record<CategoriaVoce, string> = {
  [CategoriaVoce.MATERIA_PRIMA]:     'Materie Prime',
  [CategoriaVoce.PRODUZIONE_OUTPUT]: 'Produzione',
  [CategoriaVoce.ACQUA]:             'Sistema Idrico',
  [CategoriaVoce.ENERGIA_ELETTRICA]: 'Energia Elettrica',
  [CategoriaVoce.GAS_NATURALE]:      'Gas Naturale',
  [CategoriaVoce.GASOLIO]:           'Gasolio',
  [CategoriaVoce.ALTRO]:             'Altro',
};

export const CATEGORIA_ICON: Record<CategoriaVoce, string> = {
  [CategoriaVoce.MATERIA_PRIMA]:     '🌾',
  [CategoriaVoce.PRODUZIONE_OUTPUT]: '🏭',
  [CategoriaVoce.ACQUA]:             '💧',
  [CategoriaVoce.ENERGIA_ELETTRICA]: '⚡',
  [CategoriaVoce.GAS_NATURALE]:      '🔥',
  [CategoriaVoce.GASOLIO]:           '⛽',
  [CategoriaVoce.ALTRO]:             '📦',
};

export const CATEGORIA_UNITA_DEFAULT: Record<CategoriaVoce, string> = {
  [CategoriaVoce.MATERIA_PRIMA]:     't',
  [CategoriaVoce.PRODUZIONE_OUTPUT]: 't',
  [CategoriaVoce.ACQUA]:             'm³',
  [CategoriaVoce.ENERGIA_ELETTRICA]: 'kWh',
  [CategoriaVoce.GAS_NATURALE]:      'Nm³',
  [CategoriaVoce.GASOLIO]:           'l',
  [CategoriaVoce.ALTRO]:             '',
};

export const MESI_LABEL = [
  'Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
  'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'
];

/** Template voci predefinite per un nuovo registro (struttura PMC-standard GMI) */
export const VOCI_TEMPLATE: Omit<VoceProduzione, 'id' | 'registroMensileId'>[] = [
  // Materie prime
  { categoria: CategoriaVoce.MATERIA_PRIMA, descrizione: 'Grano Tenero',    codice: 'GT', unitaMisura: 't', sortOrder: 10 },
  { categoria: CategoriaVoce.MATERIA_PRIMA, descrizione: 'Grano Duro',      codice: 'GD', unitaMisura: 't', sortOrder: 20 },
  { categoria: CategoriaVoce.MATERIA_PRIMA, descrizione: 'Mais',            codice: 'M',  unitaMisura: 't', sortOrder: 30 },
  { categoria: CategoriaVoce.MATERIA_PRIMA, descrizione: 'Orzo',            codice: 'O',  unitaMisura: 't', sortOrder: 40 },
  { categoria: CategoriaVoce.MATERIA_PRIMA, descrizione: 'Altri Cereali',   codice: 'AC', unitaMisura: 't', sortOrder: 50 },
  // Produzione
  { categoria: CategoriaVoce.PRODUZIONE_OUTPUT, descrizione: 'Farina Prodotta',  codice: 'FP', unitaMisura: 't', sortOrder: 10 },
  { categoria: CategoriaVoce.PRODUZIONE_OUTPUT, descrizione: 'Semola Prodotta',  codice: 'SP', unitaMisura: 't', sortOrder: 20 },
  // Acqua
  { categoria: CategoriaVoce.ACQUA, descrizione: 'Acquedotto (M1)',  codice: 'M1', unitaMisura: 'm³', sortOrder: 10 },
  { categoria: CategoriaVoce.ACQUA, descrizione: 'Pozzo (M2)',       codice: 'M2', unitaMisura: 'm³', sortOrder: 20 },
  { categoria: CategoriaVoce.ACQUA, descrizione: 'Recupero (M3)',    codice: 'M3', unitaMisura: 'm³', sortOrder: 30 },
  // Energia
  { categoria: CategoriaVoce.ENERGIA_ELETTRICA, descrizione: 'Energia Acquistata F1 (Punta)',         codice: 'F1', unitaMisura: 'kWh', sortOrder: 10 },
  { categoria: CategoriaVoce.ENERGIA_ELETTRICA, descrizione: 'Energia Acquistata F2 (Intermedia)',    codice: 'F2', unitaMisura: 'kWh', sortOrder: 20 },
  { categoria: CategoriaVoce.ENERGIA_ELETTRICA, descrizione: 'Energia Acquistata F3 (Fuori Punta)',   codice: 'F3', unitaMisura: 'kWh', sortOrder: 30 },
  { categoria: CategoriaVoce.ENERGIA_ELETTRICA, descrizione: 'Energia Autoprodotta (Fotovoltaico)',   codice: 'FV', unitaMisura: 'kWh', sortOrder: 40 },
  // Gas e gasolio
  { categoria: CategoriaVoce.GAS_NATURALE, descrizione: 'Gas Naturale Consumato', codice: 'GN', unitaMisura: 'Nm³', sortOrder: 10 },
  { categoria: CategoriaVoce.GASOLIO,      descrizione: 'Gasolio Consumato',      codice: 'GO', unitaMisura: 'l',   sortOrder: 10 },
];
