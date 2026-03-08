import { Component, inject, signal, computed, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import {
  CodiceRifiuto, MovimentoRifiuto,
  StatoFisicoRifiuto, TipoMovimento,
  TIPO_MOVIMENTO_LABEL, TIPO_MOVIMENTO_ICON, TIPO_MOVIMENTO_CLASS,
  MESI_LABEL, CER_TEMPLATE
} from '../../models/rifiuti.model';
import { Stabilimento } from '../../models/stabilimento.model';

type ActiveTab = 'anagrafica' | 'movimenti';

@Component({
  selector: 'app-rifiuti',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rifiuti.component.html',
  styleUrl: './rifiuti.component.css'
})
export class RifiutiComponent implements OnInit {

  private api        = inject(ApiService);
  private auth       = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // ── Dati ──────────────────────────────────────────────────────────
  stabilimenti  = signal<Stabilimento[]>([]);
  codici        = signal<CodiceRifiuto[]>([]);
  movimenti     = signal<MovimentoRifiuto[]>([]);
  anniDisp      = signal<number[]>([new Date().getFullYear()]);
  loading       = signal(false);
  errorMsg      = signal<string | null>(null);

  // ── Navigazione ────────────────────────────────────────────────────
  activeTab = signal<ActiveTab>('anagrafica');

  // ── Filtri ────────────────────────────────────────────────────────
  filterStabilimento = signal<number | null>(null);
  filterAnno         = signal<number>(new Date().getFullYear());
  filterMese         = signal<number | null>(null);
  filterPericoloso   = signal<boolean | null>(null);
  filterTipoMov      = signal<TipoMovimento | null>(null);
  searchCer          = signal('');

  // ── Codice selezionato (per vedere i movimenti correlati) ──────────
  selectedCodiceId = signal<number | null>(null);

  // ── Modal codice ───────────────────────────────────────────────────
  showCodiceModal  = signal(false);
  editingCodiceId  = signal<number | null>(null);
  codiceForm       = signal<Partial<CodiceRifiuto>>({});

  // ── Modal movimento ────────────────────────────────────────────────
  showMovModal     = signal(false);
  editingMovId     = signal<number | null>(null);
  movForm          = signal<Partial<MovimentoRifiuto>>({});

  // ── Conferma eliminazione ──────────────────────────────────────────
  confirmDeleteCodiceId  = signal<number | null>(null);
  confirmDeleteMovId     = signal<number | null>(null);

  // ── Enum exports ───────────────────────────────────────────────────
  readonly statiF           = Object.values(StatoFisicoRifiuto);
  readonly tipiMov          = Object.values(TipoMovimento);
  readonly TipoMovimento    = TipoMovimento;
  readonly TIPO_MOV_LABEL   = TIPO_MOVIMENTO_LABEL;
  readonly TIPO_MOV_ICON    = TIPO_MOVIMENTO_ICON;
  readonly TIPO_MOV_CLASS   = TIPO_MOVIMENTO_CLASS;
  readonly MESI_LABEL       = MESI_LABEL;
  readonly MESI_NUM         = [1,2,3,4,5,6,7,8,9,10,11,12];

  currentUser = () => this.auth.currentUserValue;

  // ── Computed ───────────────────────────────────────────────────────
  codiciFiltered = computed(() => {
    let list = this.codici();
    const p = this.filterPericoloso();
    const q = this.searchCer().toLowerCase().trim();
    if (p != null) list = list.filter(c => c.pericoloso === p);
    if (q) list = list.filter(c =>
      c.codiceCer.toLowerCase().includes(q) ||
      c.descrizione.toLowerCase().includes(q)
    );
    return list;
  });

  movimentiFiltrati = computed(() => {
    let list = this.movimenti();
    const tipo = this.filterTipoMov();
    const mese = this.filterMese();
    const codId = this.selectedCodiceId();
    if (codId) list = list.filter(m => m.codiceRifiutoId === codId);
    if (tipo)  list = list.filter(m => m.tipoMovimento === tipo);
    if (mese)  list = list.filter(m => m.mese === mese);
    return list.sort((a, b) => {
      const ma = a.mese ?? 0, mb = b.mese ?? 0;
      return ma !== mb ? ma - mb : (a.tipoMovimento > b.tipoMovimento ? 1 : -1);
    });
  });

  // Totali per tipo di movimento (anno selezionato)
  totaliPerTipo = computed(() => {
    const map = new Map<TipoMovimento, number>();
    for (const m of this.movimentiFiltrati()) {
      map.set(m.tipoMovimento, (map.get(m.tipoMovimento) ?? 0) + (m.quantita ?? 0));
    }
    return map;
  });

  ngOnInit() {
    this.loadStabilimenti();
  }

  loadStabilimenti() {
    this.api.getStabilimenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => { this.stabilimenti.set(data); } });
  }

  onStabilimentoChange() {
    const id = this.filterStabilimento();
    if (!id) { this.codici.set([]); this.movimenti.set([]); return; }
    this.loadCodici();
    this.loadMovimenti();
    this.api.getAnniRifiuti(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: anni => {
        const y = new Date().getFullYear();
        this.anniDisp.set(anni.length ? anni : [y]);
      }});
  }

  loadCodici() {
    const id = this.filterStabilimento();
    if (!id) return;
    this.loading.set(true);
    this.api.getCodiciRifiuto(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => { this.codici.set(data); this.loading.set(false); },
        error: () => { this.errorMsg.set('Errore nel caricamento'); this.loading.set(false); }
      });
  }

  loadMovimenti() {
    const id = this.filterStabilimento();
    if (!id) return;
    this.api.getMovimentiRifiuto({ stabilimentoId: id, anno: this.filterAnno() })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.movimenti.set(data) });
  }

  onAnnoChange() {
    this.selectedCodiceId.set(null);
    this.loadMovimenti();
  }

  // ── Codice ────────────────────────────────────────────────────────
  openNewCodice() {
    this.editingCodiceId.set(null);
    this.codiceForm.set({ stabilimentoId: this.filterStabilimento() ?? undefined, pericoloso: false, unitaMisura: 't', attivo: true });
    this.showCodiceModal.set(true);
  }

  editCodice(c: CodiceRifiuto) {
    this.editingCodiceId.set(c.id!);
    this.codiceForm.set({ ...c });
    this.showCodiceModal.set(true);
  }

  saveCodice() {
    const f = this.codiceForm();
    if (!f.codiceCer || !f.descrizione || !f.stabilimentoId) return;
    const id = this.editingCodiceId();
    const obs = id
      ? this.api.updateCodiceRifiuto(id, f)
      : this.api.createCodiceRifiuto(f as CodiceRifiuto);
    obs.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: saved => {
        this.codici.update(list =>
          id ? list.map(c => c.id === id ? saved : c) : [...list, saved]
        );
        this.showCodiceModal.set(false);
      },
      error: () => this.errorMsg.set('Errore nel salvataggio del codice CER')
    });
  }

  importTemplate() {
    const stId = this.filterStabilimento();
    if (!stId) return;
    const existing = this.codici().map(c => c.codiceCer);
    const toAdd = CER_TEMPLATE.filter(t => !existing.includes(t.codiceCer));
    let done = 0;
    if (!toAdd.length) return;
    for (const t of toAdd) {
      this.api.createCodiceRifiuto({ ...t, stabilimentoId: stId } as CodiceRifiuto)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({ next: saved => {
          this.codici.update(list => [...list, saved]);
          done++;
        }});
    }
  }

  requestDeleteCodice(id: number) { this.confirmDeleteCodiceId.set(id); }
  cancelDeleteCodice()            { this.confirmDeleteCodiceId.set(null); }

  confirmDeleteCodice() {
    const id = this.confirmDeleteCodiceId();
    if (!id) return;
    this.api.deleteCodiceRifiuto(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.codici.update(list => list.filter(c => c.id !== id));
          if (this.selectedCodiceId() === id) this.selectedCodiceId.set(null);
          this.confirmDeleteCodiceId.set(null);
        },
        error: () => this.errorMsg.set('Impossibile eliminare (ci sono movimenti associati?)')
      });
  }

  patchCodiceForm(field: string, value: any) {
    this.codiceForm.update(f => ({ ...f, [field]: value }));
  }

  // ── Movimento ─────────────────────────────────────────────────────
  openNewMovimento(codiceId?: number) {
    this.editingMovId.set(null);
    this.movForm.set({
      codiceRifiutoId: codiceId ?? this.selectedCodiceId() ?? undefined,
      anno: this.filterAnno(),
      mese: this.filterMese() ?? undefined,
      tipoMovimento: TipoMovimento.PRODUZIONE
    });
    this.showMovModal.set(true);
  }

  editMovimento(m: MovimentoRifiuto) {
    this.editingMovId.set(m.id!);
    this.movForm.set({ ...m });
    this.showMovModal.set(true);
  }

  saveMovimento() {
    const f = this.movForm();
    if (!f.codiceRifiutoId || !f.anno || !f.tipoMovimento || f.quantita == null) return;
    const id = this.editingMovId();
    const obs = id
      ? this.api.updateMovimentoRifiuto(id, f)
      : this.api.createMovimentoRifiuto(f as MovimentoRifiuto);
    obs.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: saved => {
        this.movimenti.update(list =>
          id ? list.map(m => m.id === id ? saved : m) : [...list, saved]
        );
        this.showMovModal.set(false);
      },
      error: () => this.errorMsg.set('Errore nel salvataggio del movimento')
    });
  }

  requestDeleteMov(id: number) { this.confirmDeleteMovId.set(id); }
  cancelDeleteMov()            { this.confirmDeleteMovId.set(null); }

  confirmDeleteMov() {
    const id = this.confirmDeleteMovId();
    if (!id) return;
    this.api.deleteMovimentoRifiuto(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.movimenti.update(list => list.filter(m => m.id !== id));
          this.confirmDeleteMovId.set(null);
        },
        error: () => this.errorMsg.set('Errore nell\'eliminazione')
      });
  }

  patchMovForm(field: string, value: any) {
    this.movForm.update(f => ({ ...f, [field]: value }));
  }

  // ── Helpers ───────────────────────────────────────────────────────
  meseLabel(m: number | undefined): string {
    return m ? (MESI_LABEL[m - 1] ?? '—') : 'Annuale';
  }

  cerClass(c: CodiceRifiuto): string {
    return c.pericoloso ? 'cer-pericoloso' : 'cer-normale';
  }

  isResponsabileOrAdmin(): boolean {
    const r = this.currentUser()?.ruolo;
    return r === 'ADMIN' || r === 'RESPONSABILE';
  }

  anniRange(): number[] {
    const y = new Date().getFullYear();
    const set = new Set([...this.anniDisp(), y, y + 1]);
    return Array.from(set).sort((a, b) => b - a);
  }

  getCodiceById(id: number): CodiceRifiuto | undefined {
    return this.codici().find(c => c.id === id);
  }

  selectCodice(id: number) {
    this.selectedCodiceId.set(this.selectedCodiceId() === id ? null : id);
  }

  tipoMovLabel(t: string): string {
    return TIPO_MOVIMENTO_LABEL[t as TipoMovimento] ?? t;
  }

  tipoMovIcon(t: string): string {
    return TIPO_MOVIMENTO_ICON[t as TipoMovimento] ?? '📦';
  }

  tipoMovClass(t: string): string {
    return TIPO_MOVIMENTO_CLASS[t as TipoMovimento] ?? '';
  }
}
