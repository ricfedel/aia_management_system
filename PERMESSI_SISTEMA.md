# Matrice Permessi - Sistema AIA Management

## Versione 1.1 - Aggiornato dopo revisione permessi operativi

---

## 📊 Tabella Completa Permessi

| Operazione | ADMIN | RESPONSABILE | OPERATORE | Note |
|-----------|:-----:|:------------:|:---------:|------|
| **PRESCRIZIONI** | | | | |
| Visualizza | ✅ | ✅ | ✅ | Tutti possono consultare le prescrizioni AIA |
| Crea/Modifica | ✅ | ✅ | ❌ | Solo chi gestisce può modificare prescrizioni normative |
| Elimina | ✅ | ❌ | ❌ | Solo ADMIN per sicurezza (audit trail) |
| **SCADENZE** | | | | |
| Visualizza | ✅ | ✅ | ✅ | Dashboard scadenze visibile a tutti |
| Crea | ✅ | ✅ | ❌ | ADMIN/RESPONSABILE pianificano le scadenze |
| Completa (stato) | ✅ | ✅ | ✅ | **TUTTI** possono completare scadenze operative |
| Modifica (tutti campi) | ✅ | ✅ | ❌ | Solo gestori modificano dettagli scadenza |
| Elimina | ✅ | ✅ | ❌ | ADMIN/RESPONSABILE eliminano scadenze |
| **DATI AMBIENTALI** | | | | |
| Visualizza | ✅ | ✅ | ✅ | Dashboard monitoraggio visibile a tutti |
| Crea/Modifica | ✅ | ✅ | ✅ | **OPERATORI inseriscono dati giornalieri** |
| Elimina | ✅ | ✅ | ❌ | Eliminazione richiede supervisione |
| **DOCUMENTI** | | | | |
| Visualizza/Download | ✅ | ✅ | ✅ | Accesso documenti per tutti |
| Upload | ✅ | ✅ | ✅ | **OPERATORI caricano certificati laboratorio** |
| Modifica metadati | ✅ | ✅ | ❌ | Gestione metadati richiede supervisione |
| Elimina | ✅ | ✅ | ❌ | Eliminazione richiede supervisione |
| **UTENTI** | | | | |
| Visualizza tutti | ✅ | ❌ | ❌ | Solo ADMIN vede lista completa utenti |
| Crea/Modifica/Elimina | ✅ | ❌ | ❌ | Gestione utenti esclusiva ADMIN |
| Visualizza profilo proprio | ✅ | ✅ | ✅ | Tutti vedono il proprio profilo |
| **EXPORT EXCEL** | | | | |
| Prescrizioni | ✅ | ✅ | ✅ | Export disponibile per report e audit |
| Scadenze | ✅ | ✅ | ✅ | Export per pianificazione |
| Dati Ambientali | ✅ | ✅ | ✅ | Export per relazioni tecniche |
| Utenti | ✅ | ❌ | ❌ | Dati sensibili - solo ADMIN |
| **DASHBOARD** | | | | |
| KPI Generali | ✅ | ✅ | ✅ | Dashboard analytics per tutti |
| Stats Stabilimento | ✅ | ✅ (assegnati) | ✅ (assegnati) | Filtrate per stabilimenti assegnati |

---

## 🔄 Workflow Operativi Tipici

### 👷 OPERATORE - Operatività Quotidiana

```
1. Login al sistema
2. Consulta Dashboard per overview giornaliera
3. Accede a "Dati Ambientali"
   → Inserisce nuovi dati monitoraggio (pH, COD, BOD, emissioni, etc.)
   → Sistema calcola automaticamente conformità
4. Accede a "Documenti"
   → Carica certificati analitici ricevuti dal laboratorio
   → Associa documento allo stabilimento e anno di riferimento
5. Verifica "Scadenze" imminenti
   → Completa scadenze operative quando adempimenti sono eseguiti
   → Click pulsante "✓ Completa"
6. Consulta "Prescrizioni" per verifiche normative
7. Export Excel dei dati per report interni
```

**Caso d'uso**: Operatore stabilimento carica giornalmente i dati del campionamento acque reflue e marca come completata la scadenza "Campionamento mensile acque".

---

### 👔 RESPONSABILE - Gestione e Supervisione

```
TUTTO quello che fa l'OPERATORE +

1. Gestisce "Prescrizioni" AIA
   → Crea nuove prescrizioni da autorizzazione
   → Aggiorna stati (APERTA → IN_CORSO → COMPLETATA)
   → Modifica priorità e matrici ambientali

2. Pianifica "Scadenze"
   → Crea scadenze per comunicazioni, verifiche, controlli
   → Imposta notifiche email con giorni preavviso
   → Assegna responsabili

3. Supervisione dati ambientali
   → Verifica dati inseriti dagli operatori
   → Corregge eventuali errori
   → Elimina dati duplicati o errati

4. Gestione documentale avanzata
   → Modifica metadati documenti
   → Riorganizza archivio documentale
   → Elimina documenti obsoleti

5. Report e analisi
   → Export Excel per relazioni tecniche
   → Dashboard analytics stabilimenti assegnati
```

**Caso d'uso**: Responsabile ambientale riceve nuova AIA con prescrizioni aggiornate, le inserisce nel sistema, crea le scadenze per le verifiche periodiche previste e pianifica le notifiche ai responsabili di stabilimento.

---

### 🔑 ADMIN - Amministrazione Sistema

```
TUTTO quello che fanno OPERATORE + RESPONSABILE +

1. Gestione Utenti
   → Crea nuovi account (username, email, password, ruolo)
   → Assegna stabilimenti agli utenti
   → Disattiva/riattiva account
   → Reset password

2. Accesso globale
   → Visualizza dati di TUTTI gli stabilimenti
   → Export completo dati utenti

3. Configurazione sistema
   → Parametri applicazione
   → Gestione backup
   → Monitoring e troubleshooting

4. Sicurezza
   → Audit log accessi
   → Gestione permessi
   → Configurazione JWT
```

**Caso d'uso**: ADMIN crea account per nuovo operatore assunto nello stabilimento di Verona, assegna ruolo OPERATORE, configura accesso solo allo stabilimento di Verona, genera password temporanea.

---

## 🎯 Principi di Design Permessi

### 1. **Least Privilege**
Gli utenti hanno solo i permessi necessari per svolgere il proprio lavoro.

### 2. **Separazione dei Compiti**
- **OPERATORI**: Inserimento dati operativi giornalieri
- **RESPONSABILE**: Gestione prescrizioni, pianificazione, supervisione
- **ADMIN**: Amministrazione sistema e utenti

### 3. **Tracciabilità**
- Eliminazioni limitate a ADMIN/RESPONSABILE per mantenere audit trail
- Ogni operazione logga l'utente che l'ha eseguita

### 4. **Operatività**
- **OPERATORI devono poter lavorare autonomamente** senza blocchi
- Upload documenti e inserimento dati ambientali sono operazioni quotidiane
- Completamento scadenze è operazione operativa (non gestionale)

### 5. **Protezione Dati Sensibili**
- Gestione utenti e dati personali riservata ad ADMIN
- Export utenti riservato ad ADMIN (GDPR compliance)

---

## 🔒 Implementazione Tecnica

### Backend Security (@PreAuthorize)

```java
// Dati Ambientali - OPERATORI possono creare/modificare
@PostMapping
@PreAuthorize("@stabilimentoAccessChecker.canEditDatiAmbientali()")
public ResponseEntity<DatiAmbientaliDTO> createDati(...) { ... }

// Documenti - OPERATORI possono caricare
@PostMapping("/upload")
@PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")
public ResponseEntity<FileUploadResponse> uploadDocumento(...) { ... }

// Scadenze - TUTTI possono aggiornare (completare)
@PutMapping("/{id}")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<ScadenzaDTO> updateScadenza(...) { ... }

// Prescrizioni - Solo ADMIN/RESPONSABILE possono creare
@PostMapping
@PreAuthorize("@stabilimentoAccessChecker.isResponsabileOrAdmin()")
public ResponseEntity<PrescrizioneDTO> createPrescrizione(...) { ... }
```

### Frontend Permissions (Angular Signals)

```typescript
// Dati Ambientali Component
canEdit = computed(() => {
  const user = this.authService.currentUser.value;
  return user?.ruolo === 'ADMIN' ||
         user?.ruolo === 'RESPONSABILE' ||
         user?.ruolo === 'OPERATORE'; // ✅ OPERATORE può editare
});

canDelete = computed(() => {
  const user = this.authService.currentUser.value;
  return user?.ruolo === 'ADMIN' || user?.ruolo === 'RESPONSABILE';
});

// Scadenze Component
isCompletable(stato: string): boolean {
  return stato === 'DA_FARE' || stato === 'IN_CORSO'; // ✅ Tutti possono completare
}

// Documenti Component
canUpload = computed(() => {
  const user = this.authService.currentUser.value;
  return user?.ruolo === 'ADMIN' ||
         user?.ruolo === 'RESPONSABILE' ||
         user?.ruolo === 'OPERATORE'; // ✅ OPERATORE può caricare
});
```

---

## ❓ FAQ Permessi

### Q: Perché gli OPERATORI possono modificare i dati ambientali?
**A**: Gli operatori sono responsabili dell'inserimento giornaliero dei dati di monitoraggio. Bloccarli richiederebbe intervento continuo di ADMIN/RESPONSABILE per ogni singolo dato, rendendo il sistema inutilizzabile operativamente.

### Q: Perché tutti possono completare le scadenze?
**A**: Il completamento di una scadenza è un'operazione di segnalazione ("ho finito questo adempimento"). Non modifica dati critici, ma aggiorna solo lo stato operativo. La verifica finale spetta comunque al RESPONSABILE.

### Q: Perché gli OPERATORI non possono eliminare dati?
**A**: Per tracciabilità e audit. Le eliminazioni devono essere supervisionate per evitare perdita accidentale di dati o manomissione. Gli errori si correggono con modifiche, non eliminazioni.

### Q: Gli OPERATORI possono vedere i dati di altri stabilimenti?
**A**: No. Gli utenti (inclusi OPERATORI) vedono solo i dati degli stabilimenti assegnati al loro account. L'ADMIN vede tutti gli stabilimenti.

### Q: Come si gestiscono i dati inseriti erroneamente?
**A**: Gli OPERATORI possono modificare i propri dati. Per eliminazioni, devono richiedere intervento di RESPONSABILE o ADMIN.

---

## 📝 Change Log

### v1.1 - 2026-02-09
- ✅ **OPERATORE può creare/modificare Dati Ambientali**
- ✅ **OPERATORE può caricare Documenti**
- ✅ **OPERATORE può completare Scadenze**
- ✅ Aggiunto metodo `canEditDatiAmbientali()` in StabilimentoAccessChecker
- ✅ Aggiunto metodo `canUploadDocumenti()` in StabilimentoAccessChecker
- ✅ Modificato `@PreAuthorize` in DatiAmbientaliController (POST/PUT)
- ✅ Modificato `@PreAuthorize` in ScadenzaController (PUT)
- ✅ Aggiornati computed signals frontend per tutti i componenti CRUD

### v1.0 - 2026-02-08
- Initial release con permessi base ADMIN/RESPONSABILE/OPERATORE

---

**Documento di riferimento**: Documento_Funzionale_Operativo_AIA_Management.docx
**Autore**: Sistema AIA Management Team
**Ultima modifica**: 2026-02-09
