import { Component, inject, signal, computed, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, ImportScadenzeResult, RigaImport } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Scadenza, TipoScadenza, StatoScadenza, Priorita } from '../../models/scadenza.model';
import { Stabilimento } from '../../models/stabilimento.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { EnumTranslatePipe } from '../../pipes/enum-translate.pipe';

interface ScadenzaForm extends Partial<Scadenza> {
  stabilimentoId?: number;
}

/** Cella del calendario: una giornata con le sue scadenze */
export interface CalendarDay {
  date: Date;
  dayNum: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  scadenze: Scadenza[];
}

@Component({
  selector: 'app-scadenze-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, EnumTranslatePipe],
  templateUrl: './scadenze-list.component.html',
  styleUrl: './scadenze-list.component.css'
})
export class ScadenzeListComponent implements OnInit {
  // Dependency injection
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  // Signals
  scadenze = signal<Scadenza[]>([]);
  stabilimenti = signal<Stabilimento[]>([]);
  loading = signal(true);
  showForm = signal(false);
  editMode = signal(false);

  // Form data
  formData = signal<ScadenzaForm>({
    titolo: '',
    descrizione: '',
    stabilimentoId: 0,
    dataScadenza: '',
    tipoScadenza: TipoScadenza.COMUNICAZIONE,
    stato: StatoScadenza.PENDING,
    priorita: Priorita.MEDIA,
    responsabile: '',
    emailNotifica: '',
    giorniPreavviso: 0,
    note: ''
  });

  // Computed permissions
  canEdit = computed(() => {
    const user = this.authService.currentUserValue;
    return user?.ruolo === 'ADMIN' || user?.ruolo === 'RESPONSABILE';
  });

  canDelete = computed(() => {
    const user = this.authService.currentUserValue;
    return user?.ruolo === 'ADMIN';
  });

  // Enum options
  tipiScadenza = ['MONITORAGGIO_PMC', 'RELAZIONE_ANNUALE', 'INTEGRAZIONE_ENTE', 'RINNOVO_AIA', 'COMUNICAZIONE', 'ALTRO'];
  stati = ['PENDING', 'IN_CORSO', 'COMPLETATA', 'SCADUTA'];
  prioritaOptions = ['BASSA', 'MEDIA', 'ALTA', 'URGENTE'];

  // ── Vista Lista / Calendario ─────────────────────────────────────────
  activeView = signal<'lista' | 'calendario'>('lista');

  // Calendario – navigazione
  private today = new Date();
  calYear  = signal(this.today.getFullYear());
  calMonth = signal(this.today.getMonth()); // 0-based

  // Filtro per stabilimento nel calendario
  calFilterStabilimento = signal<number | null>(null);

  // Giorno selezionato per il day-detail popup
  selectedDay = signal<CalendarDay | null>(null);

  /** Intestazioni giorni della settimana (Lun–Dom) */
  readonly weekDays = ['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom'];

  /** Nome mese corrente */
  calMonthLabel = computed(() => {
    const d = new Date(this.calYear(), this.calMonth(), 1);
    return d.toLocaleDateString('it-IT', { month: 'long', year: 'numeric' });
  });

  /** Griglia 42 celle (6 settimane × 7 giorni), da Lunedì */
  calendarDays = computed<CalendarDay[]>(() => {
    const year  = this.calYear();
    const month = this.calMonth();
    const filterSt = this.calFilterStabilimento();
    const allScadenze = this.scadenze();

    // Scadenze filtrate per stabilimento (se selezionato)
    const filtered = filterSt
      ? allScadenze.filter(s => s.stabilimento?.id === filterSt || (s as any).stabilimentoId === filterSt)
      : allScadenze;

    // Prima cella: Lunedì della settimana che contiene il 1° del mese
    const firstDay = new Date(year, month, 1);
    let startDow = firstDay.getDay(); // 0=Dom
    // Converti a Lunedì=0
    startDow = (startDow + 6) % 7;
    const start = new Date(year, month, 1 - startDow);

    const days: CalendarDay[] = [];
    for (let i = 0; i < 42; i++) {
      const d = new Date(start);
      d.setDate(start.getDate() + i);

      const isoDate = this.toIso(d);
      const dayScadenze = filtered.filter(s => s.dataScadenza === isoDate);

      const todayIso = this.toIso(this.today);
      days.push({
        date: d,
        dayNum: d.getDate(),
        isCurrentMonth: d.getMonth() === month,
        isToday: isoDate === todayIso,
        scadenze: dayScadenze
      });
    }
    return days;
  });

  /** Mappa tipo → classe CSS colore chip */
  readonly tipoColorMap: Record<string, string> = {
    MONITORAGGIO_PMC:  'chip-pmc',
    RELAZIONE_ANNUALE: 'chip-relazione',
    RINNOVO_AIA:       'chip-rinnovo',
    COMUNICAZIONE:     'chip-comunicazione',
    INTEGRAZIONE_ENTE: 'chip-integrazione',
    ALTRO:             'chip-altro',
  };

  // ── Import da Excel ──────────────────────────────────────────────────
  showImportModal  = signal(false);
  importStep       = signal<'upload' | 'mapping' | 'preview' | 'done'>('upload');
  importFile       = signal<File | null>(null);
  importLoading    = signal(false);
  importPreview    = signal<ImportScadenzeResult | null>(null);
  importResult     = signal<ImportScadenzeResult | null>(null);
  /** mapping manuale: sito → stabilimentoId  (solo per siti non auto-matchati) */
  importMapping    = signal<Record<string, number>>({});

  ngOnInit() {
    this.loadScadenze();
    this.loadStabilimenti();
  }

  loadScadenze() {
    this.loading.set(true);
    this.apiService.getScadenze()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.scadenze.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Error loading scadenze:', err);
          this.loading.set(false);
        }
      });
  }

  loadStabilimenti() {
    this.apiService.getStabilimenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.stabilimenti.set(data);
        },
        error: (err) => console.error('Error loading stabilimenti:', err)
      });
  }

  openCreateForm() {
    this.editMode.set(false);
    this.formData.set({
      titolo: '',
      descrizione: '',
      stabilimentoId: this.stabilimenti()[0]?.id || 0,
      dataScadenza: '',
      tipoScadenza: TipoScadenza.COMUNICAZIONE,
      stato: StatoScadenza.PENDING,
      priorita: Priorita.MEDIA,
      responsabile: '',
      emailNotifica: '',
      giorniPreavviso: 0,
      note: ''
    });
    this.showForm.set(true);
  }

  openEditForm(scadenza: Scadenza) {
    this.editMode.set(true);
    this.formData.set({
      id: scadenza.id,
      titolo: scadenza.titolo,
      descrizione: scadenza.descrizione || '',
      stabilimentoId: scadenza.stabilimento?.id || 0,
      dataScadenza: scadenza.dataScadenza,
      tipoScadenza: scadenza.tipoScadenza,
      stato: scadenza.stato,
      priorita: scadenza.priorita || Priorita.MEDIA,
      responsabile: scadenza.responsabile || '',
      emailNotifica: scadenza.emailNotifica || '',
      giorniPreavviso: scadenza.giorniPreavviso || 0,
      note: scadenza.note || ''
    });
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.formData.set({});
  }

  saveScadenza() {
    const data = this.formData();

    if (!data.titolo || !data.dataScadenza || !(data as ScadenzaForm).stabilimentoId) {
      alert('Compila tutti i campi obbligatori');
      return;
    }

    const request = this.editMode() && data.id
      ? this.apiService.updateScadenza(data.id, data as Scadenza)
      : this.apiService.createScadenza(data as Scadenza);

    request.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.closeForm();
          this.loadScadenze();
        },
        error: (err) => console.error('Error saving scadenza:', err)
      });
  }

  deleteScadenza(scadenza: Scadenza) {
    if (!confirm(`Eliminare la scadenza ${scadenza.titolo}?`)) {
      return;
    }

    this.apiService.deleteScadenza(scadenza.id!)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadScadenze(),
        error: (err) => console.error('Error deleting scadenza:', err)
      });
  }

  completeScadenza(scadenza: Scadenza) {
    if (!scadenza.id) return;

    const updatedScadenza: Scadenza = { ...scadenza, stato: StatoScadenza.COMPLETATA };
    this.apiService.updateScadenza(scadenza.id, updatedScadenza)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadScadenze(),
        error: (err) => console.error('Error completing scadenza:', err)
      });
  }

  // Helper methods for form updates
  updateTitolo(value: string) {
    this.formData.update(d => ({ ...d, titolo: value }));
  }

  updateDescrizione(value: string) {
    this.formData.update(d => ({ ...d, descrizione: value }));
  }

  updateStabilimentoId(value: number) {
    this.formData.update(d => ({ ...d, stabilimentoId: value }));
  }

  updateDataScadenza(value: string) {
    this.formData.update(d => ({ ...d, dataScadenza: value }));
  }

  updateTipoScadenza(value: string) {
    this.formData.update(d => ({ ...d, tipoScadenza: value as TipoScadenza }));
  }

  updateStato(value: string) {
    this.formData.update(d => ({ ...d, stato: value as StatoScadenza }));
  }

  updatePriorita(value: string) {
    this.formData.update(d => ({ ...d, priorita: value as Priorita }));
  }

  updateResponsabile(value: string) {
    this.formData.update(d => ({ ...d, responsabile: value }));
  }

  updateEmailNotifica(value: string) {
    this.formData.update(d => ({ ...d, emailNotifica: value }));
  }

  updateGiorniPreavviso(value: number) {
    this.formData.update(d => ({ ...d, giorniPreavviso: value }));
  }

  updateNote(value: string) {
    this.formData.update(d => ({ ...d, note: value }));
  }

  getPrioritaClass(priorita: string): string {
    switch (priorita?.toUpperCase()) {
      case 'ALTA':
      case 'URGENTE':
        return 'priority-high';
      case 'MEDIA':
      case 'NORMALE':
        return 'priority-medium';
      case 'BASSA':
        return 'priority-low';
      default:
        return '';
    }
  }

  getStatoClass(stato: string): string {
    switch (stato?.toUpperCase()) {
      case 'PENDING':
      case 'IN_CORSO':
        return 'status-active';
      case 'COMPLETATA':
        return 'status-completed';
      case 'SCADUTA':
        return 'status-suspended';
      default:
        return '';
    }
  }

  isCompletable(stato: string): boolean {
    return stato === 'PENDING' || stato === 'IN_CORSO';
  }

  exportExcel() {
    this.apiService.exportScadenzeExcel()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `scadenze-${new Date().toISOString().split('T')[0]}.xlsx`;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Error exporting scadenze:', err);
          alert('Errore durante l\'esportazione');
        }
      });
  }

  // ── Import da Excel ──────────────────────────────────────────────────

  openImportModal() {
    this.importStep.set('upload');
    this.importFile.set(null);
    this.importPreview.set(null);
    this.importResult.set(null);
    this.importMapping.set({});
    this.showImportModal.set(true);
  }

  closeImportModal() {
    this.showImportModal.set(false);
  }

  onImportFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.importFile.set(input.files[0]);
    }
  }

  /** Invia il file al backend per la preview */
  runPreview() {
    const file = this.importFile();
    if (!file) return;
    this.importLoading.set(true);
    this.apiService.previewImportScadenze(file, this.importMapping())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.importPreview.set(result);
          this.importLoading.set(false);
          // Se ci sono siti non mappati mostra lo step mapping, altrimenti direttamente preview
          const nonMappati = (result.sitiTrovati ?? []).filter(s =>
            !(result.righe ?? []).some(r => r.sito === s && r.stabilimentoId)
          );
          this.importStep.set(nonMappati.length > 0 ? 'mapping' : 'preview');
        },
        error: () => {
          this.importLoading.set(false);
          alert('Errore nel parsing del file. Verifica che sia un file .xlsx valido.');
        }
      });
  }

  /** Aggiorna il mapping sito→stabilimento e ri-esegue la preview */
  applyMappingAndPreview() {
    this.importStep.set('upload'); // reset step per triggerare re-preview
    this.runPreview();
  }

  setMappingForSito(sito: string, stabilimentoId: number) {
    this.importMapping.update(m => ({ ...m, [sito]: +stabilimentoId }));
  }

  toggleRiga(riga: RigaImport) {
    this.importPreview.update(p => {
      if (!p) return p;
      return {
        ...p,
        righe: p.righe!.map(r => r.rigaExcel === riga.rigaExcel ? { ...r, selezionata: !r.selezionata } : r)
      };
    });
  }

  selectAll(val: boolean) {
    this.importPreview.update(p => {
      if (!p) return p;
      return { ...p, righe: p.righe!.map(r => r.errore ? r : { ...r, selezionata: val }) };
    });
  }

  /** Conferma e persiste le righe selezionate */
  confirmImport() {
    const righe = this.importPreview()?.righe ?? [];
    if (!righe.length) return;
    this.importLoading.set(true);
    this.apiService.confirmImportScadenze(righe)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.importResult.set(result);
          this.importLoading.set(false);
          this.importStep.set('done');
          this.loadScadenze(); // ricarica la lista
        },
        error: () => {
          this.importLoading.set(false);
          alert('Errore durante l\'import.');
        }
      });
  }

  get sitiNonMappati(): string[] {
    const preview = this.importPreview();
    if (!preview) return [];
    return (preview.sitiTrovati ?? []).filter(s =>
      !(preview.righe ?? []).some(r => r.sito === s && r.stabilimentoId)
    );
  }

  get righeSelezionate(): number {
    return (this.importPreview()?.righe ?? []).filter(r => r.selezionata && !r.errore).length;
  }

  // ── Calendario – metodi di navigazione ──────────────────────────────

  prevMonth() {
    if (this.calMonth() === 0) {
      this.calMonth.set(11);
      this.calYear.update(y => y - 1);
    } else {
      this.calMonth.update(m => m - 1);
    }
    this.selectedDay.set(null);
  }

  nextMonth() {
    if (this.calMonth() === 11) {
      this.calMonth.set(0);
      this.calYear.update(y => y + 1);
    } else {
      this.calMonth.update(m => m + 1);
    }
    this.selectedDay.set(null);
  }

  goToToday() {
    this.calYear.set(this.today.getFullYear());
    this.calMonth.set(this.today.getMonth());
    this.selectedDay.set(null);
  }

  selectDay(day: CalendarDay) {
    if (day.scadenze.length === 0) {
      this.selectedDay.set(null);
      return;
    }
    this.selectedDay.set(day);
  }

  closeDayDetail() {
    this.selectedDay.set(null);
  }

  chipClass(s: Scadenza): string {
    if (s.stato === 'SCADUTA') return 'chip-scaduta';
    if (s.stato === 'COMPLETATA') return 'chip-completata';
    return this.tipoColorMap[s.tipoScadenza] ?? 'chip-altro';
  }

  /** Numero scadenze per mese (per il mini-strip annuale) */
  monthCount(m: number): number {
    const y = this.calYear();
    return this.scadenze().filter(s => {
      if (!s.dataScadenza) return false;
      const d = new Date(s.dataScadenza);
      return d.getFullYear() === y && d.getMonth() === m;
    }).length;
  }

  private toIso(d: Date): string {
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mm}-${dd}`;
  }
}
