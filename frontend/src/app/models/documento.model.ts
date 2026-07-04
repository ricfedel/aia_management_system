export interface Documento {
  id: number;
  nome: string;
  nomeFile: string;
  tipoDocumento: TipoDocumento;
  descrizione?: string;
  stabilimento?: {
    id: number;
    nome: string;
  };
  prescrizione?: {
    id: number;
    codice: string;
  };
  anno?: number;
  fileSize: number; // bytes
  mimeType: string;
  versione: number;
  enteDestinatario?: string;
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
  filePath: string;

  // ─── Campi DMS ────────────────────────────────────────────
  statoDocumento?: StatoDocumento;
  enteEmittente?: string;
  oggetto?: string;
  numeroProtocollo?: string;
  dataRicezione?: string;
  tags?: string;
  testoEstratto?: string;
  documentoPadreId?: number;
  isVersioneCorrente?: boolean;
  processo?: { id: number; codiceProcesso: string; stato: string };
}

export enum TipoDocumento {
  PRESCRIZIONE_AIA = 'PRESCRIZIONE_AIA',
  RAPPORTO_PROVA = 'RAPPORTO_PROVA',
  RELAZIONE_ANNUALE = 'RELAZIONE_ANNUALE',
  INTEGRAZIONE = 'INTEGRAZIONE',
  COMUNICAZIONE_PEC = 'COMUNICAZIONE_PEC',
  VERBALE = 'VERBALE',
  PLANIMETRIA = 'PLANIMETRIA',
  STUDIO_TECNICO = 'STUDIO_TECNICO',
  ALTRO = 'ALTRO'
}

export type StatoDocumento =
  | 'BOZZA'
  | 'RICEVUTO'
  | 'IN_LAVORAZIONE'
  | 'IN_REVISIONE'
  | 'APPROVATO'
  | 'ARCHIVIATO';

export const STATO_DOCUMENTO_LABELS: Record<StatoDocumento, string> = {
  BOZZA:          'Bozza',
  RICEVUTO:       'Ricevuto',
  IN_LAVORAZIONE: 'In lavorazione',
  IN_REVISIONE:   'In revisione',
  APPROVATO:      'Approvato',
  ARCHIVIATO:     'Archiviato'
};

export const STATO_DOCUMENTO_COLORS: Record<StatoDocumento, string> = {
  BOZZA:          '#6b7280',
  RICEVUTO:       '#3b82f6',
  IN_LAVORAZIONE: '#f59e0b',
  IN_REVISIONE:   '#8b5cf6',
  APPROVATO:      '#10b981',
  ARCHIVIATO:     '#64748b'
};

export interface DocumentoUploadRequest {
  file: File;
  stabilimentoId: number;
  anno: number;
  tipoDocumento: TipoDocumento;
  descrizione?: string;
  prescrizioneId?: number;
}

export interface DocumentoSearchParams {
  nome?: string;
  tipoDocumento?: TipoDocumento;
  stabilimentoId?: number;
  anno?: number;
  dataInizio?: string;
  dataFine?: string;
  page?: number;
  size?: number;
}

export interface DocumentoSearchResult {
  content: Documento[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
