import {
  Component, inject, signal, computed, Input, Output,
  EventEmitter, DestroyRef, OnInit
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Stabilimento } from '../../models/stabilimento.model';
import {
  PropostaEstrazione, ScadenzaProposta, PrescrizioneProposta,
  ConfermaEstrazioneRequest, ConfermaEstrazioneResponse,
  METODO_LABELS, TIPO_SCADENZA_OPTIONS, TIPO_PRESCRIZIONE_OPTIONS
} from '../../models/estrazione.model';

type Fase = 'analisi' | 'revisione' | 'completato';

@Component({
  selector: 'app-estrazione-dms',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './estrazione-dms.component.html',
  styleUrl: './estrazione-dms.component.css'
})
export class EstrazioneDmsComponent implements OnInit {

  /** ID del documento da analizzare */
  @Input({ required: true }) documentoId!: number;
  @Input() documentoNome = '';
  @Input() stabilimentoIdDefault?: number;

  @Output() chiudi   = new EventEmitter<void>();
  @Output() confermato = new EventEmitter<ConfermaEstrazioneResponse>();

  private readonly api  = inject(ApiService);
  private readonly dref = inject(DestroyRef);

  // ─── State ───────────────────────────────────────────────────────────────
  fase           = signal<Fase>('analisi');
  loading        = signal(false);
  errore         = signal<string | null>(null);
  proposta       = signal<PropostaEstrazione | null>(null);
  risultato      = signal<ConfermaEstrazioneResponse | null>(null);
  stabilimenti   = signal<Stabilimento[]>([]);
  testoAperto    = signal(false);
  noteRevisione  = signal('');

  // Copie editabili della proposta
  metadati = signal<PropostaEstrazione['metadati']>({});
  scadenze = signal<ScadenzaProposta[]>([]);
  prescrizioni = signal<PrescrizioneProposta[]>([]);

  // ─── Constants ───────────────────────────────────────────────────────────
  readonly metodoLabels = METODO_LABELS;
  readonly tipiScadenza = TIPO_SCADENZA_OPTIONS;
  readonly tipiPrescrizione = TIPO_PRESCRIZIONE_OPTIONS;

  // ─── Computed ────────────────────────────────────────────────────────────
  scadenzeSelezionate  = computed(() => this.scadenze().filter(s => s.selezionata).length);
  prescrizioniSelezionate = computed(() => this.prescrizioni().filter(p => p.selezionata).length);

  confidenzaColore = computed(() => {
    const c = this.proposta()?.confidenza ?? 0;
    if (c >= 0.75) return '#10b981';
    if (c >= 0.45) return '#f59e0b';
    return '#ef4444';
  });

  ngOnInit() {
    this.api.getStabilimenti()
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({ next: s => this.stabilimenti.set(s) });
  }

  // ─── Fase 1: Analisi ─────────────────────────────────────────────────────

  avviaAnalisi() {
    this.loading.set(true);
    this.errore.set(null);

    this.api.analizzaDocumento(this.documentoId)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: (p) => {
          this.proposta.set(p);
          // Inizializza copie editabili
          this.metadati.set({ ...p.metadati });
          // Imposta stabilimentoId di default nelle entità proposte
          const stabId = this.stabilimentoIdDefault;
          this.scadenze.set(p.scadenzeProposte.map(s => ({ ...s, stabilimentoId: stabId })));
          this.prescrizioni.set(p.prescrizioniProposte.map(pr => ({ ...pr, stabilimentoId: stabId })));
          this.loading.set(false);
          this.fase.set('revisione');
        },
        error: (err) => {
          this.errore.set(err?.error?.message ?? 'Errore durante l\'analisi del documento');
          this.loading.set(false);
        }
      });
  }

  // ─── Fase 2: Revisione ───────────────────────────────────────────────────

  toggleScadenza(idx: number) {
    this.scadenze.update(list =>
      list.map((s, i) => i === idx ? { ...s, selezionata: !s.selezionata } : s)
    );
  }

  updateScadenza(idx: number, field: keyof ScadenzaProposta, value: any) {
    this.scadenze.update(list =>
      list.map((s, i) => i === idx ? { ...s, [field]: value } : s)
    );
  }

  togglePrescrizione(idx: number) {
    this.prescrizioni.update(list =>
      list.map((p, i) => i === idx ? { ...p, selezionata: !p.selezionata } : p)
    );
  }

  updatePrescrizione(idx: number, field: keyof PrescrizioneProposta, value: any) {
    this.prescrizioni.update(list =>
      list.map((p, i) => i === idx ? { ...p, [field]: value } : p)
    );
  }

  updateMetadati(field: keyof PropostaEstrazione['metadati'], value: string) {
    this.metadati.update(m => ({ ...m, [field]: value }));
  }

  aggiungiScadenza() {
    const nuova: ScadenzaProposta = {
      tempId: 'SC-MANUALE-' + Date.now(),
      titolo: '',
      dataScadenza: new Date().toISOString().split('T')[0],
      tipo: 'COMUNICAZIONE',
      selezionata: true,
      stabilimentoId: this.stabilimentoIdDefault
    };
    this.scadenze.update(list => [...list, nuova]);
  }

  rimuoviScadenza(idx: number) {
    this.scadenze.update(list => list.filter((_, i) => i !== idx));
  }

  aggiungiPrescrizione() {
    const nuova: PrescrizioneProposta = {
      tempId: 'PR-MANUALE-' + Date.now(),
      codice: '',
      descrizione: '',
      tipo: 'ALTRO',
      selezionata: true,
      stabilimentoId: this.stabilimentoIdDefault
    };
    this.prescrizioni.update(list => [...list, nuova]);
  }

  rimuoviPrescrizione(idx: number) {
    this.prescrizioni.update(list => list.filter((_, i) => i !== idx));
  }

  selezionaTutteScadenze(val: boolean) {
    this.scadenze.update(list => list.map(s => ({ ...s, selezionata: val })));
  }

  selezionaTuttePrescrizioni(val: boolean) {
    this.prescrizioni.update(list => list.map(p => ({ ...p, selezionata: val })));
  }

  // ─── Fase 3: Conferma ────────────────────────────────────────────────────

  conferma() {
    const req: ConfermaEstrazioneRequest = {
      documentoId: this.documentoId,
      metadati: this.metadati(),
      scadenze: this.scadenze()
        .filter(s => s.selezionata && s.titolo && s.dataScadenza)
        .map(s => ({
          titolo: s.titolo,
          descrizione: s.descrizione,
          dataScadenza: s.dataScadenza,
          tipo: s.tipo,
          stabilimentoId: s.stabilimentoId ?? this.stabilimentoIdDefault
        })),
      prescrizioni: this.prescrizioni()
        .filter(p => p.selezionata && p.codice)
        .map(p => ({
          codice: p.codice,
          descrizione: p.descrizione,
          tipo: p.tipo,
          stabilimentoId: p.stabilimentoId ?? this.stabilimentoIdDefault
        })),
      noteRevisione: this.noteRevisione() || undefined
    };

    this.loading.set(true);
    this.errore.set(null);

    this.api.confermaEstrazione(this.documentoId, req)
      .pipe(takeUntilDestroyed(this.dref))
      .subscribe({
        next: (res) => {
          this.risultato.set(res);
          this.loading.set(false);
          this.fase.set('completato');
          this.confermato.emit(res);
        },
        error: (err) => {
          this.errore.set(err?.error?.message ?? 'Errore durante la conferma');
          this.loading.set(false);
        }
      });
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  getConfidenzaLabel(c?: number): string {
    if (!c) return '—';
    const pct = Math.round(c * 100);
    if (pct >= 75) return `Alta (${pct}%)`;
    if (pct >= 45) return `Media (${pct}%)`;
    return `Bassa (${pct}%)`;
  }

  getNomeStabilimento(id?: number): string {
    if (!id) return '';
    return this.stabilimenti().find(s => s.id === id)?.nome ?? '';
  }

  formatDataIt(iso?: string): string {
    if (!iso) return '—';
    try {
      const d = new Date(iso + 'T12:00:00');
      return d.toLocaleDateString('it-IT');
    } catch { return iso; }
  }

  toggleTestoAperto(): void {
    this.testoAperto.update(v => !v);
  }
}
