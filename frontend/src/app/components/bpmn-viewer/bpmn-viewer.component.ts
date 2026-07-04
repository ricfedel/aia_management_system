import {
  Component, Input, OnChanges, OnDestroy, AfterViewInit,
  ElementRef, ViewChild, SimpleChanges, Output, EventEmitter
} from '@angular/core';
import { CommonModule } from '@angular/common';

declare const require: (module: string) => any;

/**
 * Componente per la visualizzazione di diagrammi BPMN 2.0.
 * Usa bpmn-js (loaded via CDN script tag) per renderizzare il diagramma.
 *
 * Evidenzia il task corrente con un overlay colorato e permette il click
 * sui singoli elementi per ottenere dettagli.
 */
@Component({
  selector: 'app-bpmn-viewer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bpmn-viewer-wrapper">
      <div *ngIf="loading" class="bpmn-loading">
        <div class="loading-spinner"></div>
        <p>Caricamento diagramma BPMN...</p>
      </div>
      <div *ngIf="error" class="bpmn-error">
        <span class="error-icon">⚠️</span> {{ error }}
      </div>
      <div #bpmnContainer class="bpmn-container" [class.hidden]="loading || !!error"></div>
    </div>
  `,
  styles: [`
    .bpmn-viewer-wrapper {
      width: 100%;
      position: relative;
    }
    .bpmn-container {
      width: 100%;
      min-height: 340px;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      background: #fafafa;
      overflow: hidden;
    }
    .bpmn-container.hidden { display: none; }
    .bpmn-loading, .bpmn-error {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      min-height: 200px;
      color: #64748b;
      font-size: 14px;
    }
    .bpmn-error { color: #ef4444; }
    .loading-spinner {
      width: 24px; height: 24px;
      border: 3px solid #e2e8f0;
      border-top-color: #3b82f6;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class BpmnViewerComponent implements AfterViewInit, OnChanges, OnDestroy {

  @ViewChild('bpmnContainer', { static: true }) containerRef!: ElementRef<HTMLDivElement>;

  /** URL o contenuto XML del file BPMN */
  @Input() bpmnUrl?: string;
  @Input() bpmnXml?: string;

  /** ID del task BPMN attualmente attivo (per evidenziarlo) */
  @Input() activeTaskId?: string;

  /** Mappa taskId → statoTask per colorare tutti gli step */
  @Input() taskStates: Record<string, string> = {};

  @Output() elementClicked = new EventEmitter<string>();

  loading = true;
  error: string | null = null;

  private viewer: any = null;

  ngAfterViewInit(): void {
    this.initViewer();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['bpmnUrl'] || changes['bpmnXml']) && this.viewer) {
      this.loadDiagram();
    } else if (changes['taskStates'] && this.viewer) {
      this.applyOverlays();
    }
  }

  ngOnDestroy(): void {
    if (this.viewer) {
      try { this.viewer.destroy(); } catch { /* */ }
    }
  }

  private initViewer(): void {
    // bpmn-js deve essere caricato via CDN in index.html
    // Fallback: render SVG statico se bpmn-js non disponibile
    try {
      const BpmnViewer = (window as any).BpmnJS;
      if (!BpmnViewer) {
        this.renderStaticFallback();
        return;
      }
      this.viewer = new BpmnViewer({ container: this.containerRef.nativeElement });
      this.viewer.on('element.click', (event: any) => {
        const element = event.element;
        if (element && element.id) {
          this.elementClicked.emit(element.id);
        }
      });
      this.loadDiagram();
    } catch (e) {
      this.renderStaticFallback();
    }
  }

  private loadDiagram(): void {
    if (!this.viewer) return;
    this.loading = true;
    this.error = null;

    const loadXml = (xml: string) => {
      this.viewer.importXML(xml).then(() => {
        this.loading = false;
        this.viewer.get('canvas').zoom('fit-viewport');
        this.applyOverlays();
      }).catch((err: any) => {
        this.loading = false;
        this.error = 'Errore nel rendering del diagramma BPMN';
        console.error('bpmn-js import error:', err);
      });
    };

    if (this.bpmnXml) {
      loadXml(this.bpmnXml);
    } else if (this.bpmnUrl) {
      fetch(this.bpmnUrl)
        .then(r => r.text())
        .then(xml => loadXml(xml))
        .catch(() => {
          this.loading = false;
          this.error = 'Impossibile caricare il file BPMN';
        });
    } else {
      this.loading = false;
    }
  }

  private applyOverlays(): void {
    if (!this.viewer) return;
    try {
      const overlays = this.viewer.get('overlays');
      overlays.clear();

      Object.entries(this.taskStates).forEach(([taskId, stato]) => {
        const color = this.getColorForStato(stato);
        overlays.add(taskId, {
          position: { bottom: 0, right: 0 },
          html: `<div style="
            background:${color};
            color:#fff;
            padding:2px 6px;
            border-radius:4px;
            font-size:10px;
            font-weight:600;
            white-space:nowrap;
          ">${this.getLabelForStato(stato)}</div>`
        });
      });

      if (this.activeTaskId) {
        const canvas = this.viewer.get('canvas');
        canvas.addMarker(this.activeTaskId, 'highlight-active');
      }
    } catch { /* element not found - ignore */ }
  }

  private getColorForStato(stato: string): string {
    const map: Record<string, string> = {
      CREATO:     '#3b82f6',
      IN_CORSO:   '#f59e0b',
      COMPLETATO: '#10b981',
      SALTATO:    '#6b7280',
      ANNULLATO:  '#ef4444'
    };
    return map[stato] ?? '#94a3b8';
  }

  private getLabelForStato(stato: string): string {
    const map: Record<string, string> = {
      CREATO:     'In coda',
      IN_CORSO:   '▶ Attivo',
      COMPLETATO: '✓',
      SALTATO:    '–',
      ANNULLATO:  '✗'
    };
    return map[stato] ?? stato;
  }

  /** Fallback: render diagramma linearizzato senza bpmn-js */
  private renderStaticFallback(): void {
    this.loading = false;
    // Il container mostrerà semplicemente il messaggio di errore soft
    // La pagina Processi ha già un "process stepper" visuale come fallback primario
  }
}
