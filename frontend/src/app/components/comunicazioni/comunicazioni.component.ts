import { Component, inject, signal, computed, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import {
  ComunicazioneEnte, TipoComunicazione, StatoComunicazione, EnteEsterno,
  RiepilogoComunicazioni,
  TIPO_COM_LABEL, TIPO_COM_ICON,
  STATO_COM_LABEL, STATO_COM_CLASS, STATO_COM_ICON,
  ENTE_LABEL, STATO_TRANSITIONS
} from '../../models/comunicazioni.model';
import { Stabilimento } from '../../models/stabilimento.model';

type ActiveView = 'lista' | 'in-attesa';

@Component({
  selector: 'app-comunicazioni',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comunicazioni.component.html',
  styleUrl: './comunicazioni.component.css'
})
export class ComunicazioniComponent implements OnInit {

  private api        = inject(ApiService);
  private auth       = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // ── Dati ──────────────────────────────────────────────────────────────────
  stabilimenti    = signal<Stabilimento[]>([]);
  comunicazioni   = signal<ComunicazioneEnte[]>([]);
  inAttesa        = signal<ComunicazioneEnte[]>([]);
  riepilogo       = signal<RiepilogoComunicazioni>({});
  loading         = signal(false);
  errorMsg        = signal<string | null>(null);

  // ── Vista ──────────────────────────────────────────────────────────────────
  activeView = signal<ActiveView>('lista');

  // ── Filtri lista ───────────────────────────────────────────────────────────
  filterStabilimento = signal<number | null>(null);
  filterStato        = signal<string>('');
  filterEnte         = signal<string>('');
  filterFrom         = signal<string>(this.defaultFrom());
  filterTo           = signal<string>(new Date().toISOString().slice(0, 10));

  // ── Dettaglio espanso ──────────────────────────────────────────────────────
  expandedId = signal<number | null>(null);

  // ── Modal form ─────────────────────────────────────────────────────────────
  showModal    = signal(false);
  editingId    = signal<number | null>(null);
  formData     = signal<Partial<ComunicazioneEnte>>({});

  // ── Conferma eliminazione ──────────────────────────────────────────────────
  confirmDeleteId = signal<number | null>(null);

  // ── Enum exports ──────────────────────────────────────────────────────────
  readonly TipoComunicazione  = TipoComunicazione;
  readonly StatoComunicazione = StatoComunicazione;
  readonly EnteEsterno        = EnteEsterno;
  readonly tipiComList        = Object.values(TipoComunicazione);
  readonly statiComList       = Object.values(StatoComunicazione);
  readonly entiList           = Object.values(EnteEsterno);
  readonly TIPO_COM_LABEL     = TIPO_COM_LABEL;
  readonly TIPO_COM_ICON      = TIPO_COM_ICON;
  readonly STATO_COM_LABEL    = STATO_COM_LABEL;
  readonly STATO_COM_CLASS    = STATO_COM_CLASS;
  readonly STATO_COM_ICON     = STATO_COM_ICON;
  readonly ENTE_LABEL         = ENTE_LABEL;

  currentUser = () => this.auth.currentUserValue;

  // ── Computed ───────────────────────────────────────────────────────────────
  totaleRiepilogo = computed(() =>
    (this.riepilogo().BOZZA ?? 0) +
    (this.riepilogo().INVIATA ?? 0) +
    (this.riepilogo().CONSEGNATA_PEC ?? 0) +
    (this.riepilogo().RISPOSTA_RICEVUTA ?? 0) +
    (this.riepilogo().ARCHIVIATA ?? 0)
  );

  transizioniConsentite = computed(() => {
    const id = this.expandedId();
    if (!id) return [];
    const c = this.comunicazioni().find(x => x.id === id);
    if (!c) return [];
    return STATO_TRANSITIONS[c.stato] ?? [];
  });

  // ── Lifecycle ──────────────────────────────────────────────────────────────
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
      this.comunicazioni.set([]);
      this.inAttesa.set([]);
      this.riepilogo.set({});
      return;
    }
    this.loadComunicazioni();
    this.loadRiepilogo(id);
    this.loadInAttesa(id);
  }

  loadComunicazioni() {
    const id = this.filterStabilimento();
    if (!id) return;
    this.loading.set(true);
    this.api.getComunicazioni({
      stabilimentoId: id,
      stato: this.filterStato() || undefined,
      ente:  this.filterEnte()  || undefined,
      from:  this.filterFrom()  || undefined,
      to:    this.filterTo()    || undefined,
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => { this.comunicazioni.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('Errore nel caricamento'); this.loading.set(false); }
    });
  }

  loadRiepilogo(stabilimentoId: number) {
    this.api.getRiepilogoComunicazioni(stabilimentoId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.riepilogo.set(data) });
  }

  loadInAttesa(stabilimentoId: number) {
    this.api.getInAttesaRiscontro(stabilimentoId, 30)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.inAttesa.set(data) });
  }

  onFilterChange() {
    this.loadComunicazioni();
  }

  toggleExpand(id: number) {
    this.expandedId.update(v => v === id ? null : id);
  }

  // ── Modal ──────────────────────────────────────────────────────────────────
  openNew() {
    const stId = this.filterStabilimento();
    this.editingId.set(null);
    this.formData.set({
      stabilimentoId: stId ?? undefined,
      stato:          StatoComunicazione.BOZZA,
      dataInvio:      new Date().toISOString().slice(0, 10),
      hasRiscontro:   false,
    });
    this.showModal.set(true);
  }

  editComunicazione(c: ComunicazioneEnte) {
    this.editingId.set(c.id!);
    this.formData.set({ ...c });
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  saveComunicazione() {
    const f = this.formData();
    if (!f.stabilimentoId || !f.tipo || !f.ente || !f.oggetto) return;
    const id  = this.editingId();
    const obs = id
      ? this.api.updateComunicazione(id, f)
      : this.api.createComunicazione(f as ComunicazioneEnte);

    obs.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: saved => {
        this.showModal.set(false);
        this.loadComunicazioni();
        const stId = this.filterStabilimento();
        if (stId) {
          this.loadRiepilogo(stId);
          this.loadInAttesa(stId);
        }
      },
      error: () => this.errorMsg.set('Errore nel salvataggio')
    });
  }

  cambiaStato(com: ComunicazioneEnte, nuovoStato: StatoComunicazione) {
    this.api.cambiaStatoComunicazione(com.id!, nuovoStato)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.comunicazioni.update(list =>
            list.map(c => c.id === updated.id ? updated : c)
          );
          const stId = this.filterStabilimento();
          if (stId) this.loadRiepilogo(stId);
          if (stId) this.loadInAttesa(stId);
        },
        error: () => this.errorMsg.set('Errore nel cambio stato')
      });
  }

  requestDelete(id: number) { this.confirmDeleteId.set(id); }
  cancelDelete()            { this.confirmDeleteId.set(null); }

  confirmDelete() {
    const id = this.confirmDeleteId();
    if (!id) return;
    this.api.deleteComunicazione(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.comunicazioni.update(list => list.filter(c => c.id !== id));
          this.confirmDeleteId.set(null);
          const stId = this.filterStabilimento();
          if (stId) this.loadRiepilogo(stId);
        },
        error: () => this.errorMsg.set('Errore nell\'eliminazione')
      });
  }

  patchForm(field: string, value: unknown) {
    this.formData.update(f => ({ ...f, [field]: value }));
  }

  // ── Helpers ────────────────────────────────────────────────────────────────
  tipoLabel(t: string | undefined | null): string {
    if (!t) return '—';
    return TIPO_COM_LABEL[t as TipoComunicazione] ?? t;
  }

  tipoIcon(t: string | undefined | null): string {
    if (!t) return '📧';
    return TIPO_COM_ICON[t as TipoComunicazione] ?? '📧';
  }

  statoLabel(s: string | undefined | null): string {
    if (!s) return '—';
    return STATO_COM_LABEL[s as StatoComunicazione] ?? s;
  }

  statoClass(s: string | undefined | null): string {
    if (!s) return '';
    return STATO_COM_CLASS[s as StatoComunicazione] ?? '';
  }

  statoIcon(s: string | undefined | null): string {
    if (!s) return '';
    return STATO_COM_ICON[s as StatoComunicazione] ?? '';
  }

  enteLabel(e: string | undefined | null): string {
    if (!e) return '—';
    return ENTE_LABEL[e as EnteEsterno] ?? e;
  }

  transizioniDi(com: ComunicazioneEnte): StatoComunicazione[] {
    return STATO_TRANSITIONS[com.stato] ?? [];
  }

  isResponsabileOrAdmin(): boolean {
    const r = this.currentUser()?.ruolo;
    return r === 'ADMIN' || r === 'RESPONSABILE' || r === 'OPERATORE';
  }

  allegatiList(allegati: string | undefined): string[] {
    if (!allegati) return [];
    return allegati.split(',').map(s => s.trim()).filter(Boolean);
  }

  giorniTrascorsi(data: string | undefined): number {
    if (!data) return 0;
    const diff = new Date().getTime() - new Date(data).getTime();
    return Math.floor(diff / (1000 * 60 * 60 * 24));
  }

  private defaultFrom(): string {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 1);
    return d.toISOString().slice(0, 10);
  }
}
