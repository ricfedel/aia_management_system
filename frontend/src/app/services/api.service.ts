import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Stabilimento } from '../models/stabilimento.model';
import { Prescrizione } from '../models/prescrizione.model';
import { DatiAmbientali } from '../models/dati-ambientali.model';
import { Scadenza } from '../models/scadenza.model';
import { Documento, DocumentoSearchParams, DocumentoSearchResult, StatoDocumento } from '../models/documento.model';
import { DashboardStats, StabilimentoStats, ScadenzaImminente, ConformitaTrend } from '../models/dashboard.model';
import { User, RegisterRequest } from '../models/user.model';
import {
  AvviaProcessoRequest, CompletaTaskRequest,
  ProcessoDocumentoDTO, ProcessiPage, TaskProcessoDTO
} from '../models/processo.model';
import {
  PropostaEstrazione, ConfermaEstrazioneRequest, ConfermaEstrazioneResponse
} from '../models/estrazione.model';
export interface RigaImport {
  rigaExcel: number;
  sito: string;
  stabilimentoId?: number;
  stabilimentoNome?: string;
  dataScadenza?: string;
  dataPrevistaAttivazione?: string;
  dataAdempimento?: string;
  riferimento?: string;
  causale?: string;
  documentiCorrelati?: string;
  riscontroEnte?: string;
  altro?: string;
  tipoScadenzaRilevato?: string;
  errore?: string;
  selezionata: boolean;
}

export interface ImportScadenzeResult {
  righe?: RigaImport[];
  create: number;
  saltate: number;
  sitiTrovati?: string[];
}

import {
  DefinizioneFlusso, StepPreview, SaveDefinizioneFlussoRequest
} from '../models/definizione-flusso.model';
import { Monitoraggio, ParametroMonitoraggio } from '../models/monitoraggio.model';
import { RegistroMensile, VoceProduzione } from '../models/produzione.model';
import { CodiceRifiuto, MovimentoRifiuto } from '../models/rifiuti.model';
import { RilevazioneMisura, RiepilogoConformita } from '../models/conformita.model';
import { ComunicazioneEnte, RiepilogoComunicazioni } from '../models/comunicazioni.model';
import { RelazioneAnnualeDTO } from '../models/relazione-annuale.model';
import { AnagraficaCamino, FaseProcesso } from '../models/anagrafica-camino.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ========== STABILIMENTI ==========
  getStabilimenti(): Observable<Stabilimento[]> {
    return this.http.get<Stabilimento[]>(`${this.apiUrl}/stabilimenti`);
  }

  getStabilimento(id: number): Observable<Stabilimento> {
    return this.http.get<Stabilimento>(`${this.apiUrl}/stabilimenti/${id}`);
  }

  createStabilimento(stabilimento: Stabilimento): Observable<Stabilimento> {
    return this.http.post<Stabilimento>(`${this.apiUrl}/stabilimenti`, stabilimento);
  }

  updateStabilimento(id: number, stabilimento: Stabilimento): Observable<Stabilimento> {
    return this.http.put<Stabilimento>(`${this.apiUrl}/stabilimenti/${id}`, stabilimento);
  }

  deleteStabilimento(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/stabilimenti/${id}`);
  }

  // ========== PRESCRIZIONI ==========
  getPrescrizioni(): Observable<Prescrizione[]> {
    return this.http.get<Prescrizione[]>(`${this.apiUrl}/prescrizioni`);
  }

  getPrescrizione(id: number): Observable<Prescrizione> {
    return this.http.get<Prescrizione>(`${this.apiUrl}/prescrizioni/${id}`);
  }

  getPrescrizioniByStabilimento(stabilimentoId: number): Observable<Prescrizione[]> {
    return this.http.get<Prescrizione[]>(`${this.apiUrl}/prescrizioni/stabilimento/${stabilimentoId}`);
  }

  createPrescrizione(prescrizione: Prescrizione): Observable<Prescrizione> {
    return this.http.post<Prescrizione>(`${this.apiUrl}/prescrizioni`, prescrizione);
  }

  updatePrescrizione(id: number, prescrizione: Prescrizione): Observable<Prescrizione> {
    return this.http.put<Prescrizione>(`${this.apiUrl}/prescrizioni/${id}`, prescrizione);
  }

  deletePrescrizione(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/prescrizioni/${id}`);
  }

  // ========== SCADENZE ==========
  getScadenze(): Observable<Scadenza[]> {
    return this.http.get<Scadenza[]>(`${this.apiUrl}/scadenze`);
  }

  getScadenza(id: number): Observable<Scadenza> {
    return this.http.get<Scadenza>(`${this.apiUrl}/scadenze/${id}`);
  }

  getScadenzeProssimi30Giorni(): Observable<Scadenza[]> {
    return this.http.get<Scadenza[]>(`${this.apiUrl}/scadenze/prossimi-30-giorni`);
  }

  getScadenzeByStabilimento(stabilimentoId: number): Observable<Scadenza[]> {
    return this.http.get<Scadenza[]>(`${this.apiUrl}/scadenze/stabilimento/${stabilimentoId}`);
  }

  createScadenza(scadenza: Scadenza): Observable<Scadenza> {
    return this.http.post<Scadenza>(`${this.apiUrl}/scadenze`, scadenza);
  }

  updateScadenza(id: number, scadenza: Scadenza): Observable<Scadenza> {
    return this.http.put<Scadenza>(`${this.apiUrl}/scadenze/${id}`, scadenza);
  }

  deleteScadenza(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/scadenze/${id}`);
  }

  /** Carica un Excel e restituisce la preview delle righe parsate */
  previewImportScadenze(file: File, mapping?: Record<string, number>): Observable<ImportScadenzeResult> {
    const fd = new FormData();
    fd.append('file', file);
    if (mapping && Object.keys(mapping).length > 0) {
      fd.append('mapping', JSON.stringify(mapping));
    }
    return this.http.post<ImportScadenzeResult>(`${this.apiUrl}/scadenze/import/preview`, fd);
  }

  /** Persiste le righe selezionate dalla preview */
  confirmImportScadenze(righe: RigaImport[]): Observable<ImportScadenzeResult> {
    return this.http.post<ImportScadenzeResult>(`${this.apiUrl}/scadenze/import/confirm`, righe);
  }

  // ========== DATI AMBIENTALI ==========
  getDatiAmbientali(): Observable<DatiAmbientali[]> {
    return this.http.get<DatiAmbientali[]>(`${this.apiUrl}/dati-ambientali`);
  }

  createDatiAmbientali(dato: DatiAmbientali): Observable<DatiAmbientali> {
    return this.http.post<DatiAmbientali>(`${this.apiUrl}/dati-ambientali`, dato);
  }

  updateDatiAmbientali(id: number, dato: DatiAmbientali): Observable<DatiAmbientali> {
    return this.http.put<DatiAmbientali>(`${this.apiUrl}/dati-ambientali/${id}`, dato);
  }

  deleteDatiAmbientali(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/dati-ambientali/${id}`);
  }

  getDatiNonConformi(): Observable<DatiAmbientali[]> {
    return this.http.get<DatiAmbientali[]>(`${this.apiUrl}/dati-ambientali/non-conformi`);
  }

  getDatiByStabilimentoAndAnno(stabilimentoId: number, anno: number): Observable<DatiAmbientali[]> {
    return this.http.get<DatiAmbientali[]>(`${this.apiUrl}/dati-ambientali/stabilimento/${stabilimentoId}/anno/${anno}`);
  }

  // ========== DOCUMENTI ==========
  uploadDocumento(formData: FormData): Observable<Documento> {
    return this.http.post<Documento>(`${this.apiUrl}/documenti/upload`, formData);
  }

  getDocumento(id: number): Observable<Documento> {
    return this.http.get<Documento>(`${this.apiUrl}/documenti/${id}`);
  }

  downloadDocumento(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/documenti/${id}/download`, {
      responseType: 'blob'
    });
  }

  searchDocumenti(params: DocumentoSearchParams): Observable<DocumentoSearchResult> {
    let httpParams = new HttpParams();

    if (params.nome) httpParams = httpParams.set('nome', params.nome);
    if (params.tipoDocumento) httpParams = httpParams.set('tipoDocumento', params.tipoDocumento);
    if (params.stabilimentoId) httpParams = httpParams.set('stabilimentoId', params.stabilimentoId.toString());
    if (params.anno) httpParams = httpParams.set('anno', params.anno.toString());
    if (params.dataInizio) httpParams = httpParams.set('dataInizio', params.dataInizio);
    if (params.dataFine) httpParams = httpParams.set('dataFine', params.dataFine);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<DocumentoSearchResult>(`${this.apiUrl}/documenti/search`, { params: httpParams });
  }

  deleteDocumento(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/documenti/${id}`);
  }

  /** Aggiorna metadati DMS del documento */
  aggiornaStatoDocumento(
    id: number,
    params: {
      stato?: StatoDocumento;
      oggetto?: string;
      enteEmittente?: string;
      numeroProtocollo?: string;
      tags?: string;
    }
  ): Observable<Documento> {
    let httpParams = new HttpParams();
    if (params.stato)            httpParams = httpParams.set('stato', params.stato);
    if (params.oggetto)          httpParams = httpParams.set('oggetto', params.oggetto);
    if (params.enteEmittente)    httpParams = httpParams.set('enteEmittente', params.enteEmittente);
    if (params.numeroProtocollo) httpParams = httpParams.set('numeroProtocollo', params.numeroProtocollo);
    if (params.tags)             httpParams = httpParams.set('tags', params.tags);
    return this.http.patch<Documento>(`${this.apiUrl}/documenti/${id}/stato`, null, { params: httpParams });
  }

  // ========== BPM ==========

  avviaProcesso(req: AvviaProcessoRequest): Observable<ProcessoDocumentoDTO> {
    return this.http.post<ProcessoDocumentoDTO>(`${this.apiUrl}/bpm/processi/avvia`, req);
  }

  getProcessiAttivi(page = 0, size = 20): Observable<ProcessiPage> {
    return this.http.get<ProcessiPage>(`${this.apiUrl}/bpm/processi/attivi`, {
      params: { page: page.toString(), size: size.toString() }
    });
  }

  getAllProcessi(): Observable<ProcessoDocumentoDTO[]> {
    return this.http.get<ProcessoDocumentoDTO[]>(`${this.apiUrl}/bpm/processi`);
  }

  getProcesso(id: number): Observable<ProcessoDocumentoDTO> {
    return this.http.get<ProcessoDocumentoDTO>(`${this.apiUrl}/bpm/processi/${id}`);
  }

  getProcessiByDocumento(documentoId: number): Observable<ProcessoDocumentoDTO[]> {
    return this.http.get<ProcessoDocumentoDTO[]>(`${this.apiUrl}/bpm/processi/documento/${documentoId}`);
  }

  getMieiTask(): Observable<TaskProcessoDTO[]> {
    return this.http.get<TaskProcessoDTO[]>(`${this.apiUrl}/bpm/tasks/miei`);
  }

  completaTask(processoId: number, taskId: number, req: CompletaTaskRequest): Observable<ProcessoDocumentoDTO> {
    return this.http.post<ProcessoDocumentoDTO>(
      `${this.apiUrl}/bpm/processi/${processoId}/tasks/${taskId}/completa`, req
    );
  }

  sospendiProcesso(id: number, motivo?: string): Observable<ProcessoDocumentoDTO> {
    return this.http.put<ProcessoDocumentoDTO>(`${this.apiUrl}/bpm/processi/${id}/sospendi`,
      motivo ? { motivo } : {});
  }

  riprendiProcesso(id: number): Observable<ProcessoDocumentoDTO> {
    return this.http.put<ProcessoDocumentoDTO>(`${this.apiUrl}/bpm/processi/${id}/riprendi`, {});
  }

  annullaProcesso(id: number, motivo?: string): Observable<ProcessoDocumentoDTO> {
    return this.http.put<ProcessoDocumentoDTO>(`${this.apiUrl}/bpm/processi/${id}/annulla`,
      motivo ? { motivo } : {});
  }

  getBpmStats(): Observable<{ processiAttivi: number; processiTotali: number }> {
    return this.http.get<{ processiAttivi: number; processiTotali: number }>(`${this.apiUrl}/bpm/stats`);
  }

  // ========== ESTRAZIONE OCR / AI ==========

  /** Step 1: avvia analisi OCR/AI del documento */
  analizzaDocumento(documentoId: number): Observable<PropostaEstrazione> {
    return this.http.post<PropostaEstrazione>(
      `${this.apiUrl}/estrazione/${documentoId}/analizza`, {}
    );
  }

  /** Step 2: conferma la proposta revisionata, crea entità nel sistema */
  confermaEstrazione(documentoId: number, req: ConfermaEstrazioneRequest): Observable<ConfermaEstrazioneResponse> {
    return this.http.post<ConfermaEstrazioneResponse>(
      `${this.apiUrl}/estrazione/${documentoId}/conferma`, req
    );
  }

  // ========== UTENTI ==========
  getUtenti(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  getUtente(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${id}`);
  }

  createUtente(utente: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/users`, utente);
  }

  updateUtente(id: number, utente: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${id}`, utente);
  }

  deleteUtente(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
  }

  // ========== DASHBOARD ==========
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`);
  }

  getStabilimentoStats(stabilimentoId: number): Observable<StabilimentoStats> {
    return this.http.get<StabilimentoStats>(`${this.apiUrl}/dashboard/stabilimento/${stabilimentoId}/stats`);
  }

  getScadenzeImminenti(giorni: number = 30): Observable<ScadenzaImminente[]> {
    return this.http.get<ScadenzaImminente[]>(`${this.apiUrl}/dashboard/scadenze-imminenti`, {
      params: { giorni: giorni.toString() }
    });
  }

  getConformitaTrend(mesi: number = 12): Observable<ConformitaTrend[]> {
    return this.http.get<ConformitaTrend[]>(`${this.apiUrl}/dashboard/conformita-trend`, {
      params: { mesi: mesi.toString() }
    });
  }

  // ========== EXPORT ==========
  exportPrescrizioniExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/prescrizioni/excel`, {
      responseType: 'blob'
    });
  }

  // ─── Definizioni Flusso BPMN ───────────────────────────────────────────────

  getDefinizioniFlusso(soloAttive = true): Observable<DefinizioneFlusso[]> {
    const url = soloAttive
      ? `${this.apiUrl}/definizioni-flusso`
      : `${this.apiUrl}/definizioni-flusso/tutti`;
    return this.http.get<DefinizioneFlusso[]>(url);
  }

  getDefinizioneFlusso(id: number): Observable<DefinizioneFlusso> {
    return this.http.get<DefinizioneFlusso>(`${this.apiUrl}/definizioni-flusso/${id}`);
  }

  creaDefinizioneFlusso(req: SaveDefinizioneFlussoRequest): Observable<DefinizioneFlusso> {
    return this.http.post<DefinizioneFlusso>(`${this.apiUrl}/definizioni-flusso`, req);
  }

  aggiornaDefinizioneFlusso(id: number, req: SaveDefinizioneFlussoRequest): Observable<DefinizioneFlusso> {
    return this.http.put<DefinizioneFlusso>(`${this.apiUrl}/definizioni-flusso/${id}`, req);
  }

  disattivaDefinizioneFlusso(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/definizioni-flusso/${id}`);
  }

  previewDefinizioneFlusso(id: number): Observable<StepPreview[]> {
    return this.http.post<StepPreview[]>(`${this.apiUrl}/definizioni-flusso/${id}/preview`, {});
  }

  previewBpmnXml(bpmnXml: string): Observable<StepPreview[]> {
    return this.http.post<StepPreview[]>(`${this.apiUrl}/definizioni-flusso/preview`, { bpmnXml });
  }

  // ========== PUNTI DI MONITORAGGIO ==========
  getPuntiMonitoraggio(): Observable<Monitoraggio[]> {
    return this.http.get<Monitoraggio[]>(`${this.apiUrl}/punti-monitoraggio`);
  }

  getPuntiMonitoraggioByStabilimento(stabilimentoId: number): Observable<Monitoraggio[]> {
    return this.http.get<Monitoraggio[]>(`${this.apiUrl}/punti-monitoraggio/stabilimento/${stabilimentoId}`);
  }

  getPuntoMonitoraggio(id: number): Observable<Monitoraggio> {
    return this.http.get<Monitoraggio>(`${this.apiUrl}/punti-monitoraggio/${id}`);
  }

  createPuntoMonitoraggio(m: Monitoraggio): Observable<Monitoraggio> {
    return this.http.post<Monitoraggio>(`${this.apiUrl}/punti-monitoraggio`, m);
  }

  updatePuntoMonitoraggio(id: number, m: Monitoraggio): Observable<Monitoraggio> {
    return this.http.put<Monitoraggio>(`${this.apiUrl}/punti-monitoraggio/${id}`, m);
  }

  deletePuntoMonitoraggio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/punti-monitoraggio/${id}`);
  }

  getParametri(monitoraggioId: number): Observable<ParametroMonitoraggio[]> {
    return this.http.get<ParametroMonitoraggio[]>(`${this.apiUrl}/punti-monitoraggio/${monitoraggioId}/parametri`);
  }

  addParametro(monitoraggioId: number, p: ParametroMonitoraggio): Observable<ParametroMonitoraggio> {
    return this.http.post<ParametroMonitoraggio>(`${this.apiUrl}/punti-monitoraggio/${monitoraggioId}/parametri`, p);
  }

  updateParametro(monitoraggioId: number, parametroId: number, p: ParametroMonitoraggio): Observable<ParametroMonitoraggio> {
    return this.http.put<ParametroMonitoraggio>(`${this.apiUrl}/punti-monitoraggio/${monitoraggioId}/parametri/${parametroId}`, p);
  }

  deleteParametro(monitoraggioId: number, parametroId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/punti-monitoraggio/${monitoraggioId}/parametri/${parametroId}`);
  }

  // ========== PRODUZIONE E CONSUMI MENSILI ==========
  getAnniProduzione(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/produzione/anni`);
  }

  getAnniProduzioneByStabilimento(stabilimentoId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/produzione/anni/stabilimento/${stabilimentoId}`);
  }

  getRegistriMensili(stabilimentoId?: number, anno?: number): Observable<RegistroMensile[]> {
    let params = new HttpParams();
    if (stabilimentoId) params = params.set('stabilimentoId', stabilimentoId.toString());
    if (anno) params = params.set('anno', anno.toString());
    return this.http.get<RegistroMensile[]>(`${this.apiUrl}/produzione`, { params });
  }

  getRegistroMensile(id: number): Observable<RegistroMensile> {
    return this.http.get<RegistroMensile>(`${this.apiUrl}/produzione/${id}`);
  }

  getRegistroByMese(stabilimentoId: number, anno: number, mese: number): Observable<RegistroMensile> {
    return this.http.get<RegistroMensile>(`${this.apiUrl}/produzione/stabilimento/${stabilimentoId}/anno/${anno}/mese/${mese}`);
  }

  createRegistroMensile(r: RegistroMensile): Observable<RegistroMensile> {
    return this.http.post<RegistroMensile>(`${this.apiUrl}/produzione`, r);
  }

  updateRegistroMensile(id: number, r: Partial<RegistroMensile>): Observable<RegistroMensile> {
    return this.http.put<RegistroMensile>(`${this.apiUrl}/produzione/${id}`, r);
  }

  deleteRegistroMensile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/produzione/${id}`);
  }

  saveVociBatch(registroId: number, voci: VoceProduzione[]): Observable<RegistroMensile> {
    return this.http.put<RegistroMensile>(`${this.apiUrl}/produzione/${registroId}/voci/batch`, voci);
  }

  addVoce(registroId: number, v: VoceProduzione): Observable<VoceProduzione> {
    return this.http.post<VoceProduzione>(`${this.apiUrl}/produzione/${registroId}/voci`, v);
  }

  updateVoce(registroId: number, voceId: number, v: VoceProduzione): Observable<VoceProduzione> {
    return this.http.put<VoceProduzione>(`${this.apiUrl}/produzione/${registroId}/voci/${voceId}`, v);
  }

  deleteVoce(registroId: number, voceId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/produzione/${registroId}/voci/${voceId}`);
  }

  // ========== GESTIONE RIFIUTI ==========
  getCodiciRifiuto(stabilimentoId?: number, soloAttivi?: boolean, pericoloso?: boolean): Observable<CodiceRifiuto[]> {
    let params = new HttpParams();
    if (stabilimentoId) params = params.set('stabilimentoId', stabilimentoId.toString());
    if (soloAttivi != null) params = params.set('soloAttivi', soloAttivi.toString());
    if (pericoloso != null) params = params.set('pericoloso', pericoloso.toString());
    return this.http.get<CodiceRifiuto[]>(`${this.apiUrl}/rifiuti/codici`, { params });
  }

  getCodiceRifiuto(id: number): Observable<CodiceRifiuto> {
    return this.http.get<CodiceRifiuto>(`${this.apiUrl}/rifiuti/codici/${id}`);
  }

  createCodiceRifiuto(c: CodiceRifiuto): Observable<CodiceRifiuto> {
    return this.http.post<CodiceRifiuto>(`${this.apiUrl}/rifiuti/codici`, c);
  }

  updateCodiceRifiuto(id: number, c: Partial<CodiceRifiuto>): Observable<CodiceRifiuto> {
    return this.http.put<CodiceRifiuto>(`${this.apiUrl}/rifiuti/codici/${id}`, c);
  }

  deleteCodiceRifiuto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rifiuti/codici/${id}`);
  }

  getMovimentiRifiuto(params: { stabilimentoId?: number; codiceRifiutoId?: number; anno?: number; mese?: number }): Observable<MovimentoRifiuto[]> {
    let httpParams = new HttpParams();
    if (params.stabilimentoId) httpParams = httpParams.set('stabilimentoId', params.stabilimentoId.toString());
    if (params.codiceRifiutoId) httpParams = httpParams.set('codiceRifiutoId', params.codiceRifiutoId.toString());
    if (params.anno) httpParams = httpParams.set('anno', params.anno.toString());
    if (params.mese) httpParams = httpParams.set('mese', params.mese.toString());
    return this.http.get<MovimentoRifiuto[]>(`${this.apiUrl}/rifiuti/movimenti`, { params: httpParams });
  }

  createMovimentoRifiuto(m: MovimentoRifiuto): Observable<MovimentoRifiuto> {
    return this.http.post<MovimentoRifiuto>(`${this.apiUrl}/rifiuti/movimenti`, m);
  }

  updateMovimentoRifiuto(id: number, m: Partial<MovimentoRifiuto>): Observable<MovimentoRifiuto> {
    return this.http.put<MovimentoRifiuto>(`${this.apiUrl}/rifiuti/movimenti/${id}`, m);
  }

  deleteMovimentoRifiuto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/rifiuti/movimenti/${id}`);
  }

  getAnniRifiuti(stabilimentoId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/rifiuti/anni`, { params: { stabilimentoId: stabilimentoId.toString() } });
  }

  // ========== CONFORMITÀ PER-PARAMETRO ==========
  getRilevazioni(params: {
    parametroId?: number; monitoraggioId?: number;
    stabilimentoId?: number; from?: string; to?: string;
  }): Observable<RilevazioneMisura[]> {
    let p = new HttpParams();
    if (params.parametroId)    p = p.set('parametroId',    params.parametroId.toString());
    if (params.monitoraggioId) p = p.set('monitoraggioId', params.monitoraggioId.toString());
    if (params.stabilimentoId) p = p.set('stabilimentoId', params.stabilimentoId.toString());
    if (params.from)           p = p.set('from',           params.from);
    if (params.to)             p = p.set('to',             params.to);
    return this.http.get<RilevazioneMisura[]>(`${this.apiUrl}/conformita/rilevazioni`, { params: p });
  }

  createRilevazione(r: RilevazioneMisura): Observable<RilevazioneMisura> {
    return this.http.post<RilevazioneMisura>(`${this.apiUrl}/conformita/rilevazioni`, r);
  }

  updateRilevazione(id: number, r: Partial<RilevazioneMisura>): Observable<RilevazioneMisura> {
    return this.http.put<RilevazioneMisura>(`${this.apiUrl}/conformita/rilevazioni/${id}`, r);
  }

  deleteRilevazione(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/conformita/rilevazioni/${id}`);
  }

  getConformitaDashboard(stabilimentoId: number): Observable<RilevazioneMisura[]> {
    return this.http.get<RilevazioneMisura[]>(`${this.apiUrl}/conformita/dashboard/${stabilimentoId}`);
  }

  getConformitaDashboardMonitoraggio(monitoraggioId: number): Observable<RilevazioneMisura[]> {
    return this.http.get<RilevazioneMisura[]>(`${this.apiUrl}/conformita/dashboard/monitoraggio/${monitoraggioId}`);
  }

  getNonConformi(stabilimentoId: number): Observable<RilevazioneMisura[]> {
    return this.http.get<RilevazioneMisura[]>(`${this.apiUrl}/conformita/non-conformi/${stabilimentoId}`);
  }

  getRiepilogoConformita(stabilimentoId: number): Observable<RiepilogoConformita> {
    return this.http.get<RiepilogoConformita>(`${this.apiUrl}/conformita/riepilogo/${stabilimentoId}`);
  }

  // ========== COMUNICAZIONI ENTI ==========

  getComunicazioni(params: {
    stabilimentoId: number;
    stato?: string;
    ente?: string;
    from?: string;
    to?: string;
  }): Observable<ComunicazioneEnte[]> {
    let p = new HttpParams().set('stabilimentoId', params.stabilimentoId);
    if (params.stato) p = p.set('stato', params.stato);
    if (params.ente)  p = p.set('ente', params.ente);
    if (params.from)  p = p.set('from', params.from);
    if (params.to)    p = p.set('to', params.to);
    return this.http.get<ComunicazioneEnte[]>(`${this.apiUrl}/comunicazioni`, { params: p });
  }

  getComunicazione(id: number): Observable<ComunicazioneEnte> {
    return this.http.get<ComunicazioneEnte>(`${this.apiUrl}/comunicazioni/${id}`);
  }

  getInAttesaRiscontro(stabilimentoId: number, giorniSoglia = 30): Observable<ComunicazioneEnte[]> {
    const p = new HttpParams().set('giorniSoglia', giorniSoglia);
    return this.http.get<ComunicazioneEnte[]>(
      `${this.apiUrl}/comunicazioni/in-attesa-riscontro/${stabilimentoId}`, { params: p });
  }

  getRiepilogoComunicazioni(stabilimentoId: number): Observable<RiepilogoComunicazioni> {
    return this.http.get<RiepilogoComunicazioni>(
      `${this.apiUrl}/comunicazioni/riepilogo/${stabilimentoId}`);
  }

  createComunicazione(c: ComunicazioneEnte): Observable<ComunicazioneEnte> {
    return this.http.post<ComunicazioneEnte>(`${this.apiUrl}/comunicazioni`, c);
  }

  updateComunicazione(id: number, c: Partial<ComunicazioneEnte>): Observable<ComunicazioneEnte> {
    return this.http.put<ComunicazioneEnte>(`${this.apiUrl}/comunicazioni/${id}`, c);
  }

  cambiaStatoComunicazione(id: number, stato: string): Observable<ComunicazioneEnte> {
    const p = new HttpParams().set('stato', stato);
    return this.http.patch<ComunicazioneEnte>(`${this.apiUrl}/comunicazioni/${id}/stato`, null, { params: p });
  }

  deleteComunicazione(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/comunicazioni/${id}`);
  }

  // ─── Export Excel ───────────────────────────────────────────────────────────

  exportScadenzeExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/scadenze/excel`, {
      responseType: 'blob'
    });
  }

  exportDatiAmbientaliExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/dati-ambientali/excel`, {
      responseType: 'blob'
    });
  }

  exportUtentiExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/utenti/excel`, {
      responseType: 'blob'
    });
  }

  // ========== RELAZIONE ANNUALE AIA ==========

  getRelazioneAnnualePreview(stabilimentoId: number, anno: number): Observable<RelazioneAnnualeDTO> {
    const p = new HttpParams()
      .set('stabilimentoId', stabilimentoId)
      .set('anno', anno);
    return this.http.get<RelazioneAnnualeDTO>(`${this.apiUrl}/relazione-annuale/preview`, { params: p });
  }

  downloadRelazioneAnnualeDocx(stabilimentoId: number, anno: number): Observable<Blob> {
    const p = new HttpParams()
      .set('stabilimentoId', stabilimentoId)
      .set('anno', anno);
    return this.http.get(`${this.apiUrl}/relazione-annuale/docx`, { params: p, responseType: 'blob' });
  }

  // ========== ALLEGATO 2 – SINTESI PMC EXCEL ==========

  downloadAllegato2Xlsx(stabilimentoId: number, anno: number): Observable<Blob> {
    const p = new HttpParams()
      .set('stabilimentoId', stabilimentoId)
      .set('anno', anno);
    return this.http.get(`${this.apiUrl}/allegato2/xlsx`, { params: p, responseType: 'blob' });
  }

  // ========== ANAGRAFICA CAMINI ==========

  getAnagraficaCamini(params?: { stabilimentoId?: number; faseProcesso?: FaseProcesso; attivo?: boolean }): Observable<AnagraficaCamino[]> {
    let p = new HttpParams();
    if (params?.stabilimentoId != null) p = p.set('stabilimentoId', params.stabilimentoId);
    if (params?.faseProcesso)           p = p.set('faseProcesso', params.faseProcesso);
    if (params?.attivo != null)         p = p.set('attivo', params.attivo);
    return this.http.get<AnagraficaCamino[]>(`${this.apiUrl}/anagrafica-camini`, { params: p });
  }

  getAnagraficaCaminoById(id: number): Observable<AnagraficaCamino> {
    return this.http.get<AnagraficaCamino>(`${this.apiUrl}/anagrafica-camini/${id}`);
  }

  createAnagraficaCamino(c: AnagraficaCamino): Observable<AnagraficaCamino> {
    return this.http.post<AnagraficaCamino>(`${this.apiUrl}/anagrafica-camini`, c);
  }

  updateAnagraficaCamino(id: number, c: AnagraficaCamino): Observable<AnagraficaCamino> {
    return this.http.put<AnagraficaCamino>(`${this.apiUrl}/anagrafica-camini/${id}`, c);
  }

  deleteAnagraficaCamino(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/anagrafica-camini/${id}`);
  }
}
