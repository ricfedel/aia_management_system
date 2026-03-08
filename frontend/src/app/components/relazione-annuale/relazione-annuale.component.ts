import { Component, inject, signal, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { RelazioneAnnualeDTO } from '../../models/relazione-annuale.model';
import { Stabilimento } from '../../models/stabilimento.model';

@Component({
  selector: 'app-relazione-annuale',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './relazione-annuale.component.html',
  styleUrl: './relazione-annuale.component.css'
})
export class RelazioneAnnualeComponent implements OnInit {

  private api        = inject(ApiService);
  private auth       = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // ── Stato ─────────────────────────────────────────────────────────────
  stabilimenti    = signal<Stabilimento[]>([]);
  previewData     = signal<RelazioneAnnualeDTO | null>(null);
  loadingPreview  = signal(false);
  loadingDocx     = signal(false);
  loadingXlsx     = signal(false);
  errorMsg        = signal<string | null>(null);

  // ── Parametri generazione ──────────────────────────────────────────────
  selectedStabilimento = signal<number | null>(null);
  selectedAnno         = signal<number>(new Date().getFullYear() - 1);

  // ── Vista sezioni ──────────────────────────────────────────────────────
  expandedSection = signal<string | null>(null);

  // ── Anni selezionabili (ultimi 5 anni) ────────────────────────────────
  anniDisponibili: number[] = Array.from({ length: 6 },
    (_, i) => new Date().getFullYear() - i);

  currentUser = () => this.auth.currentUserValue;

  ngOnInit() {
    this.loadStabilimenti();
  }

  loadStabilimenti() {
    this.api.getStabilimenti()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: data => this.stabilimenti.set(data) });
  }

  loadPreview() {
    const stabId = this.selectedStabilimento();
    const anno   = this.selectedAnno();
    if (!stabId) return;

    this.loadingPreview.set(true);
    this.errorMsg.set(null);
    this.previewData.set(null);

    this.api.getRelazioneAnnualePreview(stabId, anno)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: data => {
          this.previewData.set(data);
          this.loadingPreview.set(false);
        },
        error: () => {
          this.errorMsg.set('Errore nel caricamento dell\'anteprima');
          this.loadingPreview.set(false);
        }
      });
  }

  downloadDocx() {
    const stabId = this.selectedStabilimento();
    const anno   = this.selectedAnno();
    if (!stabId) return;

    this.loadingDocx.set(true);
    this.errorMsg.set(null);

    this.api.downloadRelazioneAnnualeDocx(stabId, anno)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const a   = document.createElement('a');
          a.href    = url;
          a.download = `Relazione_Annuale_AIA_${anno}.docx`;
          a.click();
          URL.revokeObjectURL(url);
          this.loadingDocx.set(false);
        },
        error: () => {
          this.errorMsg.set('Errore nella generazione del documento Word');
          this.loadingDocx.set(false);
        }
      });
  }

  downloadAllegato2() {
    const stabId = this.selectedStabilimento();
    const anno   = this.selectedAnno();
    if (!stabId) return;

    this.loadingXlsx.set(true);
    this.errorMsg.set(null);

    this.api.downloadAllegato2Xlsx(stabId, anno)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: blob => {
          const url = URL.createObjectURL(blob);
          const a   = document.createElement('a');
          a.href    = url;
          a.download = `Allegato2_PMC_${anno}.xlsx`;
          a.click();
          URL.revokeObjectURL(url);
          this.loadingXlsx.set(false);
        },
        error: () => {
          this.errorMsg.set('Errore nella generazione dell\'Allegato 2 Excel');
          this.loadingXlsx.set(false);
        }
      });
  }

  toggleSection(section: string) {
    this.expandedSection.update(s => s === section ? null : section);
  }

  isSectionOpen(section: string): boolean {
    return this.expandedSection() === section;
  }

  // ── Helpers ────────────────────────────────────────────────────────────
  conformitaPercClass(perc: number | undefined): string {
    if (!perc) return 'perc-nd';
    if (perc >= 90) return 'perc-ok';
    if (perc >= 75) return 'perc-att';
    return 'perc-nc';
  }

  statoClass(stato: string | undefined): string {
    if (!stato) return '';
    if (stato === 'NON_CONFORME')  return 'stato-nc';
    if (stato === 'ATTENZIONE')    return 'stato-att';
    if (stato === 'CONFORME')      return 'stato-ok';
    return '';
  }

  fmt(v: number | undefined): string {
    if (v == null) return '—';
    if (v === Math.floor(v)) return v.toString();
    return v.toFixed(2);
  }
}
