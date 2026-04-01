export interface Stabilimento {
  id?: number;
  nome: string;
  citta: string;
  indirizzo?: string;
  numeroAIA?: string;
  dataRilascioAIA?: string;
  dataScadenzaAIA?: string;
  enteCompetente?: string;
  responsabileAmbientale?: string;
  email?: string;
  telefono?: string;
  sigla?: string;
  attivo: boolean;
}
