import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Stabilimento } from '../../models/stabilimento.model';
import { Prescrizione } from '../../models/prescrizione.model';
import { DatiAmbientali, StatoConformita } from '../../models/dati-ambientali.model';
import { Scadenza } from '../../models/scadenza.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { EnumTranslatePipe } from '../../pipes/enum-translate.pipe';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, EnumTranslatePipe],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stabilimenti: Stabilimento[] = [];
  prescrizioni: Prescrizione[] = [];
  scadenzeProssime: Scadenza[] = [];
  datiNonConformi: DatiAmbientali[] = [];

  stats = {
    stabilimentiAttivi: 0,
    prescrizioniAperte: 0,
    scadenzeImminenti: 0,
    nonConformita: 0
  };

  // Relazione Annuale
  showRelazioneForm = false;
  relazioneStabilimentoId: string = '';
  relazioneAnno: number = new Date().getFullYear();
  anniDisponibili: number[] = [];

  StatoConformita = StatoConformita;

  constructor(private apiService: ApiService) {
    // Genera lista anni (ultimi 5 anni)
    const currentYear = new Date().getFullYear();
    for (let i = 0; i < 5; i++) {
      this.anniDisponibili.push(currentYear - i);
    }
  }

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.apiService.getStabilimenti().subscribe({
      next: (data) => {
        this.stabilimenti = data;
        this.stats.stabilimentiAttivi = data.filter(s => s.attivo).length;
      },
      error: (error) => console.error('Errore nel caricamento stabilimenti:', error)
    });

    this.apiService.getPrescrizioni().subscribe({
      next: (data) => {
        this.prescrizioni = data;
        this.stats.prescrizioniAperte = data.filter(p => p.stato !== 'CHIUSA').length;
      },
      error: (error) => console.error('Errore nel caricamento prescrizioni:', error)
    });

    this.apiService.getScadenzeProssimi30Giorni().subscribe({
      next: (data) => {
        this.scadenzeProssime = data;
        this.stats.scadenzeImminenti = data.length;
      },
      error: (error) => console.error('Errore nel caricamento scadenze:', error)
    });

    this.apiService.getDatiNonConformi().subscribe({
      next: (data) => {
        this.datiNonConformi = data;
        this.stats.nonConformita = data.length;
      },
      error: (error) => console.error('Errore nel caricamento dati non conformi:', error)
    });
  }

  getConformitaClass(stato: string): string {
    switch (stato) {
      case 'CONFORME': return 'conforme';
      case 'ATTENZIONE': return 'attenzione';
      case 'NON_CONFORME': return 'non-conforme';
      default: return '';
    }
  }

  getPrioritaClass(priorita: string): string {
    switch (priorita) {
      case 'URGENTE': return 'priorita-urgente';
      case 'ALTA': return 'priorita-alta';
      case 'MEDIA': return 'priorita-media';
      case 'BASSA': return 'priorita-bassa';
      default: return '';
    }
  }

  generateRelazione() {
    if (!this.relazioneStabilimentoId) {
      alert('⚠️ Seleziona uno stabilimento');
      return;
    }

    const url = `${environment.apiUrl}/export/relazione-annuale/${this.relazioneStabilimentoId}/anno/${this.relazioneAnno}`;
    window.open(url, '_blank');
    this.showRelazioneForm = false;
  }
}
