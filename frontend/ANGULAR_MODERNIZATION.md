# 🚀 Angular 19 Modernization Guide

## ✅ Modernizzazioni Applicate

Il progetto è stato aggiornato per utilizzare le API moderne di **Angular 19** con **Signals**, **Dependency Injection funzionale** e gestione automatica delle subscription.

---

## 📦 Pattern Moderni Implementati

### 1. **Signals per State Management**

Sostituito le variabili tradizionali con **signals** per reactive state management:

**Prima (Angular tradizionale):**
```typescript
export class MyComponent {
  data: string[] = [];
  loading = false;

  constructor() {
    // ...
  }
}
```

**Dopo (Angular 19 con Signals):**
```typescript
export class MyComponent {
  data = signal<string[]>([]);
  loading = signal(false);

  constructor() {
    // Set values con .set()
    this.data.set(['item1', 'item2']);
    this.loading.set(true);

    // Update con .update()
    this.data.update(items => [...items, 'new item']);
  }
}
```

**Nel template:**
```html
<!-- Usare la call syntax () per accedere al valore -->
@if (loading()) {
  <div>Loading...</div>
}
@for (item of data(); track item.id) {
  <div>{{ item }}</div>
}
```

---

### 2. **Nuova Sintassi Control Flow (@if, @for, @switch)**

Sostituito le vecchie direttive `*ngIf`, `*ngFor`, `*ngSwitch` con la nuova sintassi più performante:

**Prima (*ngIf / *ngFor):**
```html
<div *ngIf="isVisible">Visibile</div>
<div *ngIf="user; else loading">{{ user.name }}</div>

<ng-template #loading>
  <div>Loading...</div>
</ng-template>

<div *ngFor="let item of items">{{ item }}</div>
<div *ngFor="let item of items; trackBy: trackById">{{ item }}</div>
```

**Dopo (@if / @for):**
```html
<!-- @if con @else -->
@if (isVisible) {
  <div>Visibile</div>
}

<!-- @if con @else block -->
@if (user) {
  <div>{{ user.name }}</div>
} @else {
  <div>Loading...</div>
}

<!-- @for con track obbligatorio -->
@for (item of items; track item.id) {
  <div>{{ item }}</div>
}

<!-- @for con @empty per lista vuota -->
@for (item of items; track item.id) {
  <div>{{ item }}</div>
} @empty {
  <div>Nessun elemento trovato</div>
}

<!-- @for con index e other variables -->
@for (item of items; track item.id; let idx = $index; let isFirst = $first) {
  <div>{{ idx }}: {{ item }}</div>
}
```

**@switch:**
```html
@switch (status) {
  @case ('pending') {
    <div>In attesa...</div>
  }
  @case ('success') {
    <div>Completato!</div>
  }
  @default {
    <div>Sconosciuto</div>
  }
}
```

**Vantaggi della nuova sintassi:**
- ✅ Più performante (no direttive strutturali)
- ✅ Type-safe (migliore inferenza TypeScript)
- ✅ Più leggibile e concisa
- ✅ Track obbligatorio in @for (previene bug)
- ✅ @empty built-in per liste vuote
- ✅ No necessità di `<ng-template>`

---

### 3. **Computed Signals**

Per valori derivati da altri signals:

```typescript
export class MyComponent {
  items = signal<Item[]>([]);

  // Computed signal - si aggiorna automaticamente
  itemCount = computed(() => this.items().length);
  hasItems = computed(() => this.items().length > 0);

  // Usabile nel template come items
  // {{ itemCount() }}
  // *ngIf="hasItems()"
}
```

---

### 3. **Dependency Injection Funzionale con `inject()`**

Sostituito il constructor DI con la funzione `inject()`:

**Prima:**
```typescript
export class MyComponent {
  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}
}
```

**Dopo:**
```typescript
export class MyComponent {
  private readonly apiService = inject(ApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    // Constructor è ora solo per inizializzazione
  }
}
```

**Vantaggi:**
- ✅ Codice più conciso
- ✅ Migliore tree-shaking
- ✅ Più facile da testare
- ✅ Più flessibile (può essere chiamato ovunque nel context di injection)

---

### 4. **Gestione Automatica Subscriptions con `takeUntilDestroyed()`**

Eliminato `ngOnDestroy()` e manual unsubscribe:

**Prima:**
```typescript
export class MyComponent implements OnDestroy {
  private subscription: Subscription;

  constructor(private service: MyService) {
    this.subscription = this.service.getData().subscribe(data => {
      // handle data
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
```

**Dopo:**
```typescript
export class MyComponent {
  private readonly service = inject(MyService);
  private readonly destroyRef = inject(DestroyRef);

  data = signal<Data[]>([]);

  constructor() {
    this.service.getData()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.data.set(data));
  }

  // ngOnDestroy non più necessario!
}
```

**Vantaggi:**
- ✅ Nessun memory leak
- ✅ Codice più pulito
- ✅ Meno boilerplate
- ✅ Automatic cleanup quando il componente viene distrutto

---

### 5. **Effects con `effect()`**

Per side effects reattivi:

```typescript
export class MyComponent {
  searchTerm = signal('');
  results = signal<Result[]>([]);

  constructor() {
    // Effect si esegue ogni volta che searchTerm cambia
    effect(() => {
      const term = this.searchTerm();
      console.log('Search term changed:', term);

      // Esegui side effect (es. analytics)
      this.analytics.track('search', term);
    });
  }
}
```

---

### 6. **Two-Way Binding con Signals**

Per forms con signals:

**Prima (con ngModel):**
```html
<input [(ngModel)]="uploadData.name" name="name">
```

**Dopo (con signals):**
```html
<input
  [ngModel]="uploadData().name"
  (ngModelChange)="uploadData.update(d => ({...d, name: $event}))"
  name="name">
```

O usando signal direttamente:
```typescript
name = signal('');

// Nel template:
<input [ngModel]="name()" (ngModelChange)="name.set($event)">
```

---

## 📂 Componenti Modernizzati

### ✅ NavbarComponent

**Cambiamenti:**
- ✅ `inject()` per DI invece di constructor
- ✅ `signal()` per `currentUser` e `currentLanguage`
- ✅ `computed()` per `isAdmin`
- ✅ `effect()` per logging language changes
- ✅ `takeUntilDestroyed()` per subscriptions
- ✅ Template aggiornato con call syntax `()`

**Prima:**
```typescript
export class NavbarComponent {
  currentUser: User | null = null;
  currentLanguage: Language = 'it';

  constructor(
    public authService: AuthService,
    public translationService: TranslationService,
    private router: Router
  ) {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
    });
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }
}
```

**Dopo:**
```typescript
export class NavbarComponent {
  private readonly authService = inject(AuthService);
  private readonly translationService = inject(TranslationService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  currentUser = signal<User | null>(null);
  currentLanguage = signal<Language>('it');

  isAdmin = computed(() => {
    const user = this.currentUser();
    return user?.ruolo === 'ADMIN';
  });

  constructor() {
    this.authService.currentUser
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => this.currentUser.set(user));

    effect(() => {
      console.log('Language changed to:', this.currentLanguage());
    });
  }
}
```

---

### ✅ DocumentiListComponent

**Cambiamenti:**
- ✅ `inject()` per tutti i servizi
- ✅ Tutti gli state variables convertiti a signals
- ✅ `computed()` per `canEdit` e `canDelete`
- ✅ `takeUntilDestroyed()` per tutte le subscriptions
- ✅ Template aggiornato con signal syntax
- ✅ Form binding con `update()` invece di two-way binding

**Signals implementati:**
```typescript
documenti = signal<Documento[]>([]);
stabilimenti = signal<Stabilimento[]>([]);
loading = signal(true);
showUploadForm = signal(false);
uploading = signal(false);
uploadProgress = signal(0);
selectedFile = signal<File | null>(null);
uploadData = signal({...});

canEdit = computed(() => this.authService.canEdit());
canDelete = computed(() => this.authService.canDelete());
```

**Subscription pattern:**
```typescript
loadDocumenti() {
  this.loading.set(true);
  this.apiService.searchDocumenti({})
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe({
      next: (result) => {
        this.documenti.set(result.content);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Errore:', error);
        this.loading.set(false);
      }
    });
}
```

---

## 🎯 Best Practices

### 1. **Signal Naming**
- Usa nomi descrittivi senza suffisso `$` (riservato per Observables)
- Esempio: `data` invece di `data$` per signals

### 2. **Computed Signals**
- Usa `computed()` per valori derivati
- Non fare calcoli pesanti nei template
- Esempio: `filteredItems = computed(() => this.items().filter(...))`

### 3. **Effects**
- Usa `effect()` solo per side effects (logging, analytics, DOM manipulation)
- NON usare per data fetching (usa subscriptions)
- Evita di modificare altri signals dentro effects (può causare loop)

### 4. **Subscriptions**
- Usa sempre `takeUntilDestroyed(this.destroyRef)`
- Evita manual unsubscribe quando possibile
- Usa `pipe()` per operators RxJS

### 5. **Template Syntax**
- Usa sempre `()` per accedere ai signal values nel template
- Esempio: `*ngIf="loading()"` invece di `*ngIf="loading"`

---

## 🔄 Pattern di Migrazione

### Migrare un Component Tradizionale

**Step 1:** Converti Constructor DI a `inject()`
```typescript
// Prima
constructor(private service: MyService) {}

// Dopo
private readonly service = inject(MyService);
private readonly destroyRef = inject(DestroyRef);
```

**Step 2:** Converti State a Signals
```typescript
// Prima
data: string[] = [];
loading = false;

// Dopo
data = signal<string[]>([]);
loading = signal(false);
```

**Step 3:** Converti Getters a Computed
```typescript
// Prima
get hasData(): boolean {
  return this.data.length > 0;
}

// Dopo
hasData = computed(() => this.data().length > 0);
```

**Step 4:** Aggiungi `takeUntilDestroyed()`
```typescript
// Prima
this.service.getData().subscribe(data => {
  this.data = data;
});

// Dopo
this.service.getData()
  .pipe(takeUntilDestroyed(this.destroyRef))
  .subscribe(data => this.data.set(data));
```

**Step 5:** Aggiorna Template
```html
<!-- Prima -->
<div *ngIf="loading">Loading...</div>
<div *ngFor="let item of data">{{ item }}</div>

<!-- Dopo -->
<div *ngIf="loading()">Loading...</div>
<div *ngFor="let item of data()">{{ item }}</div>
```

---

## 📚 Risorse

- [Angular Signals Documentation](https://angular.dev/guide/signals)
- [Dependency Injection with inject()](https://angular.dev/guide/di/dependency-injection-context)
- [RxJS Interop with takeUntilDestroyed](https://angular.dev/api/core/rxjs-interop/takeUntilDestroyed)
- [Computed Signals](https://angular.dev/guide/signals#computed-signals)
- [Effects](https://angular.dev/guide/signals#effects)

---

## ✨ Vantaggi della Modernizzazione

1. **Performance**: Signals offrono change detection granulare
2. **Developer Experience**: Codice più pulito e leggibile
3. **Type Safety**: Migliore inferenza dei tipi con TypeScript
4. **Memory Safety**: Automatic cleanup delle subscriptions
5. **Future-Proof**: Allineato con la direction futura di Angular
6. **Bundle Size**: Migliore tree-shaking con inject()

---

**Versione:** 1.0
**Data:** Febbraio 2026
**Angular Version:** 19.2
