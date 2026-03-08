// ── Enums ─────────────────────────────────────────────────────────────────────

export enum TipoComunicazione {
  TRASMISSIONE_DATI_PMC     = 'TRASMISSIONE_DATI_PMC',
  RELAZIONE_ANNUALE_AIA     = 'RELAZIONE_ANNUALE_AIA',
  RICHIESTA_INFORMAZIONI    = 'RICHIESTA_INFORMAZIONI',
  RISPOSTA_A_RICHIESTA      = 'RISPOSTA_A_RICHIESTA',
  COMUNICAZIONE_NON_CONFORMITA = 'COMUNICAZIONE_NON_CONFORMITA',
  DOMANDA_RINNOVO_AIA       = 'DOMANDA_RINNOVO_AIA',
  DOMANDA_MODIFICA_AIA      = 'DOMANDA_MODIFICA_AIA',
  DIFFIDA                   = 'DIFFIDA',
  PRESCRIZIONE_AGGIUNTIVA   = 'PRESCRIZIONE_AGGIUNTIVA',
  AUTORIZZAZIONE_RICEVUTA   = 'AUTORIZZAZIONE_RICEVUTA',
  COMUNICAZIONE_VARIAZIONE  = 'COMUNICAZIONE_VARIAZIONE',
  ALTRO                     = 'ALTRO'
}

export enum StatoComunicazione {
  BOZZA             = 'BOZZA',
  INVIATA           = 'INVIATA',
  CONSEGNATA_PEC    = 'CONSEGNATA_PEC',
  RISPOSTA_RICEVUTA = 'RISPOSTA_RICEVUTA',
  ARCHIVIATA        = 'ARCHIVIATA'
}

export enum EnteEsterno {
  ARPA                = 'ARPA',
  COMUNE              = 'COMUNE',
  PROVINCIA           = 'PROVINCIA',
  REGIONE             = 'REGIONE',
  MINISTERO_AMBIENTE  = 'MINISTERO_AMBIENTE',
  PREFETTURA          = 'PREFETTURA',
  ISPETTORATO_LAVORO  = 'ISPETTORATO_LAVORO',
  ASL                 = 'ASL',
  ALTRO               = 'ALTRO'
}

// ── Interfacce ────────────────────────────────────────────────────────────────

export interface ComunicazioneEnte {
  id?:                  number;
  stabilimentoId:       number;
  stabilimentoNome?:    string;

  tipo:                 TipoComunicazione;
  stato:                StatoComunicazione;
  ente:                 EnteEsterno;
  enteUfficio?:         string;
  enteReferente?:       string;

  oggetto:              string;
  dataInvio?:           string;
  numeroPecInvio?:      string;
  protocolloInterno?:   string;
  protocolloEnte?:      string;
  contenuto?:           string;
  note?:                string;
  allegati?:            string;   // CSV nomi file

  hasRiscontro?:        boolean;
  dataRiscontro?:       string;
  protocolloRiscontro?: string;
  noteRiscontro?:       string;
  allegatiRiscontro?:   string;

  prescrizioneId?:      number;
  prescrizioneOggetto?: string;

  createdAt?:           string;
  updatedAt?:           string;
  createdBy?:           string;
}

export interface RiepilogoComunicazioni {
  BOZZA?:             number;
  INVIATA?:           number;
  CONSEGNATA_PEC?:    number;
  RISPOSTA_RICEVUTA?: number;
  ARCHIVIATA?:        number;
}

// ── Label / icon maps ─────────────────────────────────────────────────────────

export const TIPO_COM_LABEL: Record<TipoComunicazione, string> = {
  [TipoComunicazione.TRASMISSIONE_DATI_PMC]:      'Trasmissione dati PMC',
  [TipoComunicazione.RELAZIONE_ANNUALE_AIA]:      'Relazione annuale AIA',
  [TipoComunicazione.RICHIESTA_INFORMAZIONI]:     'Richiesta informazioni',
  [TipoComunicazione.RISPOSTA_A_RICHIESTA]:       'Risposta a richiesta',
  [TipoComunicazione.COMUNICAZIONE_NON_CONFORMITA]: 'Comunicazione non conformità',
  [TipoComunicazione.DOMANDA_RINNOVO_AIA]:        'Domanda rinnovo AIA',
  [TipoComunicazione.DOMANDA_MODIFICA_AIA]:       'Domanda modifica AIA',
  [TipoComunicazione.DIFFIDA]:                    'Diffida',
  [TipoComunicazione.PRESCRIZIONE_AGGIUNTIVA]:    'Prescrizione aggiuntiva',
  [TipoComunicazione.AUTORIZZAZIONE_RICEVUTA]:    'Autorizzazione ricevuta',
  [TipoComunicazione.COMUNICAZIONE_VARIAZIONE]:   'Comunicazione variazione',
  [TipoComunicazione.ALTRO]:                      'Altro',
};

export const TIPO_COM_ICON: Record<TipoComunicazione, string> = {
  [TipoComunicazione.TRASMISSIONE_DATI_PMC]:      '📤',
  [TipoComunicazione.RELAZIONE_ANNUALE_AIA]:      '📊',
  [TipoComunicazione.RICHIESTA_INFORMAZIONI]:     '❓',
  [TipoComunicazione.RISPOSTA_A_RICHIESTA]:       '↩️',
  [TipoComunicazione.COMUNICAZIONE_NON_CONFORMITA]: '⚠️',
  [TipoComunicazione.DOMANDA_RINNOVO_AIA]:        '🔄',
  [TipoComunicazione.DOMANDA_MODIFICA_AIA]:       '✏️',
  [TipoComunicazione.DIFFIDA]:                    '🚨',
  [TipoComunicazione.PRESCRIZIONE_AGGIUNTIVA]:    '📋',
  [TipoComunicazione.AUTORIZZAZIONE_RICEVUTA]:    '✅',
  [TipoComunicazione.COMUNICAZIONE_VARIAZIONE]:   '🔔',
  [TipoComunicazione.ALTRO]:                      '📧',
};

export const STATO_COM_LABEL: Record<StatoComunicazione, string> = {
  [StatoComunicazione.BOZZA]:             'Bozza',
  [StatoComunicazione.INVIATA]:           'Inviata',
  [StatoComunicazione.CONSEGNATA_PEC]:    'Consegnata (PEC)',
  [StatoComunicazione.RISPOSTA_RICEVUTA]: 'Risposta ricevuta',
  [StatoComunicazione.ARCHIVIATA]:        'Archiviata',
};

export const STATO_COM_CLASS: Record<StatoComunicazione, string> = {
  [StatoComunicazione.BOZZA]:             'stato-bozza',
  [StatoComunicazione.INVIATA]:           'stato-inviata',
  [StatoComunicazione.CONSEGNATA_PEC]:    'stato-consegnata',
  [StatoComunicazione.RISPOSTA_RICEVUTA]: 'stato-risposta',
  [StatoComunicazione.ARCHIVIATA]:        'stato-archiviata',
};

export const STATO_COM_ICON: Record<StatoComunicazione, string> = {
  [StatoComunicazione.BOZZA]:             '📝',
  [StatoComunicazione.INVIATA]:           '📤',
  [StatoComunicazione.CONSEGNATA_PEC]:    '📬',
  [StatoComunicazione.RISPOSTA_RICEVUTA]: '📥',
  [StatoComunicazione.ARCHIVIATA]:        '🗂️',
};

export const ENTE_LABEL: Record<EnteEsterno, string> = {
  [EnteEsterno.ARPA]:               'ARPA',
  [EnteEsterno.COMUNE]:             'Comune',
  [EnteEsterno.PROVINCIA]:          'Provincia',
  [EnteEsterno.REGIONE]:            'Regione',
  [EnteEsterno.MINISTERO_AMBIENTE]: 'Ministero dell\'Ambiente',
  [EnteEsterno.PREFETTURA]:         'Prefettura',
  [EnteEsterno.ISPETTORATO_LAVORO]: 'Ispettorato del Lavoro',
  [EnteEsterno.ASL]:                'ASL',
  [EnteEsterno.ALTRO]:              'Altro',
};

// ── Transizioni stato consentite ──────────────────────────────────────────────

export const STATO_TRANSITIONS: Record<StatoComunicazione, StatoComunicazione[]> = {
  [StatoComunicazione.BOZZA]:             [StatoComunicazione.INVIATA],
  [StatoComunicazione.INVIATA]:           [StatoComunicazione.CONSEGNATA_PEC, StatoComunicazione.RISPOSTA_RICEVUTA],
  [StatoComunicazione.CONSEGNATA_PEC]:    [StatoComunicazione.RISPOSTA_RICEVUTA, StatoComunicazione.ARCHIVIATA],
  [StatoComunicazione.RISPOSTA_RICEVUTA]: [StatoComunicazione.ARCHIVIATA],
  [StatoComunicazione.ARCHIVIATA]:        [],
};
