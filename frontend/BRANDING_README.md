# 🎨 Branding & Internazionalizzazione - Grandi Molini Italia

## ✅ Implementazioni Completate

### 1. Sistema Multi-Lingua (i18n)
- ✅ Servizio TranslationService per gestione traduzioni
- ✅ Pipe `translate` per uso nei template
- ✅ File traduzioni IT/EN in `src/assets/i18n/`
- ✅ Language switcher nella navbar (IT | EN)
- ✅ Salvataggio preferenza lingua in localStorage

### 2. Colori Brand Grandi Molini Italia
Tutti i colori sono stati aggiornati con la palette GMI:

**Palette Colori:**
- `--gmi-primary: #003366` - Blu scuro professionale
- `--gmi-secondary: #0066CC` - Blu medio
- `--gmi-accent: #D4AF37` - Oro/Giallo (accent color)
- `--gmi-light: #F5F7FA` - Grigio chiaro (background)
- `--gmi-white: #FFFFFF` - Bianco
- `--gmi-text: #2C3E50` - Testo principale

**File aggiornati:**
- ✅ `src/styles.css` - Colori globali
- ✅ `src/app/components/navbar/navbar.component.css` - Navbar brandizzata
- ✅ Gradient blu nei pulsanti e navbar
- ✅ Accenti oro/giallo per elementi interattivi

### 3. Font Tipografico
- ✅ Font professionale: `'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif`
- ✅ Applicato in tutta l'applicazione

### 4. Logo Placeholder
- ✅ Placeholder "GMI" nella navbar con stile brand
- ✅ Brand text con nome azienda e app
- ⏳ **DA FARE:** Sostituire con logo reale (vedi istruzioni sotto)

---

## 📋 Come Aggiungere il Logo Reale

### Passo 1: Preparare il Logo
1. Il logo deve essere in formato **PNG** o **SVG** con sfondo trasparente
2. Dimensione consigliata: **200x50px** o simile (larghezza: 150-250px, altezza: 40-60px)
3. Nome file consigliato: `grandi-molini-italia-logo.png` o `gmi-logo.svg`

### Passo 2: Copiare il Logo nella Cartella Assets
```bash
# Copia il logo in:
frontend/src/assets/images/grandi-molini-italia-logo.png
```

### Passo 3: Aggiornare la Navbar
Apri il file `frontend/src/app/components/navbar/navbar.component.html` e sostituisci:

```html
<!-- DA QUESTO: -->
<div class="logo-placeholder">GMI</div>

<!-- A QUESTO: -->
<img
  src="/assets/images/grandi-molini-italia-logo.png"
  alt="Grandi Molini Italia Logo"
  class="logo-image">
```

### Passo 4: Aggiungere CSS per il Logo
Apri `frontend/src/app/components/navbar/navbar.component.css` e aggiungi dopo `.logo-placeholder`:

```css
.logo-image {
  height: 50px;
  width: auto;
  object-fit: contain;
}
```

### Passo 5: (Opzionale) Rimuovere il Placeholder
Rimuovi o commenta le regole CSS per `.logo-placeholder` se non più necessarie.

---

## 🌍 Come Usare il Sistema Multi-Lingua

### Nel Codice TypeScript
```typescript
import { TranslationService } from './services/translation.service';

constructor(private translationService: TranslationService) {}

// Ottenere una traduzione
const text = this.translationService.translate('nav.dashboard');

// Cambiare lingua
this.translationService.setLanguage('en'); // o 'it'

// Ottenere lingua corrente
const currentLang = this.translationService.getLanguage();
```

### Nei Template HTML
```html
<!-- Usare la pipe translate -->
<h1>{{ 'dashboard.title' | translate }}</h1>
<p>{{ 'dashboard.welcome' | translate }}</p>

<!-- Per testi dinamici -->
<span>{{ dynamicKey | translate }}</span>
```

### Aggiungere Nuove Traduzioni
1. Apri `src/assets/i18n/it.json`
2. Aggiungi la chiave con il testo italiano:
```json
{
  "nuovaSezione": {
    "titolo": "Il Mio Titolo",
    "descrizione": "La mia descrizione"
  }
}
```
3. Apri `src/assets/i18n/en.json`
4. Aggiungi la stessa chiave con il testo inglese:
```json
{
  "nuovaSezione": {
    "titolo": "My Title",
    "descrizione": "My description"
  }
}
```
5. Usa nel template: `{{ 'nuovaSezione.titolo' | translate }}`

---

## 🎨 Personalizzazione Colori

Se i colori brand devono essere modificati, aggiorna i valori in:

**File:** `frontend/src/styles.css`
```css
:root {
  --gmi-primary: #003366;     /* Cambia qui il blu primario */
  --gmi-secondary: #0066CC;   /* Cambia qui il blu secondario */
  --gmi-accent: #D4AF37;      /* Cambia qui il colore accent */
  /* ... */
}
```

I colori verranno applicati automaticamente in tutta l'applicazione.

---

## 📦 File Creati/Modificati

### Nuovi File:
- ✅ `src/app/services/translation.service.ts`
- ✅ `src/app/pipes/translate.pipe.ts`
- ✅ `src/assets/i18n/it.json`
- ✅ `src/assets/i18n/en.json`
- ✅ `src/assets/images/` (cartella per logo)

### File Modificati:
- ✅ `src/app/components/navbar/navbar.component.ts`
- ✅ `src/app/components/navbar/navbar.component.html`
- ✅ `src/app/components/navbar/navbar.component.css`
- ✅ `src/styles.css`

---

## ✨ Funzionalità Implementate

### Navbar Brandizzata
- Logo/placeholder GMI con sfondo oro
- Nome azienda "Grandi Molini Italia"
- Sottotitolo "AIA Management System"
- Language switcher IT/EN
- Colori brand in gradient blu
- Pulsanti con hover effect oro

### Traduzioni Complete
- Tutte le voci di menu
- Titoli e labels
- Messaggi di sistema
- Form fields
- Azioni (logout, save, delete, etc.)
- Tipi di documento

### Design Responsive
- Layout adattivo per mobile/tablet/desktop
- Menu collassabile
- Language switcher sempre visibile
- User info ottimizzata per piccoli schermi

---

## 🚀 Test

Per testare il branding e le traduzioni:

1. **Avvia il frontend:**
   ```bash
   cd frontend
   npm install
   ng serve
   ```

2. **Verifica:**
   - ✅ La navbar mostra i colori brand blu/oro
   - ✅ Il logo placeholder "GMI" è visibile
   - ✅ Il language switcher funziona (click su IT/EN)
   - ✅ Le traduzioni cambiano correttamente
   - ✅ La preferenza lingua viene salvata (ricarica pagina)

---

## 📞 Supporto

Per personalizzazioni ulteriori:
- Colori: modifica `src/styles.css` variabili CSS
- Traduzioni: aggiungi voci in `src/assets/i18n/*.json`
- Logo: segui le istruzioni nella sezione "Come Aggiungere il Logo Reale"

---

**Versione:** 1.0
**Data:** Febbraio 2026
**Brand:** Grandi Molini Italia
