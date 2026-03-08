import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import itTranslations from '../../assets/i18n/it.json';
import enTranslations from '../../assets/i18n/en.json';

export type Language = 'it' | 'en';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private currentLanguage = new BehaviorSubject<Language>('it');
  private translations: { [key: string]: any } = {
    it: itTranslations,
    en: enTranslations
  };

  currentLanguage$ = this.currentLanguage.asObservable();

  constructor() {
    // Load saved language from localStorage
    const savedLang = localStorage.getItem('language') as Language;
    if (savedLang) {
      this.setLanguage(savedLang);
    }
  }

  setLanguage(lang: Language) {
    this.currentLanguage.next(lang);
    localStorage.setItem('language', lang);
  }

  getLanguage(): Language {
    return this.currentLanguage.value;
  }

  translate(key: string): string {
    const lang = this.currentLanguage.value;
    const translation = this.getNestedTranslation(this.translations[lang], key);
    return translation || key;
  }

  private getNestedTranslation(obj: any, path: string): string {
    const keys = path.split('.');
    let result = obj;

    for (const key of keys) {
      if (result && result[key] !== undefined) {
        result = result[key];
      } else {
        return path;
      }
    }

    return result;
  }

  // Helper method for template usage
  instant(key: string): string {
    return this.translate(key);
  }
}
