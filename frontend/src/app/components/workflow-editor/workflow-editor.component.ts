import {
  Component, OnInit, OnDestroy, AfterViewInit,
  inject, signal, Input, Output, EventEmitter, DestroyRef, ElementRef, ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiService } from '../../services/api.service';
import {
  DefinizioneFlusso, StepPreview, SaveDefinizioneFlussoRequest, DEFAULT_BPMN_XML
} from '../../models/definizione-flusso.model';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const require: any;

/** Tipi di auto-task configurabili */
export type AutoTaskTipo = 'API_CALL' | 'EMAIL' | 'TIMER' | 'SCRIPT' | 'WEBHOOK' | 'GENERA_PDF';

/** Form unificato per qualsiasi tipo di auto-task su un ServiceTask BPMN */
export interface AutoTaskForm {
  tipo: AutoTaskTipo;
  // API_CALL & WEBHOOK
  url: string;
  method: string;
  body: string;
  headers: string;
  webhookSecret: string;
  // EMAIL
  emailTo: string;
  emailSubject: string;
  emailBody: string;
  // TIMER
  delay: string;
  // SCRIPT (Groovy)
  script: string;
  // GENERA_PDF
  template: string;
}

const DEFAULT_FORM: AutoTaskForm = {
  tipo: 'API_CALL',
  url: '', method: 'POST', body: '', headers: '', webhookSecret: '',
  emailTo: '', emailSubject: '', emailBody: '',
  delay: '1h',
  script: '',
  template: ''
};

@Component({
  selector: 'app-workflow-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './workflow-editor.component.html',
  styleUrl:    './workflow-editor.component.css'
})
export class WorkflowEditorComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('bpmnCanvas', { static: true }) canvasRef!: ElementRef<HTMLDivElement>;

  /** Se valorizzato, carica la definizione esistente per l'editing */
  @Input() definizione?: DefinizioneFlusso;

  @Output() salvato   = new EventEmitter<DefinizioneFlusso>();
  @Output() annullato = new EventEmitter<void>();

  private readonly api  = inject(ApiService);
  private readonly dref = inject(DestroyRef);

  // ─── Form metadata ───────────────────────────────────────────────────────
  nome        = signal('');
  descrizione = signal('');

  // ─── State ───────────────────────────────────────────────────────────────
  loading      = signal(false);
  errore       = signal<string | null>(null);
  successo     = signal<string | null>(null);
  stepsPreview = signal<StepPreview[]>([]);
  showPreview  = signal(false);
  modelerReady = signal(false);

  // ─── Pannello configurazione elemento selezionato ────────────────────────
  selectedServiceTaskId   = signal<string | null>(null);
  selectedServiceTaskName = signal<string>('');
  selectedElementType     = signal<'serviceTask' | 'xorGateway' | 'andGateway' | 'callActivity' | null>(null);
  autoTaskForm            = signal<AutoTaskForm>({ ...DEFAULT_FORM });
  callActivityElement     = signal<string>('');

  readonly HTTP_METHODS: string[]   = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'];
  readonly TASK_TYPES: { value: AutoTaskTipo; label: string; icon: string }[] = [
    { value: 'API_CALL',   label: 'Chiamata API',  icon: '🌐' },
    { value: 'WEBHOOK',    label: 'Webhook',        icon: '🔗' },
    { value: 'EMAIL',      label: 'Invia Email',    icon: '📧' },
    { value: 'TIMER',      label: 'Attendi / Timer', icon: '⏱️' },
    { value: 'SCRIPT',     label: 'Script Groovy',  icon: '📜' },
    { value: 'GENERA_PDF', label: 'Genera PDF',     icon: '📄' },
  ];

  readonly DELAY_PRESETS = [
    { label: '5 minuti',  value: '5m'  },
    { label: '30 minuti', value: '30m' },
    { label: '1 ora',     value: '1h'  },
    { label: '4 ore',     value: '4h'  },
    { label: '1 giorno',  value: '1d'  },
    { label: '3 giorni',  value: '3d'  },
    { label: '1 settimana', value: '7d' },
  ];

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private modeler: any = null;

  // ─── Lifecycle ───────────────────────────────────────────────────────────

  ngOnInit(): void {
    if (this.definizione) {
      this.nome.set(this.definizione.nome);
      this.descrizione.set(this.definizione.descrizione ?? '');
    }
  }

  async ngAfterViewInit(): Promise<void> {
    await this.initModeler();
  }

  ngOnDestroy(): void {
    if (this.modeler) {
      this.modeler.destroy();
    }
  }

  // ─── Inizializzazione bpmn-js Modeler ────────────────────────────────────

  private async initModeler(): Promise<void> {
    try {
      const BpmnModeler = (await import('bpmn-js/lib/Modeler')).default;

      this.modeler = new BpmnModeler({
        container: this.canvasRef.nativeElement
      });

      const xmlDaCarica = this.definizione?.bpmnXml ?? DEFAULT_BPMN_XML;
      await this.modeler.importXML(xmlDaCarica);

      this.modeler.get('canvas').zoom('fit-viewport');
      this.modelerReady.set(true);

      // ── Listener selezione elementi ─────────────────────────────────────
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      this.modeler.on('selection.changed', (event: any) => {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const elements: any[] = event.newSelection ?? [];
        if (elements.length === 1) {
          const el = elements[0];
          if (el?.type === 'bpmn:ServiceTask') {
            this.selectedServiceTaskId.set(el.id);
            this.selectedServiceTaskName.set(el.businessObject?.name ?? el.id);
            this.selectedElementType.set('serviceTask');
            this.autoTaskForm.set(this.readAutoTaskFromElement(el));
          } else if (el?.type === 'bpmn:ExclusiveGateway') {
            this.selectedServiceTaskId.set(el.id);
            this.selectedServiceTaskName.set(el.businessObject?.name ?? 'Gateway XOR');
            this.selectedElementType.set('xorGateway');
          } else if (el?.type === 'bpmn:ParallelGateway') {
            this.selectedServiceTaskId.set(el.id);
            this.selectedServiceTaskName.set(el.businessObject?.name ?? 'Gateway AND');
            this.selectedElementType.set('andGateway');
          } else if (el?.type === 'bpmn:CallActivity') {
            this.selectedServiceTaskId.set(el.id);
            this.selectedServiceTaskName.set(el.businessObject?.name ?? 'Sub-processo');
            this.selectedElementType.set('callActivity');
            this.callActivityElement.set(el.businessObject?.calledElement ?? '');
          } else {
            this.selectedServiceTaskId.set(null);
            this.selectedElementType.set(null);
          }
        } else {
          this.selectedServiceTaskId.set(null);
          this.selectedElementType.set(null);
        }
      });

    } catch (err: unknown) {
      console.error('Errore inizializzazione modeler:', err);
      this.errore.set('Impossibile caricare l\'editor BPMN. Verificare la console.');
    }
  }

  // ─── Lettura extensionElements → AutoTaskForm ─────────────────────────────

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private readAutoTaskFromElement(el: any): AutoTaskForm {
    try {
      const extValues = el.businessObject?.extensionElements?.values ?? [];
      // Trova il primo elemento Documentation con contenuto JSON
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const doc = extValues.find((v: any) =>
        (v.$type === 'bpmn:Documentation' || v.$localName === 'documentation') && v.text
      );
      if (doc?.text) {
        const raw = doc.text.trim();
        if (raw.startsWith('{')) {
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          const cfg: any = JSON.parse(raw);
          // Supporta sia il vecchio formato (_type) che il nuovo (tipo)
          const tipo: AutoTaskTipo =
            cfg.tipo ?? (cfg._type === 'aia:serviceCall' ? 'API_CALL' : 'API_CALL');
          return {
            tipo,
            url:          cfg.url           ?? '',
            method:       cfg.method         ?? 'POST',
            body:         cfg.body           ?? '',
            headers:      cfg.headers        ?? '',
            webhookSecret:cfg.webhookSecret  ?? '',
            emailTo:      cfg.emailTo        ?? '',
            emailSubject: cfg.emailSubject   ?? '',
            emailBody:    cfg.emailBody      ?? '',
            delay:        cfg.delay          ?? '1h',
            script:       cfg.params         ?? '',
            template:     cfg.template       ?? '',
          };
        }
      }
    } catch { /* nessuna config salvata */ }
    return { ...DEFAULT_FORM };
  }

  // ─── Scrittura AutoTaskForm → extensionElements ───────────────────────────

  applicaAutoTask(): void {
    const id   = this.selectedServiceTaskId();
    const form = this.autoTaskForm();
    if (!id || !this.modeler) return;

    try {
      const moddle     = this.modeler.get('moddle');
      const modeling   = this.modeler.get('modeling');
      const elementReg = this.modeler.get('elementRegistry');
      const element    = elementReg.get(id);
      if (!element) return;

      const bo = element.businessObject;

      // Costruisce il payload JSON in base al tipo selezionato
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const cfg: any = { tipo: form.tipo };
      switch (form.tipo) {
        case 'API_CALL':
          cfg.url    = form.url;
          cfg.method = form.method;
          if (form.body)    cfg.body    = form.body;
          if (form.headers) cfg.headers = form.headers;
          break;
        case 'WEBHOOK':
          cfg.url    = form.url;
          cfg.method = form.method;
          if (form.body)          cfg.body          = form.body;
          if (form.headers)       cfg.headers       = form.headers;
          if (form.webhookSecret) cfg.webhookSecret = form.webhookSecret;
          break;
        case 'EMAIL':
          cfg.emailTo      = form.emailTo;
          cfg.emailSubject = form.emailSubject;
          if (form.emailBody) cfg.emailBody = form.emailBody;
          break;
        case 'TIMER':
          cfg.delay = form.delay;
          break;
        case 'SCRIPT':
          cfg.action = 'GROOVY';
          cfg.params = form.script;
          break;
        case 'GENERA_PDF':
          if (form.template) cfg.template = form.template;
          break;
      }

      // Crea extensionElements con Documentation JSON
      const extEl = moddle.create('bpmn:ExtensionElements', { values: [] });
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const raw   = (moddle as any).create('bpmn:Documentation');
      raw.text    = JSON.stringify(cfg);
      extEl.values.push(raw);

      modeling.updateProperties(element, { extensionElements: extEl });

      // Imposta nome di default se il task non ne ha uno
      if (!bo.name) {
        const defaultNames: Record<AutoTaskTipo, string> = {
          API_CALL:   'Chiamata API',
          WEBHOOK:    'Webhook',
          EMAIL:      'Invia Email',
          TIMER:      'Attendi',
          SCRIPT:     'Script',
          GENERA_PDF: 'Genera PDF',
        };
        const n = defaultNames[form.tipo];
        modeling.updateProperties(element, { name: n });
        this.selectedServiceTaskName.set(n);
      }

      this.successo.set('Configurazione salvata nel diagramma ✓');
      setTimeout(() => this.successo.set(null), 2500);
    } catch (e) {
      console.error('Errore applicazione auto-task:', e);
      this.errore.set('Errore nell\'applicazione della configurazione');
    }
  }

  // ─── Update helpers ───────────────────────────────────────────────────────

  updateField<K extends keyof AutoTaskForm>(field: K, value: AutoTaskForm[K]): void {
    this.autoTaskForm.update(f => ({ ...f, [field]: value }));
  }

  setTipo(tipo: AutoTaskTipo): void {
    this.autoTaskForm.update(f => ({ ...f, tipo }));
  }

  /** Applica il calledElement a un CallActivity element */
  applicaCallActivity(): void {
    const id = this.selectedServiceTaskId();
    if (!id || !this.modeler) return;
    try {
      const modeling   = this.modeler.get('modeling');
      const elementReg = this.modeler.get('elementRegistry');
      const element    = elementReg.get(id);
      if (!element) return;
      modeling.updateProperties(element, { calledElement: this.callActivityElement() });
      this.successo.set('Sub-processo configurato ✓');
      setTimeout(() => this.successo.set(null), 2500);
    } catch (e) {
      console.error('Errore applicazione callActivity:', e);
    }
  }

  // ─── Anteprima step ───────────────────────────────────────────────────────

  async anteprimaStep(): Promise<void> {
    if (!this.modeler) return;
    this.loading.set(true);
    this.errore.set(null);
    try {
      const { xml } = await this.modeler.saveXML({ format: true });
      this.api.previewBpmnXml(xml)
        .pipe(takeUntilDestroyed(this.dref))
        .subscribe({
          next: steps => {
            this.stepsPreview.set(steps);
            this.showPreview.set(true);
            this.loading.set(false);
          },
          error: err => {
            this.errore.set(err?.error?.message ?? 'Errore nel parsing del BPMN');
            this.loading.set(false);
          }
        });
    } catch {
      this.errore.set('Errore nel recupero XML dal modeler');
      this.loading.set(false);
    }
  }

  // ─── Salvataggio ─────────────────────────────────────────────────────────

  async salva(): Promise<void> {
    if (!this.modeler) return;
    if (!this.nome().trim()) {
      this.errore.set('Il nome del flusso è obbligatorio');
      return;
    }
    this.loading.set(true);
    this.errore.set(null);

    try {
      const { xml } = await this.modeler.saveXML({ format: true });

      const req: SaveDefinizioneFlussoRequest = {
        nome:        this.nome().trim(),
        descrizione: this.descrizione() || undefined,
        bpmnXml:     xml
      };

      const obs$ = this.definizione?.id
        ? this.api.aggiornaDefinizioneFlusso(this.definizione.id, req)
        : this.api.creaDefinizioneFlusso(req);

      obs$.pipe(takeUntilDestroyed(this.dref)).subscribe({
        next: salvata => {
          this.loading.set(false);
          this.successo.set('Flusso salvato con successo!');
          setTimeout(() => this.salvato.emit(salvata), 800);
        },
        error: err => {
          this.errore.set(err?.error?.message ?? 'Errore nel salvataggio');
          this.loading.set(false);
        }
      });
    } catch {
      this.errore.set('Errore nel recupero XML dal modeler');
      this.loading.set(false);
    }
  }

  // ─── Helpers template ────────────────────────────────────────────────────

  tipoIcon(tipo: string): string {
    if (tipo.startsWith('SERVICE_TASK')) return '⚙️';
    return '👤';
  }

  tipoIconForTask(tipo: AutoTaskTipo): string {
    return this.TASK_TYPES.find(t => t.value === tipo)?.icon ?? '⚙️';
  }

  annulla(): void {
    this.annullato.emit();
  }

  /** Validazione form prima di applicare */
  get formValida(): boolean {
    const f = this.autoTaskForm();
    switch (f.tipo) {
      case 'API_CALL':
      case 'WEBHOOK':    return !!f.url.trim();
      case 'EMAIL':      return !!f.emailTo.trim() && !!f.emailSubject.trim();
      case 'TIMER':      return !!f.delay.trim();
      case 'SCRIPT':     return !!f.script.trim();
      case 'GENERA_PDF': return true;
    }
  }
}
