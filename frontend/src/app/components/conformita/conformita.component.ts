import { Component, inject, signal, computed, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import {
  RilevazioneMisura, RiepilogoConformita, PuntoConformita,
  StatoConformita, STATO_LABEL, STATO_CLASS, STATO_ICON
} from '../../models/conformita.model';
import { Stabilimento } from '../../models/stabilimento.model';
import { Monitoraggio, TipoMonitoraggio, TIPO_LABEL, TIPO_ICON } from '../../models/monitoraggio.model';

type ActiveView = 'dashboard' | 'storico' | 'non-conformi';

@Component({
  selector: 'app-conformita',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './conformita.component.html',
  styleUrl: './conformita.component.css'
})
export class ConformitaComponent implements OnInit {

  private api        = inject(ApiService);
  private auth       = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // ── Dati ──────────────────────────────────────────────────────────
  stabilimenti   = signal<Stabilimento[]>([]);
  puntiMon       = signal<Monitoraggio[]>([]);
  dashboardData  = signal<RilevazioneMisura[]>([]);
  storicoData    = signal<RilevazioneMisura[]>([]);
  nonConformi    = signal<RilevazioneMisura[]>([]);
  riepilogo      = signal<RiepilogoConformita>({});
  loading        = signal(false);
  errorMsg       = signal<string | null>(null);

  // ── Navigazione ────────────────────────────────────────────────────
  activeView = signal<ActiveView>('dashboard');

  // ── Filtri ────────────────────────────────────────────────────────
  filterStabilimento  = signal<number | null>(null);
  filterMonitoraggio  = signal<number | null>(null);
  filterFrom          = signal<string>(this.defaultFrom());
  filterTo            = signal<string>(new Date().toISOString().slice(0, 10));
  filterStato         = signal<StatoConformita | null>(null);

  // ── Modal rilevazione ──────────────────────────────────────────────
  showModal       = signal(false);
  editingId       = signal<number | null>(null);
  formData        = signal<Partial<RilevazioneMisura>>({});
  parametriDisp   = signal<{ id: number; label: string; limite?: number; unitaMisura?: string }[]>([]);

  // ── Conferma eliminazione ──────────────────────────────────────────
  confirmDeleteId = signal<number | null>(null);

  // ── Enum exports ──────────────────────────────────────────────────
  readonly StatoConformita = StatoConformita;
  readonly statiConf       = Object.values(StatoConformita);
  readonly STATO_LABEL     = STATO_LABEL;
  readonly STATO_CLASS     = STATO_CLASS;
  readonly STATO_ICON      = STATO_ICON;
  readonly TIPO_LABEL      = TIPO_LABEL;
  readonly TIPO_ICON       = TIPO_ICON;

  currentUser = () => this.auth.currentUserValue;

  // ── Computed: punti raggruppati con stato globale ──────────────────
  puntiConformita = computed<PuntoConformita[]>(() => {
    const data = this.dashboardData();
    const map = new Map<number, PuntoConformita>();
    for (const r of data) {
      if (!r.monitoraggioId) continue;
      if (!map.has(r.monitoraggioId)) {
        map.set(r.monitoraggioId, {
          monitoraggioId: r.monitoraggioId,
          monitoraggioCodice: r.monitoraggioCodice ?? '',
          monitoraggioDescrizione: r.monitoraggioDescrizione ?? '',
          monitoraggioTipo: r.monitoraggioTipo ?? '',
          rilevazioni: [],
          statoGlobale: null
        });
      }
      map.get(r.monitoraggioId)!.rilevazioni.push(r);
    }
    // Calcola stato globale (peggiore tra i parametri)
    for (const punto of map.values()) {
      const stati = punto.rilevazioni
        .map(r => r.statoConformita)
        .filter(Boolean) as StatoConformita[];
      if (stati.includes(StatoConformita.NON_CONFORME))  punto.statoGlobale = StatoConformita.NON_CONFORME;
      else if (stati.includes(StatoConformita.ATTENZIONE)) punto.statoGlobale = StatoConformita.ATTENZIONE;
      else if (stati.includes(StatoConformita.CONFORME))   punto.statoGlobale = StatoConformita.CONFORME;
    }
    return Array.from(map.values()).sort((a, b) => {
      const order = { NON_CONFORME: 0, ATTENZIONE: 1, CONFORME: 2, null: 3 };
      return (order[a.statoGlobale ?? 'null'] ?? 3) - (order[b.statoGlobale ?? 'null'] ?? 3);
    });
  });

  storicoFiltrato = computed(() => {
    const stato = this.filterStato();
    const monId = this.filterMonitoraggio();
    let list = this.storicoData();
    if (stato)  list = list.filter(r => r.statoConformita === stato);
    if (monId)  list = list.filter(r => r.monitoraggioId === monId);
    return list;
  });

  totaleConformi    = computed(() => this.riepilogo().CONFORME    ?? 0);
  totaleAttenzione  = computed(() => this.riepilogo().ATTENZIONE  ?? 0);
  totaleNonConformi = computed(() => this.riepilogo().NON_CONFORME ?? 0);
  totaleMisure      = computed(() => this.totaleConformi() + this.totaleAttenzione() + this.totaleNonConformi());

  percConformi    = computed(() => this.totaleMisure() ? Math.round(this.totaleConformi()    / this.totaleMisure() * 100) : 0);
  percAttenzione  = computed(() => this.totaleMisure() ? Math.round(this.totaleAttenzione()  / this.totaleMisure() * 100) : 0);
  percNonConformi = computed(() => this.totaleMisure() ? Math.round(this.totaleNonConformi() / this.totaleMisure() * 100) : 0);

  ngOnInit() {
    this.loadStabilimenti();
  }

  loadStabilimenti() {
    this.api.getStabilimenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.stabilimenti.set(data) });
  }

  onStabilimentoChange() {
    const id = this.filterStabilimento();
    if (!id) {
      this.dashboardData.set([]); this.storicoData.set([]);
      this.nonConformi.set([]); this.riepilogo.set({});
      return;
    }
    this.loadPuntiMonitoraggio(id);
    this.loadDashboard(id);
    this.loadRiepilogo(id);
    this.loadNonConformi(id);
  }

  loadPuntiMonitoraggio(stabilimentoId: number) {
    this.api.getPuntiMonitoraggioByStabilimento(stabilimentoId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.puntiMon.set(data) });
  }

  loadDashboard(stabilimentoId: number) {
    this.loading.set(true);
    this.api.getConformitaDashboard(stabilimentoId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => { this.dashboardData.set(data); this.loading.set(false); },
        error: () => { this.errorMsg.set('Errore nel caricamento della dashboard'); this.loading.set(false); }
      });
  }

  loadRiepilogo(stabilimentoId: number) {
    this.api.getRiepilogoConformita(stabilimentoId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.riepilogo.set(data) });
  }

  loadNonConformi(stabilimentoId: number) {
    this.api.getNonConformi(stabilimentoId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.nonConformi.set(data) });
  }

  loadStorico() {
    const stId = this.filterStabilimento();
    if (!stId) return;
    this.loading.set(true);
    this.api.getRilevazioni({
      stabilimentoId: stId,
      from: this.filterFrom(),
      to:   this.filterTo()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => { this.storicoData.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('Errore nel caricamento dello storico'); this.loading.set(false); }
    });
  }

  onViewChange(view: ActiveView) {
    this.activeView.set(view);
    if (view === 'storico') this.loadStorico();
  }

  // ── Modal rilevazione ──────────────────────────────────────────────
  openNew(monitoraggioId?: number) {
    this.editingId.set(null);
    this.formData.set({
      dataCampionamento: new Date().toISOString().slice(0, 10),
      monitoraggioId: monitoraggioId
    });
    this.loadParametriForForm(monitoraggioId);
    this.showModal.set(true);
  }

  editRilevazione(r: RilevazioneMisura) {
    this.editingId.set(r.id!);
    this.formData.set({ ...r });
    this.loadParametriForForm(r.monitoraggioId);
    this.showModal.set(true);
  }

  loadParametriForForm(monitoraggioId?: number) {
    if (!monitoraggioId) {
      this.parametriDisp.set([]);
      return;
    }
    const punto = this.puntiMon().find(p => p.id === monitoraggioId);
    if (punto?.parametri) {
      this.parametriDisp.set(punto.parametri
        .filter(p => p.attivo !== false)
        .map(p => ({
          id: p.id!,
          label: `${p.nome}${p.codice ? ' (' + p.codice + ')' : ''}${p.limiteValore != null ? ' — limite: ≤ ' + p.limiteValore + ' ' + (p.limiteUnita ?? p.unitaMisura ?? '') : ''}`,
          limite: p.limiteValore,
          unitaMisura: p.unitaMisura
        })));
    } else {
      // Carica dal server se non in cache
      this.api.getConformitaDashboardMonitoraggio(monitoraggioId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({ next: data => {
          const uniqueParams = new Map<number, typeof data[0]>();
          for (const r of data) uniqueParams.set(r.parametroMonitoraggioId, r);
          this.parametriDisp.set(Array.from(uniqueParams.values()).map(r => ({
            id: r.parametroMonitoraggioId,
            label: `${r.parametroNome ?? ''}${r.parametroLimiteValore != null ? ' — limite: ≤ ' + r.parametroLimiteValore + ' ' + (r.parametroUnitaMisura ?? '') : ''}`,
            limite: r.parametroLimiteValore,
            unitaMisura: r.parametroUnitaMisura
          })));
        }});
    }
  }

  onMonitoraggioFormChange(monId: number | undefined) {
    this.patchForm('monitoraggioId', monId);
    this.patchForm('parametroMonitoraggioId', undefined);
    this.loadParametriForForm(monId);
  }

  onParametroSelect(paramId: number) {
    this.patchForm('parametroMonitoraggioId', paramId);
    const p = this.parametriDisp().find(x => x.id === paramId);
    if (p?.unitaMisura) this.patchForm('unitaMisura', p.unitaMisura);
  }

  saveRilevazione() {
    const f = this.formData();
    if (!f.parametroMonitoraggioId || !f.dataCampionamento || f.valoreMisurato == null) return;
    const id = this.editingId();
    const obs = id
      ? this.api.updateRilevazione(id, f)
      : this.api.createRilevazione(f as RilevazioneMisura);
    obs.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: saved => {
        // Aggiorna vista corrente
        const stId = this.filterStabilimento();
        if (stId) {
          this.loadDashboard(stId);
          this.loadRiepilogo(stId);
          this.loadNonConformi(stId);
          if (this.activeView() === 'storico') this.loadStorico();
        }
        this.showModal.set(false);
      },
      error: () => this.errorMsg.set('Errore nel salvataggio della rilevazione')
    });
  }

  requestDelete(id: number) { this.confirmDeleteId.set(id); }
  cancelDelete()            { this.confirmDeleteId.set(null); }

  confirmDelete() {
    const id = this.confirmDeleteId();
    if (!id) return;
    this.api.deleteRilevazione(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const stId = this.filterStabilimento();
          if (stId) {
            this.loadDashboard(stId);
            this.loadRiepilogo(stId);
            this.loadNonConformi(stId);
            if (this.activeView() === 'storico') this.loadStorico();
          }
          this.confirmDeleteId.set(null);
        },
        error: () => this.errorMsg.set('Errore nell\'eliminazione')
      });
  }

  patchForm(field: string, value: any) {
    this.formData.update(f => ({ ...f, [field]: value }));
  }

  // ── Helpers ───────────────────────────────────────────────────────
  statoLabel(s: StatoConformita | undefined | null): string {
    return s ? (STATO_LABEL[s] ?? s) : '—';
  }

  statoClass(s: StatoConformita | undefined | null): string {
    return s ? (STATO_CLASS[s] ?? '') : '';
  }

  statoIcon(s: StatoConformita | undefined | null): string {
    return s ? (STATO_ICON[s] ?? '⚪') : '⚪';
  }

  tipoIcon(t: string | undefined | null): string {
    if (!t) return '';
    return TIPO_ICON[t as TipoMonitoraggio] ?? '';
  }

  tipoLabel(t: string | undefined | null): string {
    if (!t) return t ?? '';
    return TIPO_LABEL[t as TipoMonitoraggio] ?? t;
  }

  percValoreVsLimite(valore: number | undefined, limite: number | undefined): number {
    if (!valore || !limite) return 0;
    return Math.min(Math.round((valore / limite) * 100), 200);
  }

  barsClass(perc: number): string {
    if (perc > 100) return 'bar-nc';
    if (perc > 80)  return 'bar-att';
    return 'bar-ok';
  }

  isResponsabileOrAdmin(): boolean {
    const r = this.currentUser()?.ruolo;
    return r === 'ADMIN' || r === 'RESPONSABILE' || r === 'OPERATORE';
  }

  private defaultFrom(): string {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 1);
    return d.toISOString().slice(0, 10);
  }
}
