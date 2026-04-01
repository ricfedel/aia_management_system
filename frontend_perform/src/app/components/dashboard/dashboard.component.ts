import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Stabilimento } from '../../models/stabilimento.model';
import { Prescrizione } from '../../models/prescrizione.model';
import { DatiAmbientali, StatoConformita } from '../../models/dati-ambientali.model';
import { ScadenzaImminente, KpiAmbientale, StatoCampionamento } from '../../models/dashboard.model';
import { AltraAutorizzazione } from '../../models/altra-autorizzazione.model';
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
  altreAutorizzazioniMap: Map<number, AltraAutorizzazione[]> = new Map();
  kpiAmbientali: KpiAmbientale[] = [];
  statoCampionamenti: StatoCampionamento[] = [];
  campionamentiPerStabilimento: Map<string, StatoCampionamento[]> = new Map();

  // Calendario
  calendarMese: number = new Date().getMonth();
  calendarAnno: number = new Date().getFullYear();
  calendarDays: { date: Date; campionamenti: StatoCampionamento[] }[] = [];
  readonly NOMI_MESI = ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
                        'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'];
  readonly NOMI_GIORNI = ['L','M','M','G','V','S','D'];
  selectedDay: { date: Date; campionamenti: StatoCampionamento[] } | null = null;
  prescrizioni: Prescrizione[] = [];
  scadenzeProssime: ScadenzaImminente[] = [];
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

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
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

    this.apiService.getKpiAmbientali().subscribe({
      next: (data) => { this.kpiAmbientali = data; },
      error: (error) => console.error('Errore nel caricamento KPI ambientali:', error)
    });

    this.apiService.getStatoCampionamenti().subscribe({
      next: (data) => {
        this.statoCampionamenti = data;
        this.campionamentiPerStabilimento = new Map();
        data.forEach(c => {
          const list = this.campionamentiPerStabilimento.get(c.stabilimentoNome) || [];
          list.push(c);
          this.campionamentiPerStabilimento.set(c.stabilimentoNome, list);
        });
        this.buildCalendar();
      },
      error: (error) => console.error('Errore nel caricamento stato campionamenti:', error)
    });

    this.apiService.getAltreAutorizzazioni().subscribe({
      next: (data) => {
        this.altreAutorizzazioniMap = new Map();
        data.forEach(a => {
          const list = this.altreAutorizzazioniMap.get(a.stabilimentoId) || [];
          list.push(a);
          this.altreAutorizzazioniMap.set(a.stabilimentoId, list);
        });
      },
      error: (error) => console.error('Errore nel caricamento altre autorizzazioni:', error)
    });

    this.apiService.getPrescrizioni().subscribe({
      next: (data) => {
        this.prescrizioni = data;
        this.stats.prescrizioniAperte = data.filter(p => p.stato !== 'CHIUSA').length;
      },
      error: (error) => console.error('Errore nel caricamento prescrizioni:', error)
    });

    this.apiService.getScadenzeImminenti(90).subscribe({
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

  getPrescrizioniAperte(): Prescrizione[] {
    return this.prescrizioni.filter(p => p.stato !== 'CHIUSA').slice(0, 5);
  }

  get ruolo(): string {
    return this.authService.currentUserValue?.ruolo || '';
  }

  get isAdmin(): boolean {
    return this.ruolo === 'ADMIN';
  }

  get isResponsabileOrAdmin(): boolean {
    return this.ruolo === 'ADMIN' || this.ruolo === 'RESPONSABILE';
  }

  navigateTo(route: string, enabled: boolean): void {
    if (enabled) {
      this.router.navigate([route]);
    }
  }

  getConformitaClass(stato: string): string {
    switch (stato) {
      case 'CONFORME': return 'conforme';
      case 'ATTENZIONE': return 'attenzione';
      case 'NON_CONFORME': return 'non-conforme';
      default: return '';
    }
  }

  getRifiutiClass(ok: boolean): string {
    return ok ? 'semaforo-verde' : 'semaforo-rosso';
  }

  getCampionamentoClass(colore: string): string {
    switch (colore) {
      case 'VERDE': return 'camp-verde';
      case 'GIALLO': return 'camp-giallo';
      case 'ROSSO': return 'camp-rosso';
      default: return '';
    }
  }

  getCampionamentiStabilimenti(): string[] {
    return Array.from(this.campionamentiPerStabilimento.keys());
  }

  buildCalendar(): void {
    const anno = this.calendarAnno;
    const mese = this.calendarMese;
    const giorniNelMese = new Date(anno, mese + 1, 0).getDate();

    // Mappa data (YYYY-MM-DD) → campionamenti
    const mappa = new Map<string, StatoCampionamento[]>();
    this.statoCampionamenti.forEach(c => {
      if (!c.prossimaScadenza) return;
      const d = new Date(c.prossimaScadenza);
      if (d.getFullYear() === anno && d.getMonth() === mese) {
        const key = c.prossimaScadenza.substring(0, 10);
        const list = mappa.get(key) || [];
        list.push(c);
        mappa.set(key, list);
      }
    });

    this.calendarDays = [];
    for (let g = 1; g <= giorniNelMese; g++) {
      const date = new Date(anno, mese, g);
      const key = `${anno}-${String(mese + 1).padStart(2, '0')}-${String(g).padStart(2, '0')}`;
      this.calendarDays.push({ date, campionamenti: mappa.get(key) || [] });
    }
  }

  calendarPrevMese(): void {
    if (this.calendarMese === 0) {
      this.calendarMese = 11;
      this.calendarAnno--;
    } else {
      this.calendarMese--;
    }
    this.buildCalendar();
  }

  calendarNextMese(): void {
    if (this.calendarMese === 11) {
      this.calendarMese = 0;
      this.calendarAnno++;
    } else {
      this.calendarMese++;
    }
    this.buildCalendar();
  }

  getCalendarOffset(): number[] {
    // Offset iniziale: lunedì = 0
    const primoGiorno = new Date(this.calendarAnno, this.calendarMese, 1).getDay();
    const offset = (primoGiorno + 6) % 7; // converte domenica=0 in lunedì=0
    return Array(offset).fill(0);
  }

  selectCalendarDay(day: { date: Date; campionamenti: StatoCampionamento[] }): void {
    if (day.campionamenti.length === 0) return;
    if (day.campionamenti.length === 1) {
      this.navigateToPuntiMonitoraggio(day.campionamenti[0].monitoraggioId);
      return;
    }
    const isSame = this.selectedDay != null && this.selectedDay.date.getTime() === day.date.getTime();
    this.selectedDay = isSame ? null : day;
    if (!isSame) {
      setTimeout(() => {
        document.querySelector('.cal-detail')?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }, 50);
    }
  }

  navigateToPuntiMonitoraggio(monitoraggioId?: number): void {
    this.router.navigate(['/punti-monitoraggio'], monitoraggioId ? { queryParams: { id: monitoraggioId } } : {});
  }

  isToday(date: Date): boolean {
    const oggi = new Date();
    return date.getDate() === oggi.getDate() &&
           date.getMonth() === oggi.getMonth() &&
           date.getFullYear() === oggi.getFullYear();
  }

  getAltreAutorizzazioni(stabilimentoId?: number): AltraAutorizzazione[] {
    if (!stabilimentoId) return [];
    return this.altreAutorizzazioniMap.get(stabilimentoId) || [];
  }

  getGiorniRimanestiClass(giorni: number): string {
    if (giorni <= 15) return 'giorni-urgente';
    if (giorni <= 30) return 'giorni-warning';
    return 'giorni-ok';
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
