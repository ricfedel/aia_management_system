export type TipoProcesso =
  | 'LAVORAZIONE_DOCUMENTO'
  | 'RINNOVO_AIA'
  | 'NON_CONFORMITA'
  | 'INTEGRAZIONE_ENTE';

export type StatoProcesso =
  | 'AVVIATO'
  | 'IN_CORSO'
  | 'IN_ATTESA'
  | 'COMPLETATO'
  | 'ANNULLATO'
  | 'SOSPESO';

export type TipoTask = 'USER_TASK' | 'SERVICE_TASK' | 'GATEWAY';

export type StatoTask =
  | 'CREATO'
  | 'IN_CORSO'
  | 'COMPLETATO'
  | 'SALTATO'
  | 'ANNULLATO';

export interface TaskProcessoDTO {
  id: number;
  taskIdBpmn: string;
  nomeTask: string;
  tipoTask: TipoTask;
  statoTask: StatoTask;
  assegnatoA?: string;
  completatoDa?: string;
  commento?: string;
  esito?: string;
  dataCreazione: string;
  dataCompletamento?: string;
  dataScadenza?: string;
}

export interface ProcessoDocumentoDTO {
  id: number;
  codiceProcesso: string;
  tipoProcesso: TipoProcesso;
  stato: StatoProcesso;
  taskCorrente?: string;
  avviatoDa: string;
  assegnatoA?: string;
  note?: string;
  dataAvvio: string;
  dataCompletamento?: string;
  dataAggiornamento: string;
  // Definizione flusso custom (null = processo predefinito)
  definizioneFlussoId?: number;
  definizioneFlussoNome?: string;
  // Documento collegato
  documentoId?: number;
  documentoNome?: string;
  documentoTipo?: string;
  // Stabilimento
  stabilimentoId?: number;
  stabilimentoNome?: string;
  // Tasks
  tasks: TaskProcessoDTO[];
}

export interface AvviaProcessoRequest {
  documentoId: number;
  /** Tipo predefinito — obbligatorio se definizioneFlussoId è null */
  tipoProcesso?: TipoProcesso;
  /** ID della DefinizioneFlusso custom — se valorizzato, tipoProcesso è ignorato */
  definizioneFlussoId?: number;
  stabilimentoId?: number;
  assegnatoA?: string;
  note?: string;
}

export interface CompletaTaskRequest {
  esito?: string;
  commento?: string;
  formDataJson?: string;
}

export interface ProcessiPage {
  content: ProcessoDocumentoDTO[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const TIPO_PROCESSO_LABELS: Record<TipoProcesso, string> = {
  LAVORAZIONE_DOCUMENTO: 'Lavorazione Documento',
  RINNOVO_AIA: 'Rinnovo AIA',
  NON_CONFORMITA: 'Non Conformità',
  INTEGRAZIONE_ENTE: 'Integrazione Ente'
};

export const STATO_PROCESSO_LABELS: Record<StatoProcesso, string> = {
  AVVIATO: 'Avviato',
  IN_CORSO: 'In corso',
  IN_ATTESA: 'In attesa',
  COMPLETATO: 'Completato',
  ANNULLATO: 'Annullato',
  SOSPESO: 'Sospeso'
};

export const STATO_PROCESSO_COLORS: Record<StatoProcesso, string> = {
  AVVIATO:    '#3b82f6',
  IN_CORSO:   '#f59e0b',
  IN_ATTESA:  '#8b5cf6',
  COMPLETATO: '#10b981',
  ANNULLATO:  '#ef4444',
  SOSPESO:    '#6b7280'
};
