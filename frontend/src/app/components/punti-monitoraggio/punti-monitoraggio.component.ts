import { Component, inject, signal, computed, OnInit, DestroyRef, effect } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import {
  Monitoraggio, ParametroMonitoraggio,
  TipoMonitoraggio, FrequenzaMonitoraggio,
  TIPO_LABEL, TIPO_ICON
} from '../../models/monitoraggio.model';
import { AnagraficaCamino } from '../../models/anagrafica-camino.model';
import { Stabilimento } from '../../models/stabilimento.model';

interface MonitoraggioForm extends Partial<Monitoraggio> {
  stabilimentoId?: number;
}

interface ParametroForm extends Partial<ParametroMonitoraggio> {}

@Component({
  selector: 'app-punti-monitoraggio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './punti-monitoraggio.component.html',
  styleUrl: './punti-monitoraggio.component.css'
})
export class PuntiMonitoraggioComponent implements OnInit {

  private api       = inject(ApiService);
  private auth      = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // ── dati ──────────────────────────────────────────────────────────────
  punti         = signal<Monitoraggio[]>([]);
  stabilimenti  = signal<Stabilimento[]>([]);
  loading       = signal(false);
  errorMsg      = signal<string | null>(null);

  // ── filtri ────────────────────────────────────────────────────────────
  filterStabilimento = signal<number | null>(null);
  filterTipo         = signal<TipoMonitoraggio | null>(null);
  filterSoloAttivi   = signal(true);
  searchText         = signal('');

  // ── accordion: quale punto è espanso ──────────────────────────────────
  expandedId = signal<number | null>(null);

  // ── form punto ────────────────────────────────────────────────────────
  showPuntoForm   = signal(false);
  editingPuntoId  = signal<number | null>(null);
  puntoForm       = signal<MonitoraggioForm>({});

  // ── form parametro ────────────────────────────────────────────────────
  editingParamForPuntoId = signal<number | null>(null);   // quale punto ha il form parametro aperto
  editingParamId         = signal<number | null>(null);   // null = nuovo
  paramForm              = signal<ParametroForm>({});

  // ── anagrafica camini (per il select nel form) ────────────────────────
  anagraficaCamini = signal<AnagraficaCamino[]>([]);

  // ── conferma eliminazione ─────────────────────────────────────────────
  confirmDeletePuntoId = signal<number | null>(null);
  confirmDeleteParamId = signal<{ monitoraggioId: number; parametroId: number } | null>(null);

  // ── enum exports ──────────────────────────────────────────────────────
  readonly tipiMonitoraggio  = Object.values(TipoMonitoraggio);
  readonly frequenze         = Object.values(FrequenzaMonitoraggio);
  readonly TIPO_LABEL        = TIPO_LABEL;
  readonly TIPO_ICON         = TIPO_ICON;

  currentUser = () => this.auth.currentUserValue;

  // ── computed: punti filtrati raggruppati per tipo ─────────────────────
  puntiFiltrati = computed(() => {
    let list = this.punti();
    const st  = this.filterStabilimento();
    const tp  = this.filterTipo();
    const txt = this.searchText().toLowerCase().trim();

    if (st)  list = list.filter(p => p.stabilimentoId === st);
    if (tp)  list = list.filter(p => p.tipoMonitoraggio === tp);
    if (this.filterSoloAttivi()) list = list.filter(p => p.attivo !== false);
    if (txt) list = list.filter(p =>
      p.codice.toLowerCase().includes(txt) ||
      p.descrizione.toLowerCase().includes(txt) ||
      (p.puntoEmissione ?? '').toLowerCase().includes(txt)
    );
    return list;
  });

  gruppiPerTipo = computed(() => {
    const map = new Map<TipoMonitoraggio, Monitoraggio[]>();
    for (const tipo of this.tipiMonitoraggio) {
      const items = this.puntiFiltrati().filter(p => p.tipoMonitoraggio === tipo);
      if (items.length) map.set(tipo as TipoMonitoraggio, items);
    }
    // Punti senza tipo
    const senzaTipo = this.puntiFiltrati().filter(p => !p.tipoMonitoraggio);
    if (senzaTipo.length) map.set('ALTRO' as TipoMonitoraggio, senzaTipo);
    return map;
  });

  // ── lifecycle ─────────────────────────────────────────────────────────
  ngOnInit() {
    this.loadStabilimenti();
    this.loadPunti();
    this.loadAnagraficaCamini();
  }

  loadAnagraficaCamini() {
    this.api.getAnagraficaCamini({ attivo: true }).subscribe({
      next: list => this.anagraficaCamini.set(list)
    });
  }

  loadPunti() {
    this.loading.set(true);
    this.errorMsg.set(null);
    this.api.getPuntiMonitoraggio()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => { this.punti.set(data); this.loading.set(false); },
        error: () => { this.errorMsg.set('Errore nel caricamento dei punti di monitoraggio'); this.loading.set(false); }
      });
  }

  loadStabilimenti() {
    this.api.getStabilimenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.stabilimenti.set(data) });
  }

  // ── accordion ─────────────────────────────────────────────────────────
  toggleExpand(id: number) {
    this.expandedId.set(this.expandedId() === id ? null : id);
    // chiudi eventuali form parametro aperti se cambia punto
    if (this.editingParamForPuntoId() !== id) {
      this.closeParamForm();
    }
  }

  isExpanded(id: number) { return this.expandedId() === id; }

  // ── form punto: apri/chiudi ────────────────────────────────────────────
  openNewPunto() {
    this.editingPuntoId.set(null);
    this.puntoForm.set({
      attivo: true,
      stabilimentoId: this.filterStabilimento() ?? undefined
    });
    this.showPuntoForm.set(true);
  }

  editPunto(m: Monitoraggio) {
    this.editingPuntoId.set(m.id!);
    this.puntoForm.set({ ...m });
    this.showPuntoForm.set(true);
  }

  closePuntoForm() {
    this.showPuntoForm.set(false);
    this.editingPuntoId.set(null);
    this.puntoForm.set({});
  }

  savePunto() {
    const f = this.puntoForm();
    if (!f.codice || !f.descrizione || !f.stabilimentoId) return;

    const payload: Monitoraggio = {
      codice:               f.codice,
      descrizione:          f.descrizione,
      stabilimentoId:       f.stabilimentoId,
      tipoMonitoraggio:     f.tipoMonitoraggio,
      puntoEmissione:       f.puntoEmissione,
      frequenza:            f.frequenza,
      prossimaScadenza:     f.prossimaScadenza,
      laboratorio:          f.laboratorio,
      metodica:             f.metodica,
      normativaRiferimento: f.normativaRiferimento,
      matricola:            f.matricola,
      attivo:               f.attivo ?? true,
      anagraficaCaminoId:   f.tipoMonitoraggio === TipoMonitoraggio.EMISSIONI_ATMOSFERA
                              ? (f.anagraficaCaminoId ?? null) as any
                              : null
    };

    const id = this.editingPuntoId();
    const obs = id
      ? this.api.updatePuntoMonitoraggio(id, payload)
      : this.api.createPuntoMonitoraggio(payload);

    obs.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: saved => {
        this.punti.update(list =>
          id ? list.map(p => p.id === id ? { ...p, ...saved } : p)
             : [...list, saved]
        );
        this.closePuntoForm();
      },
      error: () => this.errorMsg.set('Errore nel salvataggio del punto di monitoraggio')
    });
  }

  // ── elimina punto ─────────────────────────────────────────────────────
  requestDeletePunto(id: number) { this.confirmDeletePuntoId.set(id); }
  cancelDeletePunto()            { this.confirmDeletePuntoId.set(null); }

  confirmDeletePunto() {
    const id = this.confirmDeletePuntoId();
    if (!id) return;
    this.api.deletePuntoMonitoraggio(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.punti.update(list => list.filter(p => p.id !== id));
          this.confirmDeletePuntoId.set(null);
        },
        error: () => this.errorMsg.set('Errore nell\'eliminazione del punto di monitoraggio')
      });
  }

  // ── form parametro ────────────────────────────────────────────────────
  openNewParam(monitoraggioId: number) {
    this.editingParamForPuntoId.set(monitoraggioId);
    this.editingParamId.set(null);
    this.paramForm.set({ attivo: true });
  }

  editParam(monitoraggioId: number, p: ParametroMonitoraggio) {
    this.editingParamForPuntoId.set(monitoraggioId);
    this.editingParamId.set(p.id!);
    this.paramForm.set({ ...p });
  }

  closeParamForm() {
    this.editingParamForPuntoId.set(null);
    this.editingParamId.set(null);
    this.paramForm.set({});
  }

  saveParam(monitoraggioId: number) {
    const f = this.paramForm();
    if (!f.nome) return;

    const payload: ParametroMonitoraggio = {
      nome:               f.nome,
      codice:             f.codice,
      unitaMisura:        f.unitaMisura,
      limiteValore:       f.limiteValore,
      limiteUnita:        f.limiteUnita,
      limiteRiferimento:  f.limiteRiferimento,
      frequenza:          f.frequenza,
      metodoAnalisi:      f.metodoAnalisi,
      note:               f.note,
      attivo:             f.attivo ?? true
    };

    const paramId = this.editingParamId();
    const obs = paramId
      ? this.api.updateParametro(monitoraggioId, paramId, payload)
      : this.api.addParametro(monitoraggioId, payload);

    obs.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: saved => {
        this.punti.update(list => list.map(punto => {
          if (punto.id !== monitoraggioId) return punto;
          const params = punto.parametri ?? [];
          const newParams = paramId
            ? params.map(pp => pp.id === paramId ? saved : pp)
            : [...params, saved];
          return { ...punto, parametri: newParams };
        }));
        this.closeParamForm();
      },
      error: () => this.errorMsg.set('Errore nel salvataggio del parametro')
    });
  }

  // ── elimina parametro ─────────────────────────────────────────────────
  requestDeleteParam(monitoraggioId: number, parametroId: number) {
    this.confirmDeleteParamId.set({ monitoraggioId, parametroId });
  }
  cancelDeleteParam() { this.confirmDeleteParamId.set(null); }

  confirmDeleteParam() {
    const target = this.confirmDeleteParamId();
    if (!target) return;
    this.api.deleteParametro(target.monitoraggioId, target.parametroId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.punti.update(list => list.map(punto => {
            if (punto.id !== target.monitoraggioId) return punto;
            return { ...punto, parametri: (punto.parametri ?? []).filter(p => p.id !== target.parametroId) };
          }));
          this.confirmDeleteParamId.set(null);
        },
        error: () => this.errorMsg.set('Errore nell\'eliminazione del parametro')
      });
  }

  // ── helpers ───────────────────────────────────────────────────────────
  patchPuntoForm(field: string, value: any) {
    this.puntoForm.update(f => ({ ...f, [field]: value }));
  }

  patchParamForm(field: string, value: any) {
    this.paramForm.update(f => ({ ...f, [field]: value }));
  }

  tipoLabel(tipo: TipoMonitoraggio | undefined): string {
    return tipo ? (TIPO_LABEL[tipo] ?? tipo) : '—';
  }

  tipoIcon(tipo: TipoMonitoraggio | undefined): string {
    return tipo ? (TIPO_ICON[tipo] ?? '📍') : '📍';
  }

  freqLabel(f: FrequenzaMonitoraggio | undefined): string {
    if (!f) return '—';
    const map: Record<string, string> = {
      GIORNALIERA: 'Giornaliera', SETTIMANALE: 'Settimanale',
      MENSILE: 'Mensile',        BIMESTRALE: 'Bimestrale',
      TRIMESTRALE: 'Trimestrale', SEMESTRALE: 'Semestrale',
      ANNUALE: 'Annuale',        BIENNALE: 'Biennale',
      TRIENNALE: 'Triennale'
    };
    return map[f] ?? f;
  }

  isAdmin(): boolean {
    return this.currentUser()?.ruolo === 'ADMIN';
  }

  isResponsabileOrAdmin(): boolean {
    const r = this.currentUser()?.ruolo;
    return r === 'ADMIN' || r === 'RESPONSABILE';
  }

  getGruppiKeys(): TipoMonitoraggio[] {
    return Array.from(this.gruppiPerTipo().keys());
  }

  getPuntiByTipo(tipo: TipoMonitoraggio): Monitoraggio[] {
    return this.gruppiPerTipo().get(tipo) ?? [];
  }

  isScaduto(data: string | undefined): boolean {
    if (!data) return false;
    return new Date(data) < new Date();
  }

  /** Restituisce i camini attivi per lo stabilimento selezionato nel form */
  caminiPerForm(): AnagraficaCamino[] {
    const stabId = this.puntoForm().stabilimentoId;
    if (!stabId) return this.anagraficaCamini();
    return this.anagraficaCamini().filter(c => c.stabilimentoId === stabId);
  }

  readonly TipoMonitoraggio = TipoMonitoraggio;

  isImminente(data: string | undefined): boolean {
    if (!data) return false;
    const d = new Date(data);
    const now = new Date();
    const diff = (d.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
    return diff >= 0 && diff <= 30;
  }
}
