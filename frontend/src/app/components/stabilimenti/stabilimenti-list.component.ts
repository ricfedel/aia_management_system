import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Stabilimento } from '../../models/stabilimento.model';

@Component({
  selector: 'app-stabilimenti-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stabilimenti-list.component.html',
  styleUrl: './stabilimenti-list.component.css'
})
export class StabilimentiListComponent implements OnInit {
  stabilimenti: Stabilimento[] = [];
  loading = true;
  showForm = false;
  editingStabilimento: Stabilimento | null = null;

  // Form data
  formData: Stabilimento = this.getEmptyStabilimento();

  constructor(
    private apiService: ApiService,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.loadStabilimenti();
  }

  loadStabilimenti() {
    this.loading = true;
    this.apiService.getStabilimenti().subscribe({
      next: (data) => {
        this.stabilimenti = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Errore caricamento stabilimenti:', error);
        this.loading = false;
      }
    });
  }

  openCreateForm() {
    this.editingStabilimento = null;
    this.formData = this.getEmptyStabilimento();
    this.showForm = true;
  }

  openEditForm(stabilimento: Stabilimento) {
    this.editingStabilimento = stabilimento;
    this.formData = { ...stabilimento };
    this.showForm = true;
  }

  closeForm() {
    this.showForm = false;
    this.editingStabilimento = null;
    this.formData = this.getEmptyStabilimento();
  }

  saveStabilimento() {
    if (this.editingStabilimento) {
      // Update
      this.apiService.updateStabilimento(this.editingStabilimento.id!, this.formData).subscribe({
        next: () => {
          this.loadStabilimenti();
          this.closeForm();
        },
        error: (error) => {
          console.error('Errore aggiornamento:', error);
          alert('Errore durante aggiornamento stabilimento');
        }
      });
    } else {
      // Create
      this.apiService.createStabilimento(this.formData).subscribe({
        next: () => {
          this.loadStabilimenti();
          this.closeForm();
        },
        error: (error) => {
          console.error('Errore creazione:', error);
          alert('Errore durante creazione stabilimento');
        }
      });
    }
  }

  deleteStabilimento(stabilimento: Stabilimento) {
    if (!confirm(`Sei sicuro di voler eliminare lo stabilimento "${stabilimento.nome}"?`)) {
      return;
    }

    this.apiService.deleteStabilimento(stabilimento.id!).subscribe({
      next: () => {
        this.loadStabilimenti();
      },
      error: (error) => {
        console.error('Errore eliminazione:', error);
        alert('Errore durante eliminazione stabilimento');
      }
    });
  }

  private getEmptyStabilimento(): Stabilimento {
    return {
      nome: '',
      citta: '',
      indirizzo: '',
      numeroAIA: '',
      dataRilascioAIA: '',
      dataScadenzaAIA: '',
      enteCompetente: '',
      responsabileAmbientale: '',
      email: '',
      telefono: '',
      attivo: true
    };
  }

  get canEdit(): boolean {
    return this.authService.canEdit();
  }

  get canDelete(): boolean {
    return this.authService.canDelete();
  }
}
