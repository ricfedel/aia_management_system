import { Pipe, PipeTransform } from '@angular/core';
import { TranslationService } from '../services/translation.service';

/**
 * Pipe per tradurre valori enum (stati, priorità, matrici ambientali, ecc.)
 *
 * Uso: {{ valore | enumTranslate:'categoria' }}
 *
 * Esempi:
 * - {{ 'CONFORME' | enumTranslate:'statoConformita' }}
 * - {{ 'ALTA' | enumTranslate:'priorita' }}
 * - {{ 'ARIA' | enumTranslate:'matriceAmbientale' }}
 */
@Pipe({
  name: 'enumTranslate',
  standalone: true,
  pure: false // Per aggiornare quando cambia la lingua
})
export class EnumTranslatePipe implements PipeTransform {

  constructor(private translationService: TranslationService) {}

  transform(value: string | null | undefined, category: string): string {
    if (!value) {
      return '';
    }

    // Converti il valore in uppercase per gestire case variations
    const normalizedValue = value.toUpperCase().replace(/\s+/g, '_');

    // Costruisci la chiave di traduzione: enums.categoria.VALORE
    const translationKey = `enums.${category}.${normalizedValue}`;

    // Ottieni la traduzione
    const translation = this.translationService.translate(translationKey);

    // Se la traduzione non esiste, ritorna il valore originale formattato
    if (translation === translationKey) {
      return this.formatFallback(value);
    }

    return translation;
  }

  /**
   * Formatta il valore se la traduzione non è disponibile
   * Es: "NON_CONFORME" → "Non Conforme"
   */
  private formatFallback(value: string): string {
    return value
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, char => char.toUpperCase());
  }
}
