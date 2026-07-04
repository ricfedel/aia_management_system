export type MetodoEstrazione = 'PDFBOX_TEXT' | 'AI_EXTRACTION' | 'FALLBACK_REGEX';

export interface MetadatiDocumento {
  oggetto?: string;
  enteEmittente?: string;
  numeroProtocollo?: string;
  dataRicezione?: string;
  tipoProvvedimento?: string;
  riferimentoAIA?: string;
}

export interface ScadenzaProposta {
  tempId: string;
  titolo: string;
  descrizione?: string;
  dataScadenza: string;
  tipo?: string;
  testoOrigine?: string;
  giorni?: number;
  confidenza?: number;
  selezionata: boolean;
  // Aggiunto dall'operatore per la conferma
  stabilimentoId?: number;
}

export interface PrescrizioneProposta {
  tempId: string;
  codice: string;
  descrizione?: string;
  tipo?: string;
  testoOrigine?: string;
  confidenza?: number;
  selezionata: boolean;
  stabilimentoId?: number;
}

export interface PropostaEstrazione {
  documentoId: number;
  nomeFile: string;
  metodo: MetodoEstrazione;
  testoEstratto?: string;
  confidenza?: number;
  avvisi?: string;
  metadati: MetadatiDocumento;
  scadenzeProposte: ScadenzaProposta[];
  prescrizioniProposte: PrescrizioneProposta[];
}

export interface ConfermaEstrazioneRequest {
  documentoId: number;
  metadati: MetadatiDocumento;
  scadenze: ScadenzaConfermata[];
  prescrizioni: PrescrizioneConfermata[];
  processoId?: number;
  taskId?: number;
  noteRevisione?: string;
}

export interface ScadenzaConfermata {
  titolo: string;
  descrizione?: string;
  dataScadenza: string;
  tipo?: string;
  stabilimentoId?: number;
}

export interface PrescrizioneConfermata {
  codice: string;
  descrizione?: string;
  tipo?: string;
  stabilimentoId?: number;
}

export interface ConfermaEstrazioneResponse {
  successo: boolean;
  messaggio: string;
  scadenzeCreate: number;
  prescrizioniCreate: number;
  idScadenzeCreate: number[];
  idPrescrizioniCreate: number[];
  nuovoStatoDocumento: string;
  processoAggiornato?: any;
}

export const METODO_LABELS: Record<MetodoEstrazione, string> = {
  PDFBOX_TEXT:   '📄 Testo PDF',
  AI_EXTRACTION: '🤖 Analisi AI',
  FALLBACK_REGEX: '🔍 Regex'
};

export const TIPO_SCADENZA_OPTIONS = [
  { value: 'MONITORAGGIO',  label: 'Monitoraggio' },
  { value: 'REPORTING',     label: 'Reporting / Relazione' },
  { value: 'COMUNICAZIONE', label: 'Comunicazione' },
  { value: 'CAMPIONAMENTO', label: 'Campionamento' },
  { value: 'MANUTENZIONE',  label: 'Manutenzione' },
];

export const TIPO_PRESCRIZIONE_OPTIONS = [
  { value: 'EMISSIONI_ATMOSFERICHE', label: 'Emissioni atmosferiche' },
  { value: 'SCARICHI_IDRICI',        label: 'Scarichi idrici' },
  { value: 'GESTIONE_RIFIUTI',       label: 'Gestione rifiuti' },
  { value: 'EMISSIONI_ACUSTICHE',    label: 'Emissioni acustiche' },
  { value: 'MONITORAGGIO',           label: 'Monitoraggio' },
  { value: 'SUOLO_SOTTOSUOLO',       label: 'Suolo / Sottosuolo' },
  { value: 'ALTRO',                  label: 'Altro' },
];
