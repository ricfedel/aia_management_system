import {
  Component, inject, signal, computed, DestroyRef, OnInit
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import {
  ProcessoDocumentoDTO, TaskProcessoDTO, AvviaProcessoRequest,
  CompletaTaskRequest, StatoProcesso, TipoProcesso,
  TIPO_PROCESSO_LABELS, STATO_PROCESSO_LABELS, STATO_PROCESSO_COLORS
} from '../../models/processo.model';
import { Documento } from '../../models/documento.model';
import { Stabilimento } from '../../models/stabilimento.model';
import { BpmnViewerComponent } from '../bpmn-viewer/bpmn-viewer.component';
import { DefinizioneFlusso } from '../../models/definizione-flusso.model';

@Component({
  selector: 'app-processi-list',
  standalone: true,
  imports: [CommonModule, FormsModule, BpmnViewerComponent],
  templateUrl: './processi-list.component.html',
  styleUrl: './processi-list.component.css'
})
export class ProcessiListComponent implements OnInit {

  private readonly api   = inject(ApiService);
  private readonly auth  = inject(AuthService);
  private readonly dref  = inject(DestroyRef);

  // ─── State ───────────────────────────────────────────────────────────────
  processi          = signal<ProcessoDocumentoDTO[]>([]);
  documenti         = signal<Documento[]>([]);
  stabilimenti      = signal<Stabilimento[]>([]);
  definizioniFlusso = signal<DefinizioneFlusso[]>([]);
  loading           = signal(true);
  activeTab         = signal<'attivi' | 'tutti' | 'miei'>('attivi');

  selectedProcesso = signal<ProcessoDocumentoDTO | null>(null);
  showAvviaForm    = signal(false);
  showTaskForm     = signal(false);
  selectedTask     = signal<TaskProcessoDTO | null>(null);

  /** Modalità form avvio: 'predefinito' | 'custom' */
  modalitaAvvio = signal<'predefinito' | 'custom'>('predefinito');

  avviaData = signal<AvviaProcessoRequest>({
    documentoId: 0,
    tipoProcesso: 'LAVORAZIONE_DOCUMENTO',
    assegnatoA: '',
    note: ''
  });

  completaData = signal<CompletaTaskRequest>({
    esito: 'APPROVATO',
    commento: ''
  });

  // ─── Constants (exposed to template) ────────────────────────────────────
  readonly tipiProcesso: TipoProcesso[] = [
    'LAVORAZIONE_DOCUMENTO', 'RINNOVO_AIA', 'NON_CONFORMITA', 'INTEGRAZIONE_ENTE'
  ];
  readonly tipoLabels = TIPO_PROCESSO_LABELS;
  readonly statoLabels = STATO_PROCESSO_LABELS;
  readonly statoColors = STATO_PROCESSO_COLORS;
  readonly bpmnUrl = 'assets/bpmn/lavorazione-documento.bpmn';

  // ─── Computed ────────────────────────────────────────────────────────────
  processiAttivi = computed(() =>
    this.processi().filter(p => !['COMPLETATO','ANNULLATO'].includes(p.stato))
  );

  processiUtente = computed(() => {
    const me = this.auth.currentUserValue?.username ?? '';
    return this.processi().filter(p =>
      p.avviatoDa === me || p.assegnatoA === me
    );
  });

  displayedProcessi = computed(() => {
    switch (this.activeTab()) {
      case 'attivi': return this.processiAttivi();
      case 'miei':   return this.processiUtente();
      default:       return this.processi();
    }
  });

  taskStatMap = computed<Record<string, string>>(() => {
    const p = this.selectedProcesso();
    if (!p) return {};
    const map: Record<string, string> = {};
    p.tasks.forEach(t => { map[t.taskIdBpmn] = t.statoTask; });
    return map;
  });

  activeTaskBpmnId = computed<string | undefined>(() => {
    const p = this.selectedProcesso();
    if (!p) return undefined;
    const active = p.tasks.find(t => t.statoTask === 'IN_CORSO');
    return active?.taskIdBpmn;
  });

  canManage = computed(() => {
    const r = this.auth.currentUserValue?.ruolo;
    return r === 'ADMIN' || r === 'RESPONSABILE' || r === 'OPERATORE';
  });

  canAdmin = computed(() => {
    const r = this.auth.currentUserValue?.ruolo;
    return r === 'ADMIN' || r === 'RESPONSABILE';
  });

  // ─── Lifecycle ───────────────────────────────────────────────────────────
  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.loading.set(true);
    this.api.getAllProcessi()
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: data => { this.processi.set(data); this.loading.set(false); },
        error: () => this.loading.set(false)
      });

    this.api.searchDocumenti({}).pipe(takeUntilDestroyed(this.dref))
      .subscribe({ next: r => this.documenti.set(r.content) });

    this.api.getStabilimenti().pipe(takeUntilDestroyed(this.dref))
      .subscribe({ next: s => this.stabilimenti.set(s) });

    this.api.getDefinizioniFlusso(true).pipe(takeUntilDestroyed(this.dref))
      .subscribe({ next: d => this.definizioniFlusso.set(d) });
  }

  // ─── Selezione processo ──────────────────────────────────────────────────
  selectProcesso(p: ProcessoDocumentoDTO) {
    this.selectedProcesso.set(p);
    this.showTaskForm.set(false);
    this.selectedTask.set(null);
  }

  // ─── Avvio processo ───────────────────────────────────────────────────────
  openAvviaForm() {
    const docs = this.documenti();
    this.modalitaAvvio.set('predefinito');
    this.avviaData.set({
      documentoId: docs.length > 0 ? docs[0].id : 0,
      tipoProcesso: 'LAVORAZIONE_DOCUMENTO',
      assegnatoA: this.auth.currentUserValue?.username ?? '',
      note: ''
    });
    this.showAvviaForm.set(true);
  }

  setModalitaAvvio(m: 'predefinito' | 'custom') {
    this.modalitaAvvio.set(m);
    this.avviaData.update(d => ({
      ...d,
      tipoProcesso:       m === 'predefinito' ? 'LAVORAZIONE_DOCUMENTO' : undefined,
      definizioneFlussoId: m === 'custom'      ? this.definizioniFlusso()[0]?.id : undefined
    }));
  }

  avviaProcesso() {
    const d = this.avviaData();
    if (!d.documentoId) { alert('Seleziona un documento'); return; }
    if (this.modalitaAvvio() === 'custom' && !d.definizioneFlussoId) {
      alert('Seleziona una definizione di flusso'); return;
    }
    if (this.modalitaAvvio() === 'predefinito' && !d.tipoProcesso) {
      alert('Seleziona un tipo di processo'); return;
    }
    this.api.avviaProcesso(d)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: p => {
          this.processi.update(list => [p, ...list]);
          this.showAvviaForm.set(false);
          this.selectProcesso(p);
        },
        error: err => alert(err?.error?.message ?? 'Errore avvio processo')
      });
  }

  // ─── Completamento task ───────────────────────────────────────────────────
  openTaskForm(task: TaskProcessoDTO) {
    this.selectedTask.set(task);
    this.completaData.set({ esito: 'APPROVATO', commento: '' });
    this.showTaskForm.set(true);
  }

  completaTask() {
    const processo = this.selectedProcesso();
    const task     = this.selectedTask();
    if (!processo || !task) return;

    this.api.completaTask(processo.id, task.id, this.completaData())
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: updated => {
          this.processi.update(list => list.map(p => p.id === updated.id ? updated : p));
          this.selectedProcesso.set(updated);
          this.showTaskForm.set(false);
          this.selectedTask.set(null);
        },
        error: err => alert(err?.error?.message ?? 'Errore completamento task')
      });
  }

  // ─── Azioni processo ─────────────────────────────────────────────────────
  sospendiProcesso(p: ProcessoDocumentoDTO) {
    const motivo = prompt('Motivo della sospensione (opzionale):') ?? '';
    this.api.sospendiProcesso(p.id, motivo || undefined)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: updated => this.aggiornaProcesso(updated),
        error: err => alert(err?.error?.message ?? 'Errore')
      });
  }

  riprendiProcesso(p: ProcessoDocumentoDTO) {
    this.api.riprendiProcesso(p.id)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({ next: updated => this.aggiornaProcesso(updated) });
  }

  annullaProcesso(p: ProcessoDocumentoDTO) {
    if (!confirm(`Annullare il processo "${p.codiceProcesso}"?`)) return;
    const motivo = prompt('Motivo annullamento:') ?? '';
    this.api.annullaProcesso(p.id, motivo || undefined)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({ next: updated => this.aggiornaProcesso(updated) });
  }

  private aggiornaProcesso(updated: ProcessoDocumentoDTO) {
    this.processi.update(list => list.map(p => p.id === updated.id ? updated : p));
    if (this.selectedProcesso()?.id === updated.id) {
      this.selectedProcesso.set(updated);
    }
  }

  // ─── Helpers template ────────────────────────────────────────────────────
  getStatoBadgeStyle(stato: StatoProcesso): string {
    const c = this.statoColors[stato] ?? '#94a3b8';
    return `background-color:${c}20; color:${c}; border:1px solid ${c}40`;
  }

  getTaskStepClass(task: TaskProcessoDTO): string {
    const map: Record<string, string> = {
      CREATO:     'step-pending',
      IN_CORSO:   'step-active',
      COMPLETATO: 'step-done',
      SALTATO:    'step-skip',
      ANNULLATO:  'step-error'
    };
    return map[task.statoTask] ?? 'step-pending';
  }

  isProcessoAttivo(p: ProcessoDocumentoDTO): boolean {
    return !['COMPLETATO','ANNULLATO'].includes(p.stato);
  }

  updateAvvia(field: keyof AvviaProcessoRequest, value: any) {
    this.avviaData.update(d => ({ ...d, [field]: value }));
  }

  updateCompleta(field: keyof CompletaTaskRequest, value: any) {
    this.completaData.update(d => ({ ...d, [field]: value }));
  }

  formatDate(s?: string): string {
    if (!s) return '—';
    return new Date(s).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
