import {
  Component, OnInit, inject, signal, DestroyRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiService } from '../../services/api.service';
import { DefinizioneFlusso, StepPreview } from '../../models/definizione-flusso.model';
import { WorkflowEditorComponent } from '../workflow-editor/workflow-editor.component';

@Component({
  selector: 'app-workflow-list',
  standalone: true,
  imports: [CommonModule, WorkflowEditorComponent],
  templateUrl: './workflow-list.component.html',
  styleUrl:    './workflow-list.component.css'
})
export class WorkflowListComponent implements OnInit {

  private readonly api  = inject(ApiService);
  private readonly dref = inject(DestroyRef);

  // ─── State ───────────────────────────────────────────────────────────────
  definizioni   = signal<DefinizioneFlusso[]>([]);
  loading       = signal(true);
  errore        = signal<string | null>(null);

  /** null = lista; undefined = nuovo; DefinizioneFlusso = edit */
  modalEditor   = signal<DefinizioneFlusso | null | undefined>(null);

  stepsModal    = signal<{ nome: string; steps: StepPreview[] } | null>(null);
  confirmDelete = signal<DefinizioneFlusso | null>(null);

  // ─── Init ────────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.carica();
  }

  carica(): void {
    this.loading.set(true);
    this.api.getDefinizioniFlusso(true)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: list => { this.definizioni.set(list); this.loading.set(false); },
        error: () => { this.errore.set('Errore nel caricamento dei flussi'); this.loading.set(false); }
      });
  }

  // ─── Azioni ──────────────────────────────────────────────────────────────

  nuovo(): void {
    this.modalEditor.set(undefined); // undefined = modalità creazione
  }

  modifica(df: DefinizioneFlusso): void {
    this.modalEditor.set(df);
  }

  chiudiEditor(): void {
    this.modalEditor.set(null);
  }

  onSalvato(df: DefinizioneFlusso): void {
    this.chiudiEditor();
    this.carica();
  }

  anteprimaStep(df: DefinizioneFlusso): void {
    this.api.previewDefinizioneFlusso(df.id)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: steps => this.stepsModal.set({ nome: df.nome, steps }),
        error: () => this.errore.set('Errore nel parsing del flusso')
      });
  }

  chiediConfermaEliminazione(df: DefinizioneFlusso): void {
    this.confirmDelete.set(df);
  }

  confermaEliminazione(): void {
    const df = this.confirmDelete();
    if (!df) return;
    this.api.disattivaDefinizioneFlusso(df.id)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: () => {
          this.confirmDelete.set(null);
          this.carica();
        },
        error: () => this.errore.set('Errore nella disattivazione')
      });
  }

  // ─── Template helpers ────────────────────────────────────────────────────

  tipoIcon(tipo: string): string {
    return tipo === 'SERVICE_TASK' ? '⚙️' : '👤';
  }

  trackById(_i: number, df: DefinizioneFlusso): number { return df.id; }

  get inEditor(): boolean {
    return this.modalEditor() !== null;
  }

  get definizioneDaEditare(): DefinizioneFlusso | undefined {
    const v = this.modalEditor();
    return v === null ? undefined : v;
  }
}
