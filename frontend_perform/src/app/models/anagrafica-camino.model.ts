export type FaseProcesso =
  | 'TRASPORTO'
  | 'PREPULITURA'
  | 'PULITURA'
  | 'MACINAZIONE'
  | 'MOVIMENTAZIONE'
  | 'MANUTENZIONE'
  | 'CONTROLLO_QUALITA'
  | 'ALTRO';

export const FASE_PROCESSO_LABELS: Record<FaseProcesso, string> = {
  TRASPORTO:         'Trasporto',
  PREPULITURA:       'Prepulitura',
  PULITURA:          'Pulitura',
  MACINAZIONE:       'Macinazione',
  MOVIMENTAZIONE:    'Movimentazione',
  MANUTENZIONE:      'Manutenzione',
  CONTROLLO_QUALITA: 'Controllo qualità',
  ALTRO:             'Altro',
};

export interface AnagraficaCamino {
  id?: number;
  stabilimentoId: number;
  stabilimentoNome?: string;
  sigla: string;
  faseProcesso?: FaseProcesso;
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
  attivo?: boolean;
  createdAt?: string;
}
