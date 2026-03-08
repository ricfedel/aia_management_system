import { Component, inject, signal, computed, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { DatiAmbientali, StatoConformita } from '../../models/dati-ambientali.model';
import { Stabilimento } from '../../models/stabilimento.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { EnumTranslatePipe } from '../../pipes/enum-translate.pipe';

@Component({
  selector: 'app-dati-ambientali-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, EnumTranslatePipe],
  templateUrl: './dati-ambientali-list.component.html',
  styleUrl: './dati-ambientali-list.component.css'
})
export class DatiAmbientaliListComponent implements OnInit {
  // Dependency injection
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  // Signals
  datiAmbientali = signal<DatiAmbientali[]>([]);
  stabilimenti = signal<Stabilimento[]>([]);
  loading = signal(true);
  showForm = signal(false);
  editMode = signal(false);

  // Form data
  formData = signal<Partial<DatiAmbientali>>({
    dataCampionamento: '',
    parametro: '',
    valoreMisurato: undefined,
    unitaMisura: '',
    limiteAutorizzato: undefined,
    statoConformita: StatoConformita.CONFORME,
    laboratorio: '',
    note: ''
  });

  // Computed permissions
  canEdit = computed(() => {
    const user = this.authService.currentUserValue;
    return user?.ruolo === 'ADMIN' || user?.ruolo === 'RESPONSABILE' || user?.ruolo === 'OPERATORE';
  });

  canDelete = computed(() => {
    const user = this.authService.currentUserValue;
    return user?.ruolo === 'ADMIN' || user?.ruolo === 'RESPONSABILE';
  });

  // Enum options
  matriciAmbientali = ['ARIA', 'ACQUA', 'SUOLO', 'RIFIUTI', 'RUMORE', 'ENERGIA', 'EMISSIONI', 'SCARICHI'];
  statiConformita = ['CONFORME', 'NON_CONFORME', 'DA_VERIFICARE', 'SUPERATO'];
  metodi = ['ANALITICO', 'STRUMENTALE', 'LABORATORIO', 'IN_SITU'];

  ngOnInit() {
    this.loadDatiAmbientali();
    this.loadStabilimenti();
  }

  loadDatiAmbientali() {
    this.loading.set(true);
    this.apiService.getDatiAmbientali()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.datiAmbientali.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Error loading dati ambientali:', err);
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
      dataCampionamento: this.getTodayDate(),
      parametro: '',
      valoreMisurato: undefined,
      unitaMisura: '',
      limiteAutorizzato: undefined,
      statoConformita: StatoConformita.CONFORME,
      laboratorio: '',
      note: ''
    });
    this.showForm.set(true);
  }

  openEditForm(dato: DatiAmbientali) {
    this.editMode.set(true);
    this.formData.set({
      id: dato.id,
      dataCampionamento: dato.dataCampionamento,
      parametro: dato.parametro,
      valoreMisurato: dato.valoreMisurato,
      unitaMisura: dato.unitaMisura,
      limiteAutorizzato: dato.limiteAutorizzato,
      statoConformita: dato.statoConformita,
      laboratorio: dato.laboratorio,
      note: dato.note || ''
    });
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.formData.set({});
  }

  saveDatiAmbientali() {
    const data = this.formData();

    if (!data.dataCampionamento || !data.parametro) {
      alert('Compila tutti i campi obbligatori');
      return;
    }

    const request = this.editMode() && data.id
      ? this.apiService.updateDatiAmbientali(data.id, data as DatiAmbientali)
      : this.apiService.createDatiAmbientali(data as DatiAmbientali);

    request.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.closeForm();
          this.loadDatiAmbientali();
        },
        error: (err) => console.error('Error saving dati ambientali:', err)
      });
  }

  deleteDatiAmbientali(dato: DatiAmbientali) {
    if (!confirm(`Eliminare il dato ${dato.parametro} del ${dato.dataCampionamento}?`)) {
      return;
    }

    this.apiService.deleteDatiAmbientali(dato.id!)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadDatiAmbientali(),
        error: (err) => console.error('Error deleting dati ambientali:', err)
      });
  }

  // Helper methods for form updates
  updateDataCampionamento(value: string) {
    this.formData.update(d => ({ ...d, dataCampionamento: value }));
  }

  updateParametro(value: string) {
    this.formData.update(d => ({ ...d, parametro: value }));
  }

  updateValoreMisurato(value: string) {
    const numValue = value ? parseFloat(value) : undefined;
    this.formData.update(d => ({ ...d, valoreMisurato: numValue }));
  }

  updateUnitaMisura(value: string) {
    this.formData.update(d => ({ ...d, unitaMisura: value }));
  }

  updateLimiteAutorizzato(value: string) {
    const numValue = value ? parseFloat(value) : undefined;
    this.formData.update(d => ({ ...d, limiteAutorizzato: numValue }));
  }

  updateStatoConformita(value: string) {
    this.formData.update(d => ({ ...d, statoConformita: value as StatoConformita }));
  }

  updateMetodoAnalisi(value: string) {
    this.formData.update(d => ({ ...d, metodoAnalisi: value }));
  }

  updateLaboratorio(value: string) {
    this.formData.update(d => ({ ...d, laboratorio: value }));
  }

  updateNote(value: string) {
    this.formData.update(d => ({ ...d, note: value }));
  }

  // Helper method to get today's date in YYYY-MM-DD format
  private getTodayDate(): string {
    const today = new Date();
    return today.toISOString().split('T')[0];
  }

  getConformitaClass(stato: string): string {
    switch (stato?.toUpperCase()) {
      case 'CONFORME':
        return 'status-compliant';
      case 'NON_CONFORME':
      case 'SUPERATO':
        return 'status-non-compliant';
      case 'DA_VERIFICARE':
        return 'status-pending';
      default:
        return '';
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('it-IT');
  }

  formatNumber(value: number | undefined): string {
    if (value === undefined || value === null) return '-';
    return value.toLocaleString('it-IT');
  }

  exportExcel() {
    this.apiService.exportDatiAmbientaliExcel()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `dati-ambientali-${new Date().toISOString().split('T')[0]}.xlsx`;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Error exporting dati ambientali:', err);
          alert('Errore durante l\'esportazione');
        }
      });
  }
}
