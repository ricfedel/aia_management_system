export enum StatoConformita {
  CONFORME      = 'CONFORME',
  ATTENZIONE    = 'ATTENZIONE',
  NON_CONFORME  = 'NON_CONFORME'
}

export interface RilevazioneMisura {
  id?: number;
  parametroMonitoraggioId: number;
  parametroNome?: string;
  parametroCodice?: string;
  parametroUnitaMisura?: string;
  parametroLimiteValore?: number;
  parametroLimiteRiferimento?: string;
  // Punto di monitoraggio
  monitoraggioId?: number;
  monitoraggioCodice?: string;
  monitoraggioDescrizione?: string;
  monitoraggioTipo?: string;
  // Stabilimento
  stabilimentoId?: number;
  stabilimentoNome?: string;
  // Misura
  dataCampionamento: string;
  valoreMisurato: number;
  unitaMisura?: string;
  statoConformita?: StatoConformita;
  rapportoProva?: string;
  laboratorio?: string;
  note?: string;
  createdAt?: string;
}

export interface RiepilogoConformita {
  CONFORME?: number;
  ATTENZIONE?: number;
  NON_CONFORME?: number;
}

/** Raggruppa rilevazioni per punto di monitoraggio */
export interface PuntoConformita {
  monitoraggioId: number;
  monitoraggioCodice: string;
  monitoraggioDescrizione: string;
  monitoraggioTipo: string;
  rilevazioni: RilevazioneMisura[];
  statoGlobale: StatoConformita | null;
}

export const STATO_LABEL: Record<StatoConformita, string> = {
  [StatoConformita.CONFORME]:     'Conforme',
  [StatoConformita.ATTENZIONE]:   'Attenzione',
  [StatoConformita.NON_CONFORME]: 'Non Conforme',
};

export const STATO_CLASS: Record<StatoConformita, string> = {
  [StatoConformita.CONFORME]:     'stato-conforme',
  [StatoConformita.ATTENZIONE]:   'stato-attenzione',
  [StatoConformita.NON_CONFORME]: 'stato-nc',
};

export const STATO_ICON: Record<StatoConformita, string> = {
  [StatoConformita.CONFORME]:     '🟢',
  [StatoConformita.ATTENZIONE]:   '🟡',
  [StatoConformita.NON_CONFORME]: '🔴',
};
