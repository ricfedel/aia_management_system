export interface DatiAmbientali {
  id?: number;
  monitoraggio?: any;
  dataCampionamento: string;
  parametro: string;
  valoreMisurato?: number;
  unitaMisura?: string;
  limiteAutorizzato?: number;
  statoConformita?: StatoConformita;
  metodoAnalisi?: string;
  rapportoProva?: string;
  laboratorio?: string;
  note?: string;
}

export enum StatoConformita {
  CONFORME = 'CONFORME',
  ATTENZIONE = 'ATTENZIONE',
  NON_CONFORME = 'NON_CONFORME'
}
