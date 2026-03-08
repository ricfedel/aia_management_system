import { Component, inject, signal, computed, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import {
  RegistroMensile, VoceProduzione,
  CategoriaVoce, StatoRegistro,
  CATEGORIA_LABEL, CATEGORIA_ICON, CATEGORIA_UNITA_DEFAULT,
  MESI_LABEL, VOCI_TEMPLATE
} from '../../models/produzione.model';
import { Stabilimento } from '../../models/stabilimento.model';

@Component({
  selector: 'app-produzione-consumi',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './produzione-consumi.component.html',
  styleUrl: './produzione-consumi.component.css'
})
export class ProduzioneConsumiComponent implements OnInit {

  private api        = inject(ApiService);
  private auth       = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // ── Dati ──────────────────────────────────────────────────────────────
  registri      = signal<RegistroMensile[]>([]);
  stabilimenti  = signal<Stabilimento[]>([]);
  anniDisponibili = signal<number[]>([]);
  loading       = signal(false);
  errorMsg      = signal<string | null>(null);

  // ── Filtri ────────────────────────────────────────────────────────────
  filterStabilimento = signal<number | null>(null);
  filterAnno         = signal<number>(new Date().getFullYear());

  // ── Vista attiva ──────────────────────────────────────────────────────
  activeRegistroId = signal<number | null>(null);

  // ── Editing voci ──────────────────────────────────────────────────────
  editingVoci   = signal<VoceProduzione[]>([]);
  isDirty       = signal(false);
  savingBatch   = signal(false);

  // ── Modal nuovo registro ──────────────────────────────────────────────
  showNewModal    = signal(false);
  newRegistroForm = signal<{ stabilimentoId?: number; anno: number; mese: number; note?: string }>({
    anno: new Date().getFullYear(),
    mese: new Date().getMonth() + 1
  });

  // ── Conferma eliminazione ─────────────────────────────────────────────
  confirmDeleteId = signal<number | null>(null);

  // ── Enum exports ──────────────────────────────────────────────────────
  readonly categorie        = Object.values(CategoriaVoce);
  readonly statiRegistro    = Object.values(StatoRegistro);
  readonly StatoRegistro    = StatoRegistro;
  readonly CATEGORIA_LABEL  = CATEGORIA_LABEL;
  readonly CATEGORIA_ICON   = CATEGORIA_ICON;
  readonly MESI_LABEL       = MESI_LABEL;
  readonly VOCI_TEMPLATE    = VOCI_TEMPLATE;

  currentUser = () => this.auth.currentUserValue;

  // ── Anno corrente per grid ────────────────────────────────────────────
  readonly MESI_NUM = [1,2,3,4,5,6,7,8,9,10,11,12];

  // ── Grid: matrice stabilimento × mese per l'anno selezionato ─────────
  gridData = computed(() => {
    const anno = this.filterAnno();
    const stId = this.filterStabilimento();
    const list = this.registri().filter(r => r.anno === anno && (!stId || r.stabilimentoId === stId));
    // Mappa mese→registro
    const byMese = new Map<number, RegistroMensile>();
    for (const r of list) byMese.set(r.mese, r);
    return byMese;
  });

  activeRegistro = computed(() =>
    this.registri().find(r => r.id === this.activeRegistroId()) ?? null
  );

  // ── Voci grouped per editing ──────────────────────────────────────────
  vociByCategoria = computed(() => {
    const voci = this.editingVoci();
    const map = new Map<CategoriaVoce, VoceProduzione[]>();
    for (const cat of this.categorie) {
      const items = voci.filter(v => v.categoria === cat);
      if (items.length) map.set(cat as CategoriaVoce, items);
    }
    return map;
  });

  // ── Totali per categoria ──────────────────────────────────────────────
  totaleCategoria = computed(() => {
    const map = new Map<CategoriaVoce, number>();
    for (const cat of this.categorie) {
      const items = this.editingVoci().filter(v => v.categoria === cat);
      const tot = items.reduce((s, v) => s + (v.quantita ?? 0), 0);
      map.set(cat as CategoriaVoce, tot);
    }
    return map;
  });

  ngOnInit() {
    this.loadStabilimenti();
    this.loadAnni();
    this.loadRegistri();
  }

  loadStabilimenti() {
    this.api.getStabilimenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.stabilimenti.set(data) });
  }

  loadAnni() {
    this.api.getAnniProduzione()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: anni => {
          const currentYear = new Date().getFullYear();
          this.anniDisponibili.set(anni.length ? anni : [currentYear]);
        }
      });
  }

  loadRegistri() {
    this.loading.set(true);
    const stId = this.filterStabilimento();
    const anno = this.filterAnno();
    this.api.getRegistriMensili(stId ?? undefined, anno)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => { this.registri.set(data); this.loading.set(false); },
        error: () => { this.errorMsg.set('Errore nel caricamento dei dati'); this.loading.set(false); }
      });
  }

  onFiltriChange() {
    this.activeRegistroId.set(null);
    this.editingVoci.set([]);
    this.loadRegistri();
  }

  // ── Selezione mese ────────────────────────────────────────────────────
  selectRegistro(r: RegistroMensile) {
    if (r.id === this.activeRegistroId()) {
      this.activeRegistroId.set(null);
      this.editingVoci.set([]);
      return;
    }
    this.activeRegistroId.set(r.id ?? null);
    this.editingVoci.set((r.voci ?? []).map(v => ({ ...v })));
    this.isDirty.set(false);
  }

  selectMese(mese: number) {
    const r = this.gridData().get(mese);
    if (r) {
      this.selectRegistro(r);
    } else {
      // Nessun registro per questo mese → proponi creazione
      this.newRegistroForm.set({
        stabilimentoId: this.filterStabilimento() ?? undefined,
        anno: this.filterAnno(),
        mese
      });
      this.showNewModal.set(true);
    }
  }

  // ── Editing voci ──────────────────────────────────────────────────────
  patchVoce(index: number, field: keyof VoceProduzione, value: any) {
    this.editingVoci.update(list => {
      const copy = [...list];
      copy[index] = { ...copy[index], [field]: value };
      return copy;
    });
    this.isDirty.set(true);
  }

  addVoceCustom(categoria: CategoriaVoce) {
    const defaultUnita = CATEGORIA_UNITA_DEFAULT[categoria];
    this.editingVoci.update(list => [
      ...list,
      { categoria, descrizione: '', unitaMisura: defaultUnita, sortOrder: list.length * 10 }
    ]);
    this.isDirty.set(true);
  }

  removeVoce(index: number) {
    this.editingVoci.update(list => list.filter((_, i) => i !== index));
    this.isDirty.set(true);
  }

  applyTemplate() {
    const existing = this.editingVoci();
    const newVoci = VOCI_TEMPLATE.filter(t =>
      !existing.some(e => e.descrizione === t.descrizione && e.categoria === t.categoria)
    );
    this.editingVoci.update(list => [...list, ...newVoci]);
    this.isDirty.set(true);
  }

  saveBatch() {
    const registroId = this.activeRegistroId();
    if (!registroId) return;
    this.savingBatch.set(true);
    this.api.saveVociBatch(registroId, this.editingVoci())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: saved => {
          this.registri.update(list => list.map(r => r.id === registroId ? saved : r));
          this.editingVoci.set((saved.voci ?? []).map(v => ({ ...v })));
          this.isDirty.set(false);
          this.savingBatch.set(false);
        },
        error: () => {
          this.errorMsg.set('Errore nel salvataggio dei dati');
          this.savingBatch.set(false);
        }
      });
  }

  // ── Crea nuovo registro ───────────────────────────────────────────────
  submitNewRegistro() {
    const f = this.newRegistroForm();
    if (!f.stabilimentoId || !f.anno || !f.mese) return;

    const payload: RegistroMensile = {
      stabilimentoId: f.stabilimentoId,
      anno: f.anno,
      mese: f.mese,
      note: f.note,
      voci: []
    };

    this.api.createRegistroMensile(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: saved => {
          this.registri.update(list => [...list, saved]);
          this.showNewModal.set(false);
          this.selectRegistro(saved);
          // Auto-applica template se nuovo
          this.applyTemplate();
        },
        error: (err) => {
          this.errorMsg.set(err.status === 409
            ? 'Esiste già un registro per questo mese.'
            : 'Errore nella creazione del registro');
        }
      });
  }

  // ── Cambia stato registro ─────────────────────────────────────────────
  cambiaStato(registroId: number, stato: StatoRegistro) {
    this.api.updateRegistroMensile(registroId, { stato })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: saved => this.registri.update(list => list.map(r => r.id === registroId ? saved : r)),
        error: () => this.errorMsg.set('Errore nell\'aggiornamento dello stato')
      });
  }

  // ── Elimina registro ──────────────────────────────────────────────────
  requestDelete(id: number) { this.confirmDeleteId.set(id); }
  cancelDelete()            { this.confirmDeleteId.set(null); }

  confirmDelete() {
    const id = this.confirmDeleteId();
    if (!id) return;
    this.api.deleteRegistroMensile(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.registri.update(list => list.filter(r => r.id !== id));
          if (this.activeRegistroId() === id) {
            this.activeRegistroId.set(null);
            this.editingVoci.set([]);
          }
          this.confirmDeleteId.set(null);
        },
        error: () => this.errorMsg.set('Errore nell\'eliminazione')
      });
  }

  // ── Helpers ───────────────────────────────────────────────────────────
  meseLabel(m: number): string { return MESI_LABEL[m - 1] ?? '—'; }

  statoClass(s: StatoRegistro | undefined): string {
    switch (s) {
      case StatoRegistro.BOZZA:       return 'stato-bozza';
      case StatoRegistro.INVIATO:     return 'stato-inviato';
      case StatoRegistro.APPROVATO:   return 'stato-approvato';
      case StatoRegistro.RETTIFICATO: return 'stato-rettificato';
      default: return '';
    }
  }

  statoLabel(s: StatoRegistro | undefined): string {
    switch (s) {
      case StatoRegistro.BOZZA:       return 'Bozza';
      case StatoRegistro.INVIATO:     return 'Inviato';
      case StatoRegistro.APPROVATO:   return 'Approvato';
      case StatoRegistro.RETTIFICATO: return 'Rettificato';
      default: return '—';
    }
  }

  variazione(corrente: number | undefined, precedente: number | undefined): number | null {
    if (corrente == null || precedente == null || precedente === 0) return null;
    return Math.round(((corrente - precedente) / precedente) * 100);
  }

  isResponsabileOrAdmin(): boolean {
    const r = this.currentUser()?.ruolo;
    return r === 'ADMIN' || r === 'RESPONSABILE';
  }

  getCategorieKeys(): CategoriaVoce[] {
    return Array.from(this.vociByCategoria().keys());
  }

  getVociByCategoria(cat: CategoriaVoce): VoceProduzione[] {
    return this.vociByCategoria().get(cat) ?? [];
  }

  getVociIndices(cat: CategoriaVoce): number[] {
    return this.editingVoci()
      .map((v, i) => ({ v, i }))
      .filter(x => x.v.categoria === cat)
      .map(x => x.i);
  }

  patchNewForm(field: string, value: any) {
    this.newRegistroForm.update(f => ({ ...f, [field]: value }));
  }

  anniRange(): number[] {
    const currentYear = new Date().getFullYear();
    const anni = this.anniDisponibili();
    // Includi sempre l'anno corrente e quello successivo
    const set = new Set([...anni, currentYear, currentYear + 1]);
    return Array.from(set).sort((a, b) => b - a);
  }
}
