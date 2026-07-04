export interface AltraAutorizzazione {
  id?: number;
  stabilimentoId: number;
  stabilimentoNome?: string;
  tipo: TipoAltraAutorizzazione;
  numeroAutorizzazione?: string;
  enteCompetente?: string;
  dataRilascio?: string;
  dataScadenza?: string;
  descrizione?: string;
}

export enum TipoAltraAutorizzazione {
  SCARICO_ACQUE = 'SCARICO_ACQUE',
  BONIFICHE = 'BONIFICHE',
  EMISSIONI_ATMOSFERA = 'EMISSIONI_ATMOSFERA',
  RIFIUTI = 'RIFIUTI',
  ACUSTICA = 'ACUSTICA',
  ALTRO = 'ALTRO'
}
