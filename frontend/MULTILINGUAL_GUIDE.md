# Guida Sistema Multilingua - AIA Management System

## Panoramica

Il sistema supporta **Italiano** e **Inglese** con traduzione completa di:
- ✅ Label UI e intestazioni
- ✅ Messaggi di sistema
- ✅ Valori enum (stati, priorità, matrici ambientali, ecc.)
- ⚠️ Descrizioni utente (approccio ibrido - vedi sotto)

---

## 1. Traduzione Label UI

### Uso della TranslatePipe

Per tradurre label statiche dell'interfaccia:

```html
<!-- Template HTML -->
<h1>{{ 'dashboard.title' | translate }}</h1>
<button>{{ 'common.save' | translate }}</button>
<label>{{ 'documenti.nome' | translate }}</label>
```

### Struttura File Traduzione

I file si trovano in `src/assets/i18n/`:

```
i18n/
├── it.json  (Italiano)
└── en.json  (Inglese)
```

Esempio struttura:
```json
{
  "common": {
    "save": "Salva",
    "cancel": "Annulla"
  },
  "dashboard": {
    "title": "Dashboard AIA Management System"
  }
}
```

---

## 2. Traduzione Valori Enum

### Uso della EnumTranslatePipe

Per tradurre valori che arrivano dal backend (stati, priorità, ecc.):

```html
<!-- Prima (senza traduzione) -->
<td>{{ prescrizione.stato }}</td>
<!-- Mostra: "APERTA" -->

<!-- Dopo (con traduzione) -->
<td>{{ prescrizione.stato | enumTranslate:'statoPrescrizione' }}</td>
<!-- Mostra: "Aperta" (IT) o "Open" (EN) -->
```

### Categorie Enum Disponibili

| Categoria | Uso | Esempi Valori |
|-----------|-----|---------------|
| `statoConformita` | Stato conformità dati ambientali | CONFORME, NON_CONFORME, DA_VERIFICARE |
| `priorita` | Livelli priorità | ALTA, MEDIA, BASSA, URGENTE |
| `matriceAmbientale` | Matrici ambientali | ARIA, ACQUA, RIFIUTI, ENERGIA |
| `statoPrescrizione` | Stato prescrizioni AIA | APERTA, IN_CORSO, COMPLETATA, CHIUSA |
| `tipoScadenza` | Tipo di scadenza | COMUNICAZIONE, VERIFICA, RINNOVO |
| `statoScadenza` | Stato scadenza | DA_FARE, IN_CORSO, COMPLETATA, SCADUTA |
| `ruolo` | Ruolo utente | ADMIN, RESPONSABILE, OPERATORE |
| `statoStabilimento` | Stato stabilimento | ATTIVO, INATTIVO, SOSPESO |
| `tipoDocumento` | Tipo documento | PRESCRIZIONE_AIA, RAPPORTO_PROVA, ecc. |

### Aggiungere Nuovi Enum

**1. Aggiungi traduzioni a `it.json` e `en.json`:**

```json
// it.json
{
  "enums": {
    "nuovaCategoria": {
      "VALORE_1": "Valore Uno",
      "VALORE_2": "Valore Due"
    }
  }
}

// en.json
{
  "enums": {
    "nuovaCategoria": {
      "VALORE_1": "Value One",
      "VALORE_2": "Value Two"
    }
  }
}
```

**2. Usa nel template:**

```html
<td>{{ oggetto.campo | enumTranslate:'nuovaCategoria' }}</td>
```

### Fallback Automatico

Se una traduzione non esiste, la pipe formatta automaticamente il valore:
- `NON_CONFORME` → `Non Conforme`
- `IN_ATTESA` → `In Attesa`

---

## 3. Traduzione Descrizioni Utente

### Problema

Le descrizioni sono testo libero inserito dagli utenti:
- Descrizioni prescrizioni
- Note su scadenze
- Commenti su documenti

**Non possiamo tradurle automaticamente** (troppo costoso e impreciso).

### Soluzioni Possibili

#### Opzione A: Monolingua (Attuale)

**Approccio**: Le descrizioni restano nella lingua inserita dall'utente.

**Pro**:
- ✅ Semplice
- ✅ Nessun costo aggiuntivo
- ✅ Contenuto originale preservato

**Contro**:
- ❌ Utenti multilingua vedono mix di lingue

**Implementazione**: Nessuna modifica necessaria.

```html
<!-- La descrizione viene mostrata così com'è -->
<td>{{ prescrizione.descrizione }}</td>
```

---

#### Opzione B: Campo Multilingua nel Database

**Approccio**: Salvare descrizioni in entrambe le lingue.

**Modello Backend**:
```java
@Entity
public class Prescrizione {
    private Long id;
    private String codice;

    // Descrizioni multilingua
    @Column(columnDefinition = "TEXT")
    private String descrizioneIt;

    @Column(columnDefinition = "TEXT")
    private String descrizioneEn;

    // Getter/Setter...
}
```

**Frontend Service**:
```typescript
export class PrescrizioneService {
  constructor(private translationService: TranslationService) {}

  getDescrizione(prescrizione: Prescrizione): string {
    const lang = this.translationService.currentLanguage.value;
    return lang === 'it' ? prescrizione.descrizioneIt : prescrizione.descrizioneEn;
  }
}
```

**Template**:
```html
<td>{{ prescrizioneService.getDescrizione(prescrizione) }}</td>
```

**Pro**:
- ✅ Supporto completo multilingua
- ✅ Nessuna traduzione automatica necessaria

**Contro**:
- ❌ Schema database più complesso
- ❌ Utenti devono inserire testo in entrambe le lingue
- ❌ Interfaccia form più complessa

**Costo implementazione**: ~4-6 ore

---

#### Opzione C: Traduzione Automatica API

**Approccio**: Usare servizio esterno (Google Translate, DeepL) per tradurre al volo.

**Service Example**:
```typescript
export class TranslationApiService {
  private cache = new Map<string, string>();

  async translateText(text: string, targetLang: string): Promise<string> {
    const cacheKey = `${text}_${targetLang}`;

    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)!;
    }

    // Chiamata API DeepL/Google
    const translated = await this.callTranslationApi(text, targetLang);
    this.cache.set(cacheKey, translated);

    return translated;
  }
}
```

**Template con Pipe Async**:
```html
<td>{{ prescrizione.descrizione | autoTranslate | async }}</td>
```

**Pro**:
- ✅ Traduzione automatica
- ✅ Nessuna modifica al database
- ✅ Trasparente per gli utenti

**Contro**:
- ❌ Costo API ricorrente
- ❌ Ritardo nella visualizzazione
- ❌ Qualità traduzione variabile
- ❌ Dipendenza da servizio esterno

**Costi**:
- Implementazione: ~2-3 ore
- API: €20-50/mese (DeepL) o €20/milione caratteri (Google)

---

#### Opzione D: Traduzione Manuale On-Demand

**Approccio**: Pulsante "Traduci" che chiede API solo quando necessario.

**Template**:
```html
<td>
  {{ showTranslated ? (translatedText || prescrizione.descrizione) : prescrizione.descrizione }}
  <button (click)="translateDescription(prescrizione)"
          *ngIf="currentLang !== 'it'">
    🌐 {{ 'common.translate' | translate }}
  </button>
</td>
```

**Component**:
```typescript
async translateDescription(prescrizione: Prescrizione) {
  this.translatedText = await this.translationApi.translateText(
    prescrizione.descrizione,
    this.currentLang
  );
  this.showTranslated = true;
}
```

**Pro**:
- ✅ Costo API ridotto (solo quando richiesto)
- ✅ Utente controlla quando tradurre
- ✅ Mostra sempre originale

**Contro**:
- ❌ Richiede click aggiuntivo
- ❌ Qualità traduzione variabile

**Costo implementazione**: ~2-3 ore + costi API ridotti

---

## 4. Raccomandazione

### Per Grandi Molini Italiani

**Scenario attuale**: Utenti principalmente italiani, alcuni utenti inglesi occasionali.

**Raccomandazione**: **Opzione A (Monolingua)** + **Opzione D (Traduzione On-Demand)** per casi specifici.

**Motivazione**:
- La maggior parte dei contenuti è in italiano
- Enum e UI già completamente tradotti
- Traduzione on-demand disponibile per utenti esteri
- Costo ridotto, implementazione semplice

**Implementazione futura**: Se aumenta percentuale utenti non italiani, migrare a **Opzione B (Campo Multilingua)**.

---

## 5. Cambio Lingua

### Componente Navbar

```html
<div class="language-switcher">
  <button [class.active]="currentLanguage() === 'it'"
          (click)="switchLanguage('it')">IT</button>
  <button [class.active]="currentLanguage() === 'en'"
          (click)="switchLanguage('en')">EN</button>
</div>
```

### Service

```typescript
export class TranslationService {
  private currentLanguage = new BehaviorSubject<Language>('it');
  currentLanguage$ = this.currentLanguage.asObservable();

  setLanguage(lang: Language) {
    this.currentLanguage.next(lang);
    localStorage.setItem('language', lang);
  }

  getLanguage(): Language {
    return this.currentLanguage.value;
  }
}
```

### Persistenza

La lingua scelta viene salvata in `localStorage` e ripristinata al prossimo accesso.

---

## 6. Testing

### Test Checklist

- [ ] Cambio lingua aggiorna tutti i componenti
- [ ] Enum tradotti correttamente in entrambe le lingue
- [ ] Fallback funziona per valori non tradotti
- [ ] Lingua persiste dopo logout/login
- [ ] Date formattate secondo locale (IT: dd/MM/yyyy, EN: MM/dd/yyyy)

### Test Manuale

1. Login come admin
2. Cambia lingua da IT a EN
3. Naviga: Dashboard → Stabilimenti → Documenti
4. Verifica che tutti i testi siano in inglese
5. Verifica che stati/priorità siano tradotti
6. Logout e login → verifica lingua mantenuta

---

## 7. Manutenzione

### Aggiungere Nuova Label

1. Aggiungi chiave a `it.json`:
```json
{
  "nuovaSezione": {
    "nuovaLabel": "Testo italiano"
  }
}
```

2. Aggiungi traduzione a `en.json`:
```json
{
  "nuovaSezione": {
    "nuovaLabel": "English text"
  }
}
```

3. Usa nel template:
```html
{{ 'nuovaSezione.nuovaLabel' | translate }}
```

### Aggiungere Nuovo Enum

1. Definisci valori in `enums.<categoria>` nei file JSON
2. Usa `enumTranslate` pipe nel template
3. Test in entrambe le lingue

---

## 8. Best Practices

✅ **DO**:
- Usa sempre chiavi di traduzione strutturate (`sezione.chiave`)
- Testa in entrambe le lingue prima del deploy
- Mantieni traduzioni sincronizzate tra it.json e en.json
- Usa `enumTranslate` per tutti i valori dal backend

❌ **DON'T**:
- Hardcodare testo in italiano nei template
- Usare chiavi troppo generiche (`label1`, `text2`)
- Dimenticare di tradurre nuove funzionalità
- Tradurre dati utente senza consenso

---

## 9. Supporto Tecnico

**Contatti**:
- Frontend: Vedi `translation.service.ts`
- Backend enum: Vedi modelli in `model/`
- Problemi: Creare issue su repository

**File chiave**:
- `src/app/services/translation.service.ts` - Service principale
- `src/app/pipes/translate.pipe.ts` - Pipe per label UI
- `src/app/pipes/enum-translate.pipe.ts` - Pipe per enum
- `src/assets/i18n/it.json` - Traduzioni italiane
- `src/assets/i18n/en.json` - Traduzioni inglesi

---

**Ultima modifica**: Febbraio 2026
**Versione**: 2.0
