import { Component, inject, signal, computed, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Prescrizione, MatriceAmbientale, StatoPrescrizione, Priorita } from '../../models/prescrizione.model';

interface PrescrizioneForm extends Partial<Prescrizione> {
  stabilimentoId?: number;
}
import { Stabilimento } from '../../models/stabilimento.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { EnumTranslatePipe } from '../../pipes/enum-translate.pipe';

@Component({
  selector: 'app-prescrizioni-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, EnumTranslatePipe],
  templateUrl: './prescrizioni-list.component.html',
  styleUrl: './prescrizioni-list.component.css'
})
export class PrescrizioniListComponent implements OnInit {
  // Dependency injection
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  // Signals
  prescrizioni = signal<Prescrizione[]>([]);
  stabilimenti = signal<Stabilimento[]>([]);
  loading = signal(true);
  showForm = signal(false);
  editMode = signal(false);

  // Form data
  formData = signal<PrescrizioneForm>({
    codice: '',
    descrizione: '',
    stabilimentoId: 0,
    matriceAmbientale: MatriceAmbientale.ARIA,
    stato: StatoPrescrizione.APERTA,
    priorita: Priorita.MEDIA,
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
  matriciAmbientali = ['ARIA', 'ACQUA', 'SUOLO', 'RIFIUTI', 'RUMORE', 'ENERGIA', 'EMISSIONI', 'SCARICHI'];
  stati = ['APERTA', 'IN_CORSO', 'COMPLETATA', 'CHIUSA', 'SOSPESA', 'ANNULLATA'];
  prioritaOptions = ['ALTA', 'MEDIA', 'BASSA', 'URGENTE', 'NORMALE'];

  ngOnInit() {
    this.loadPrescrizioni();
    this.loadStabilimenti();
  }

  loadPrescrizioni() {
    this.loading.set(true);
    this.apiService.getPrescrizioni()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.prescrizioni.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Error loading prescrizioni:', err);
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
      codice: '',
      descrizione: '',
      stabilimentoId: this.stabilimenti()[0]?.id || 0,
      matriceAmbientale: MatriceAmbientale.ARIA,
      stato: StatoPrescrizione.APERTA,
      priorita: Priorita.MEDIA,
      note: ''
    });
    this.showForm.set(true);
  }

  openEditForm(prescrizione: Prescrizione) {
    this.editMode.set(true);
    this.formData.set({
      id: prescrizione.id,
      codice: prescrizione.codice,
      descrizione: prescrizione.descrizione,
      stabilimentoId: prescrizione.stabilimento?.id || 0,
      matriceAmbientale: prescrizione.matriceAmbientale,
      stato: prescrizione.stato,
      priorita: prescrizione.priorita,
      note: prescrizione.note || ''
    });
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.formData.set({});
  }

  savePrescrizione() {
    const data = this.formData();

    if (!data.codice || !data.descrizione || !(data as PrescrizioneForm).stabilimentoId) {
      alert('Compila tutti i campi obbligatori');
      return;
    }

    const request = this.editMode() && data.id
      ? this.apiService.updatePrescrizione(data.id, data as Prescrizione)
      : this.apiService.createPrescrizione(data as Prescrizione);

    request.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.closeForm();
          this.loadPrescrizioni();
        },
        error: (err) => console.error('Error saving prescrizione:', err)
      });
  }

  deletePrescrizione(prescrizione: Prescrizione) {
    if (!confirm(`Eliminare la prescrizione ${prescrizione.codice}?`)) {
      return;
    }

    this.apiService.deletePrescrizione(prescrizione.id!)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadPrescrizioni(),
        error: (err) => console.error('Error deleting prescrizione:', err)
      });
  }

  // Helper methods for form updates
  updateCodice(value: string) {
    this.formData.update(d => ({ ...d, codice: value }));
  }

  updateDescrizione(value: string) {
    this.formData.update(d => ({ ...d, descrizione: value }));
  }

  updateStabilimentoId(value: number) {
    this.formData.update(d => ({ ...d, stabilimentoId: value }));
  }

  updateMatrice(value: string) {
    this.formData.update(d => ({ ...d, matriceAmbientale: value as MatriceAmbientale }));
  }

  updateStato(value: string) {
    this.formData.update(d => ({ ...d, stato: value as StatoPrescrizione }));
  }

  updatePriorita(value: string) {
    this.formData.update(d => ({ ...d, priorita: value as Priorita }));
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
      case 'APERTA':
      case 'IN_CORSO':
        return 'status-active';
      case 'COMPLETATA':
      case 'CHIUSA':
        return 'status-completed';
      case 'SOSPESA':
      case 'ANNULLATA':
        return 'status-suspended';
      default:
        return '';
    }
  }

  exportExcel() {
    this.apiService.exportPrescrizioniExcel()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `prescrizioni-${new Date().toISOString().split('T')[0]}.xlsx`;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Error exporting prescrizioni:', err);
          alert('Errore durante l\'esportazione');
        }
      });
  }
}
