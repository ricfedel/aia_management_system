import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { AnagraficaCamino, FaseProcesso, FASE_PROCESSO_LABELS } from '../../models/anagrafica-camino.model';
import { Stabilimento } from '../../models/stabilimento.model';

@Component({
  selector: 'app-anagrafica-camini',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './anagrafica-camini.component.html',
  styleUrl: './anagrafica-camini.component.css'
})
export class AnagraficaCaminiComponent implements OnInit {
  private api    = inject(ApiService);
  private auth   = inject(AuthService);

  camini         = signal<AnagraficaCamino[]>([]);
  stabilimenti   = signal<Stabilimento[]>([]);
  loading        = signal(false);
  error          = signal<string | null>(null);

  // filtri
  filtroStabilimento = signal<number | null>(null);
  filtroFase         = signal<FaseProcesso | null>(null);
  filtroAttivo       = signal<boolean | null>(null);
  filtroTesto        = signal('');

  // modal
  showModal    = signal(false);
  isEdit       = signal(false);
  saving       = signal(false);
  modalError   = signal<string | null>(null);
  form         = signal<Partial<AnagraficaCamino>>({});

  // confirm delete
  deleteId     = signal<number | null>(null);

  readonly faseLabelMap = FASE_PROCESSO_LABELS;
  readonly fasiList: FaseProcesso[] = [
    'TRASPORTO','PREPULITURA','PULITURA','MACINAZIONE',
    'MOVIMENTAZIONE','MANUTENZIONE','CONTROLLO_QUALITA','ALTRO'
  ];

  caminiFiltered = computed(() => {
    const txt = this.filtroTesto().toLowerCase();
    return this.camini().filter(c => {
      if (this.filtroStabilimento() && c.stabilimentoId !== this.filtroStabilimento()) return false;
      if (this.filtroFase() && c.faseProcesso !== this.filtroFase()) return false;
      if (this.filtroAttivo() !== null && c.attivo !== this.filtroAttivo()) return false;
      if (txt && !(
        c.sigla.toLowerCase().includes(txt) ||
        (c.origine ?? '').toLowerCase().includes(txt) ||
        (c.impiantoAbbattimento ?? '').toLowerCase().includes(txt)
      )) return false;
      return true;
    });
  });

  ngOnInit() {
    this.api.getStabilimenti().subscribe(s => this.stabilimenti.set(s));
    this.loadCamini();
  }

  loadCamini() {
    this.loading.set(true);
    this.error.set(null);
    this.api.getAnagraficaCamini().subscribe({
      next: list => { this.camini.set(list); this.loading.set(false); },
      error: () => { this.error.set('Errore nel caricamento'); this.loading.set(false); }
    });
  }

  // ── Modal nuovo ────────────────────────────────────────────────────────
  openNew() {
    this.form.set({
      attivo: true,
      temperaturaAmbiente: true,
      durataGAnno: 365
    });
    this.isEdit.set(false);
    this.modalError.set(null);
    this.showModal.set(true);
  }

  openEdit(c: AnagraficaCamino) {
    this.form.set({ ...c });
    this.isEdit.set(true);
    this.modalError.set(null);
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  save() {
    const f = this.form();
    if (!f.stabilimentoId || !f.sigla) {
      this.modalError.set('Stabilimento e sigla sono obbligatori');
      return;
    }
    this.saving.set(true);
    this.modalError.set(null);

    const payload: AnagraficaCamino = {
      ...(f as AnagraficaCamino),
      temperaturaAmbiente: f.temperaturaAmbiente ?? (f.temperaturaC == null),
    };

    const req = this.isEdit() && f.id
      ? this.api.updateAnagraficaCamino(f.id, payload)
      : this.api.createAnagraficaCamino(payload);

    req.subscribe({
      next: () => { this.saving.set(false); this.showModal.set(false); this.loadCamini(); },
      error: () => { this.saving.set(false); this.modalError.set('Errore nel salvataggio'); }
    });
  }

  // ── Delete ─────────────────────────────────────────────────────────────
  confirmDelete(id: number) { this.deleteId.set(id); }
  cancelDelete()             { this.deleteId.set(null); }

  doDelete() {
    const id = this.deleteId();
    if (!id) return;
    this.api.deleteAnagraficaCamino(id).subscribe({
      next: () => { this.deleteId.set(null); this.loadCamini(); },
      error: () => this.error.set('Errore nell\'eliminazione')
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────
  temperaturaLabel(c: AnagraficaCamino): string {
    return c.temperaturaAmbiente ? 'ambiente' : `${c.temperaturaC} °C`;
  }

  updateForm(patch: Partial<AnagraficaCamino>) {
    this.form.update(f => ({ ...f, ...patch }));
  }

  trackById(_: number, c: AnagraficaCamino) { return c.id; }
}
