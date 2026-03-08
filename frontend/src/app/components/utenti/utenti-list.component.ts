import { Component, inject, signal, computed, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { User, UserRole, RegisterRequest } from '../../models/user.model';
import { Stabilimento } from '../../models/stabilimento.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { EnumTranslatePipe } from '../../pipes/enum-translate.pipe';

@Component({
  selector: 'app-utenti-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, EnumTranslatePipe],
  templateUrl: './utenti-list.component.html',
  styleUrl: './utenti-list.component.css'
})
export class UtentiListComponent implements OnInit {
  // Dependency injection
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  // Signals
  utenti = signal<User[]>([]);
  stabilimenti = signal<Stabilimento[]>([]);
  loading = signal(true);
  showForm = signal(false);
  editMode = signal(false);

  // Form data
  formData = signal<Partial<RegisterRequest & { id?: number; confirmPassword?: string }>>({
    username: '',
    email: '',
    nome: '',
    cognome: '',
    password: '',
    confirmPassword: '',
    ruolo: UserRole.OPERATORE,
    stabilimentiIds: []
  });

  // Computed permissions - Only ADMIN can manage users
  isAdmin = computed(() => {
    //const user = this.authService.currentUser.subscribe();
    return  'ADMIN';
  });

  // Enum options
  ruoli = [UserRole.ADMIN, UserRole.RESPONSABILE, UserRole.OPERATORE];

  ngOnInit() {
    // Check admin permission
    if (!this.isAdmin()) {
      alert('Solo gli amministratori possono accedere a questa pagina');
      return;
    }

    this.loadUtenti();
    this.loadStabilimenti();
  }

  loadUtenti() {
    this.loading.set(true);
    this.apiService.getUtenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.utenti.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Error loading utenti:', err);
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
      username: '',
      email: '',
      nome: '',
      cognome: '',
      password: '',
      confirmPassword: '',
      ruolo: UserRole.OPERATORE,
      stabilimentiIds: []
    });
    this.showForm.set(true);
  }

  openEditForm(utente: User) {
    this.editMode.set(true);
    this.formData.set({
      id: utente.id,
      username: utente.username,
      email: utente.email,
      nome: utente.nome,
      cognome: utente.cognome,
      ruolo: utente.ruolo,
      stabilimentiIds: utente.stabilimenti || []
    });
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.formData.set({});
  }

  saveUtente() {
    const data = this.formData();

    // Validation
    if (!data.username || !data.email || !data.nome || !data.cognome || !data.ruolo) {
      alert('Compila tutti i campi obbligatori');
      return;
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(data.email)) {
      alert('Inserisci un indirizzo email valido');
      return;
    }

    // Password validation for new users
    if (!this.editMode()) {
      if (!data.password || data.password.length < 8) {
        alert('La password deve contenere almeno 8 caratteri');
        return;
      }
      if (data.password !== data.confirmPassword) {
        alert('Le password non corrispondono');
        return;
      }
    }

    // Prepare request
    const request = this.editMode() && data.id
      ? this.apiService.updateUtente(data.id, {
          username: data.username,
          email: data.email,
          nome: data.nome,
          cognome: data.cognome,
          ruolo: data.ruolo!,
          stabilimenti: data.stabilimentiIds
        })
      : this.apiService.createUtente({
          username: data.username!,
          email: data.email!,
          password: data.password!,
          nome: data.nome!,
          cognome: data.cognome!,
          ruolo: data.ruolo!,
          stabilimentiIds: data.stabilimentiIds
        });

    request.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.closeForm();
          this.loadUtenti();
        },
        error: (err) => {
          console.error('Error saving utente:', err);
          alert('Errore durante il salvataggio: ' + (err.error?.message || err.message));
        }
      });
  }

  deleteUtente(utente: User) {
    if (!confirm(`Eliminare l'utente ${utente.username}?`)) {
      return;
    }

    // Prevent deleting current user
    const currentUser = this.authService.currentUserValue;
    if (currentUser?.id === utente.id) {
      alert('Non puoi eliminare il tuo account mentre sei connesso');
      return;
    }

    this.apiService.deleteUtente(utente.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadUtenti(),
        error: (err) => {
          console.error('Error deleting utente:', err);
          alert('Errore durante l\'eliminazione: ' + (err.error?.message || err.message));
        }
      });
  }

  // Helper methods for form updates
  updateUsername(value: string) {
    this.formData.update(d => ({ ...d, username: value }));
  }

  updateEmail(value: string) {
    this.formData.update(d => ({ ...d, email: value }));
  }

  updateNome(value: string) {
    this.formData.update(d => ({ ...d, nome: value }));
  }

  updateCognome(value: string) {
    this.formData.update(d => ({ ...d, cognome: value }));
  }

  updatePassword(value: string) {
    this.formData.update(d => ({ ...d, password: value }));
  }

  updateConfirmPassword(value: string) {
    this.formData.update(d => ({ ...d, confirmPassword: value }));
  }

  updateRuolo(value: string) {
    this.formData.update(d => ({ ...d, ruolo: value as UserRole }));
  }

  updateStabilimenti(event: Event) {
    const select = event.target as HTMLSelectElement;
    const selectedIds = Array.from(select.selectedOptions).map(opt => Number(opt.value));
    this.formData.update(d => ({ ...d, stabilimentiIds: selectedIds }));
  }

  getRuoloClass(ruolo: string): string {
    switch (ruolo?.toUpperCase()) {
      case 'ADMIN':
        return 'role-admin';
      case 'RESPONSABILE':
        return 'role-responsabile';
      case 'OPERATORE':
        return 'role-operatore';
      default:
        return '';
    }
  }

  getStatoClass(attivo: boolean): string {
    return attivo ? 'status-active' : 'status-inactive';
  }

  getStabilimentiNames(stabilimentiIds: number[] | undefined): string {
    if (!stabilimentiIds || stabilimentiIds.length === 0) {
      return 'Nessuno';
    }
    const names = stabilimentiIds
      .map(id => this.stabilimenti().find(s => s.id === id)?.nome)
      .filter(name => name !== undefined);
    return names.join(', ') || 'N/A';
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('it-IT');
  }

  exportExcel() {
    this.apiService.exportUtentiExcel()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `utenti-${new Date().toISOString().split('T')[0]}.xlsx`;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error('Error exporting utenti:', err);
          alert('Errore durante l\'esportazione');
        }
      });
  }
}
