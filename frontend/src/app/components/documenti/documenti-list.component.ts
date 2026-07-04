import { Component, inject, signal, computed, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Documento, TipoDocumento, StatoDocumento, STATO_DOCUMENTO_LABELS, STATO_DOCUMENTO_COLORS } from '../../models/documento.model';
import { Stabilimento } from '../../models/stabilimento.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { EnumTranslatePipe } from '../../pipes/enum-translate.pipe';
import { EstrazioneDmsComponent } from '../estrazione-dms/estrazione-dms.component';
import { ConfermaEstrazioneResponse } from '../../models/estrazione.model';

@Component({
  selector: 'app-documenti-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, EnumTranslatePipe, EstrazioneDmsComponent],
  templateUrl: './documenti-list.component.html',
  styleUrl: './documenti-list.component.css'
})
export class DocumentiListComponent implements OnInit {
  private readonly apiService  = inject(ApiService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef  = inject(DestroyRef);

  // ── Dati ────────────────────────────────────────────────────────────────
  documenti    = signal<Documento[]>([]);
  stabilimenti = signal<Stabilimento[]>([]);
  loading      = signal(true);

  // ── Filtri ───────────────────────────────────────────────────────────────
  filterStabilimentoId = signal<number | null>(null);
  filterAnno           = signal<number | null>(null);
  filterTipo           = signal<TipoDocumento | null>(null);
  filterKeyword        = signal<string>('');

  // ── Upload ──────────────────────────────────────────────────────────────
  showUploadForm  = signal(false);
  uploading       = signal(false);
  uploadProgress  = signal(0);
  selectedFile    = signal<File | null>(null);

  uploadData = signal({
    stabilimentoId: 0,
    anno: new Date().getFullYear(),
    tipo: '' as TipoDocumento,
    descrizione: ''
  });

  // Costante per tipi documento
  readonly tipiDocumento = Object.values(TipoDocumento);

  // Anno corrente e range per il filtro
  readonly annoCorrente = new Date().getFullYear();
  readonly anniDisponibili = Array.from(
    { length: this.annoCorrente - 2018 },
    (_, i) => this.annoCorrente - i
  );

  // ── Estrazione OCR / AI ─────────────────────────────────────────────────
  documentoInEstrazione = signal<Documento | null>(null);

  apriEstrazione(documento: Documento) { this.documentoInEstrazione.set(documento); }
  chiudiEstrazione()                   { this.documentoInEstrazione.set(null); }

  onEstrazioneConfermata(response: ConfermaEstrazioneResponse) {
    this.documentoInEstrazione.set(null);
    this.loadDocumenti();
    if (response.scadenzeCreate > 0 || response.prescrizioniCreate > 0) {
      alert(`✅ ${response.messaggio}`);
    }
  }

  // ── Helpers stato DMS ───────────────────────────────────────────────────
  getStatoLabel(stato: StatoDocumento): string { return STATO_DOCUMENTO_LABELS[stato] ?? stato; }
  getStatoColore(stato: StatoDocumento): string { return STATO_DOCUMENTO_COLORS[stato] ?? '#94a3b8'; }

  // ── Permissions ─────────────────────────────────────────────────────────
  canUpload = computed(() => {
    const user = this.authService.currentUserValue;
    return user?.ruolo === 'ADMIN' || user?.ruolo === 'RESPONSABILE' || user?.ruolo === 'OPERATORE';
  });

  canDelete = computed(() => {
    const user = this.authService.currentUserValue;
    return user?.ruolo === 'ADMIN' || user?.ruolo === 'RESPONSABILE';
  });

  ngOnInit() {
    this.loadStabilimenti();
    this.loadDocumenti();
  }

  // ── Caricamento dati ────────────────────────────────────────────────────

  loadDocumenti() {
    this.loading.set(true);

    const params: Record<string, any> = {};
    if (this.filterStabilimentoId()) params['stabilimentoId'] = this.filterStabilimentoId();
    if (this.filterAnno())           params['anno']           = this.filterAnno();
    if (this.filterTipo())           params['tipoDocumento']  = this.filterTipo();
    if (this.filterKeyword().trim()) params['keyword']        = this.filterKeyword().trim();

    this.apiService.searchDocumenti(params)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.documenti.set(result.content ?? []);
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Errore caricamento documenti:', error);
          this.documenti.set([]);
          this.loading.set(false);
        }
      });
  }

  loadStabilimenti() {
    this.apiService.getStabilimenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => this.stabilimenti.set(data),
        error: (error) => console.error('Errore caricamento stabilimenti:', error)
      });
  }

  // ── Filtri ───────────────────────────────────────────────────────────────

  onFilterStabilimento(value: string) {
    this.filterStabilimentoId.set(value ? +value : null);
    this.loadDocumenti();
  }

  onFilterAnno(value: string) {
    this.filterAnno.set(value ? +value : null);
    this.loadDocumenti();
  }

  onFilterTipo(value: string) {
    this.filterTipo.set(value ? value as TipoDocumento : null);
    this.loadDocumenti();
  }

  onFilterKeyword(value: string) {
    this.filterKeyword.set(value);
    // Debounce: ricarica solo dopo 400ms di inattività
    if (this._keywordTimer) clearTimeout(this._keywordTimer);
    this._keywordTimer = setTimeout(() => this.loadDocumenti(), 400);
  }

  private _keywordTimer: any;

  resetFiltri() {
    this.filterStabilimentoId.set(null);
    this.filterAnno.set(null);
    this.filterTipo.set(null);
    this.filterKeyword.set('');
    this.loadDocumenti();
  }

  get filtriAttivi(): boolean {
    return !!(
      this.filterStabilimentoId() ||
      this.filterAnno() ||
      this.filterTipo() ||
      this.filterKeyword().trim()
    );
  }

  // ── Upload ───────────────────────────────────────────────────────────────

  openUploadForm() {
    this.showUploadForm.set(true);
    const stabs = this.stabilimenti();
    this.uploadData.set({
      stabilimentoId: stabs.length > 0 ? stabs[0].id! : 0,
      anno: new Date().getFullYear(),
      tipo: TipoDocumento.ALTRO,
      descrizione: ''
    });
    this.selectedFile.set(null);
  }

  closeUploadForm() {
    this.showUploadForm.set(false);
    this.selectedFile.set(null);
    this.uploadProgress.set(0);
  }

  onFileSelected(event: Event) {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      this.selectedFile.set(target.files[0]);
    }
  }

  uploadDocumento() {
    const file = this.selectedFile();
    if (!file) { alert('Seleziona un file'); return; }

    const data = this.uploadData();
    if (!data.tipo) { alert('Seleziona il tipo di documento'); return; }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('stabilimentoId', data.stabilimentoId.toString());
    formData.append('anno', data.anno.toString());
    formData.append('tipoDocumento', data.tipo);
    if (data.descrizione) formData.append('descrizione', data.descrizione);

    this.uploading.set(true);
    this.uploadProgress.set(50);

    this.apiService.uploadDocumento(formData)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.uploadProgress.set(100);
          this.uploading.set(false);
          this.loadDocumenti();
          this.closeUploadForm();
        },
        error: (error) => {
          console.error('Errore upload:', error);
          alert('Errore durante upload documento');
          this.uploading.set(false);
          this.uploadProgress.set(0);
        }
      });
  }

  downloadDocumento(documento: Documento) {
    this.apiService.downloadDocumento(documento.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = documento.nomeFile;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (error) => {
          console.error('Errore download:', error);
          alert('Errore durante download documento');
        }
      });
  }

  deleteDocumento(documento: Documento) {
    if (!confirm(`Sei sicuro di voler eliminare il documento "${documento.nome}"?`)) return;

    this.apiService.deleteDocumento(documento.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDocumenti(),
        error: (error) => {
          console.error('Errore eliminazione:', error);
          alert('Errore durante eliminazione documento');
        }
      });
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  // Helper per update uploadData
  updateStabilimentoId(id: number) { this.uploadData.update(d => ({...d, stabilimentoId: id})); }
  updateAnno(anno: number)          { this.uploadData.update(d => ({...d, anno: anno})); }
  updateTipo(tipo: TipoDocumento)   { this.uploadData.update(d => ({...d, tipo: tipo})); }
  updateDescrizione(desc: string)   { this.uploadData.update(d => ({...d, descrizione: desc})); }
}
