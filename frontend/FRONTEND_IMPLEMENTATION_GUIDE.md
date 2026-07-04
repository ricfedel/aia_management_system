# Frontend Angular - Guida Implementazione Completa

## Stato Attuale

### ✅ Completato

1. **Models** (7 file)
   - `user.model.ts` - User, LoginRequest, RegisterRequest, AuthResponse
   - `documento.model.ts` - Documento, TipoDocumento, Upload/Search
   - `dashboard.model.ts` - DashboardStats, StabilimentoStats, Trends
   - `stabilimento.model.ts` - Stabilimento interface
   - `prescrizione.model.ts` - Prescrizione, MatriceAmbientale, StatoPrescrizione
   - `scadenza.model.ts` - Scadenza, TipoScadenza, StatoScadenza
   - `dati-ambientali.model.ts` - DatiAmbientali, StatoConformita

2. **Services** (2 file)
   - `auth.service.ts` - Autenticazione completa con JWT
   - `api.service.ts` - API base (da espandere)

3. **Interceptors** (1 file)
   - `jwt.interceptor.ts` - Gestione automatica JWT e refresh token

4. **Guards** (2 file)
   - `auth.guard.ts` - Protezione routes autenticate
   - `admin.guard.ts` - Protezione routes solo ADMIN

5. **Components** (2 file)
   - `login.component` - Login completo (TS, HTML, CSS)
   - `dashboard.component` - Dashboard base (da migliorare)

---

## 🚧 Da Implementare

### 1. Aggiornamento Package.json

Aggiungi dipendenze per grafici e UI:

\`\`\`json
{
  "dependencies": {
    "@angular/common": "^19.2.0",
    "@angular/compiler": "^19.2.0",
    "@angular/core": "^19.2.0",
    "@angular/forms": "^19.2.0",
    "@angular/platform-browser": "^19.2.0",
    "@angular/platform-browser-dynamic": "^19.2.0",
    "@angular/router": "^19.2.0",
    "chart.js": "^4.4.0",
    "ng2-charts": "^6.0.0",
    "rxjs": "~7.8.0",
    "tslib": "^2.3.0",
    "zone.js": "~0.15.0"
  }
}
\`\`\`

**Comando:**
\`\`\`bash
npm install chart.js ng2-charts --save
\`\`\`

---

### 2. Configurazione App (app.config.ts)

Aggiorna `src/app/app.config.ts` per includere interceptor e HTTP:

\`\`\`typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { jwtInterceptor } from './interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([jwtInterceptor])
    )
  ]
};
\`\`\`

**Nota:** Adatta `jwtInterceptor` per Angular 19 functional interceptors se necessario.

---

### 3. Routing Completo (app.routes.ts)

\`\`\`typescript
import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { LoginComponent } from './components/auth/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { StabilimentiListComponent } from './components/stabilimenti/stabilimenti-list.component';
import { StabilimentoDetailComponent } from './components/stabilimenti/stabilimento-detail.component';
import { PrescrizioniListComponent } from './components/prescrizioni/prescrizioni-list.component';
import { ScadenzeListComponent } from './components/scadenze/scadenze-list.component';
import { DocumentiListComponent } from './components/documenti/documenti-list.component';
import { UsersListComponent } from './components/users/users-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'stabilimenti',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: StabilimentiListComponent },
      { path: ':id', component: StabilimentoDetailComponent }
    ]
  },
  {
    path: 'prescrizioni',
    component: PrescrizioniListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'scadenze',
    component: ScadenzeListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'documenti',
    component: DocumentiListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'users',
    component: UsersListComponent,
    canActivate: [AuthGuard, AdminGuard],
    data: { roles: ['ADMIN'] }
  },
  { path: '**', redirectTo: '/dashboard' }
];
\`\`\`

---

### 4. API Service Completo

Espandi `src/app/services/api.service.ts`:

\`\`\`typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// Import models
import { Stabilimento } from '../models/stabilimento.model';
import { Prescrizione } from '../models/prescrizione.model';
import { DatiAmbientali } from '../models/dati-ambientali.model';
import { Scadenza } from '../models/scadenza.model';
import { Documento, DocumentoSearchParams, DocumentoSearchResult } from '../models/documento.model';
import { DashboardStats, StabilimentoStats, ScadenzaImminente, ConformitaTrend } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // ========== STABILIMENTI ==========
  getStabilimenti(): Observable<Stabilimento[]> {
    return this.http.get<Stabilimento[]>(\`\${this.apiUrl}/stabilimenti\`);
  }

  getStabilimento(id: number): Observable<Stabilimento> {
    return this.http.get<Stabilimento>(\`\${this.apiUrl}/stabilimenti/\${id}\`);
  }

  createStabilimento(stabilimento: Stabilimento): Observable<Stabilimento> {
    return this.http.post<Stabilimento>(\`\${this.apiUrl}/stabilimenti\`, stabilimento);
  }

  updateStabilimento(id: number, stabilimento: Stabilimento): Observable<Stabilimento> {
    return this.http.put<Stabilimento>(\`\${this.apiUrl}/stabilimenti/\${id}\`, stabilimento);
  }

  deleteStabilimento(id: number): Observable<void> {
    return this.http.delete<void>(\`\${this.apiUrl}/stabilimenti/\${id}\`);
  }

  // ========== PRESCRIZIONI ==========
  getPrescrizioni(): Observable<Prescrizione[]> {
    return this.http.get<Prescrizione[]>(\`\${this.apiUrl}/prescrizioni\`);
  }

  getPrescrizione(id: number): Observable<Prescrizione> {
    return this.http.get<Prescrizione>(\`\${this.apiUrl}/prescrizioni/\${id}\`);
  }

  getPrescrizioniByStabilimento(stabilimentoId: number): Observable<Prescrizione[]> {
    return this.http.get<Prescrizione[]>(\`\${this.apiUrl}/prescrizioni/stabilimento/\${stabilimentoId}\`);
  }

  createPrescrizione(prescrizione: Prescrizione): Observable<Prescrizione> {
    return this.http.post<Prescrizione>(\`\${this.apiUrl}/prescrizioni\`, prescrizione);
  }

  updatePrescrizione(id: number, prescrizione: Prescrizione): Observable<Prescrizione> {
    return this.http.put<Prescrizione>(\`\${this.apiUrl}/prescrizioni/\${id}\`, prescrizione);
  }

  deletePrescrizione(id: number): Observable<void> {
    return this.http.delete<void>(\`\${this.apiUrl}/prescrizioni/\${id}\`);
  }

  // ========== SCADENZE ==========
  getScadenze(): Observable<Scadenza[]> {
    return this.http.get<Scadenza[]>(\`\${this.apiUrl}/scadenze\`);
  }

  getScadenza(id: number): Observable<Scadenza> {
    return this.http.get<Scadenza>(\`\${this.apiUrl}/scadenze/\${id}\`);
  }

  getScadenzeProssimi30Giorni(): Observable<Scadenza[]> {
    return this.http.get<Scadenza[]>(\`\${this.apiUrl}/scadenze/prossimi-30-giorni\`);
  }

  getScadenzeByStabilimento(stabilimentoId: number): Observable<Scadenza[]> {
    return this.http.get<Scadenza[]>(\`\${this.apiUrl}/scadenze/stabilimento/\${stabilimentoId}\`);
  }

  createScadenza(scadenza: Scadenza): Observable<Scadenza> {
    return this.http.post<Scadenza>(\`\${this.apiUrl}/scadenze\`, scadenza);
  }

  updateScadenza(id: number, scadenza: Scadenza): Observable<Scadenza> {
    return this.http.put<Scadenza>(\`\${this.apiUrl}/scadenze/\${id}\`, scadenza);
  }

  deleteScadenza(id: number): Observable<void> {
    return this.http.delete<void>(\`\${this.apiUrl}/scadenze/\${id}\`);
  }

  // ========== DATI AMBIENTALI ==========
  getDatiAmbientali(): Observable<DatiAmbientali[]> {
    return this.http.get<DatiAmbientali[]>(\`\${this.apiUrl}/dati-ambientali\`);
  }

  getDatiNonConformi(): Observable<DatiAmbientali[]> {
    return this.http.get<DatiAmbientali[]>(\`\${this.apiUrl}/dati-ambientali/non-conformi\`);
  }

  getDatiByStabilimentoAndAnno(stabilimentoId: number, anno: number): Observable<DatiAmbientali[]> {
    return this.http.get<DatiAmbientali[]>(\`\${this.apiUrl}/dati-ambientali/stabilimento/\${stabilimentoId}/anno/\${anno}\`);
  }

  // ========== DOCUMENTI ==========
  uploadDocumento(formData: FormData): Observable<Documento> {
    return this.http.post<Documento>(\`\${this.apiUrl}/documenti/upload\`, formData);
  }

  getDocumento(id: number): Observable<Documento> {
    return this.http.get<Documento>(\`\${this.apiUrl}/documenti/\${id}\`);
  }

  downloadDocumento(id: number): Observable<Blob> {
    return this.http.get(\`\${this.apiUrl}/documenti/\${id}/download\`, {
      responseType: 'blob'
    });
  }

  searchDocumenti(params: DocumentoSearchParams): Observable<DocumentoSearchResult> {
    let httpParams = new HttpParams();
    
    if (params.nome) httpParams = httpParams.set('nome', params.nome);
    if (params.tipo) httpParams = httpParams.set('tipo', params.tipo);
    if (params.stabilimentoId) httpParams = httpParams.set('stabilimentoId', params.stabilimentoId.toString());
    if (params.anno) httpParams = httpParams.set('anno', params.anno.toString());
    if (params.dataInizio) httpParams = httpParams.set('dataInizio', params.dataInizio);
    if (params.dataFine) httpParams = httpParams.set('dataFine', params.dataFine);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<DocumentoSearchResult>(\`\${this.apiUrl}/documenti/search\`, { params: httpParams });
  }

  deleteDocumento(id: number): Observable<void> {
    return this.http.delete<void>(\`\${this.apiUrl}/documenti/\${id}\`);
  }

  // ========== DASHBOARD ==========
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(\`\${this.apiUrl}/dashboard/stats\`);
  }

  getStabilimentoStats(stabilimentoId: number): Observable<StabilimentoStats> {
    return this.http.get<StabilimentoStats>(\`\${this.apiUrl}/dashboard/stabilimento/\${stabilimentoId}/stats\`);
  }

  getScadenzeImminenti(giorni: number = 30): Observable<ScadenzaImminente[]> {
    return this.http.get<ScadenzaImminente[]>(\`\${this.apiUrl}/dashboard/scadenze-imminenti\`, {
      params: { giorni: giorni.toString() }
    });
  }

  getConformitaTrend(mesi: number = 12): Observable<ConformitaTrend[]> {
    return this.http.get<ConformitaTrend[]>(\`\${this.apiUrl}/dashboard/conformita-trend\`, {
      params: { mesi: mesi.toString() }
    });
  }
}
\`\`\`

---

### 5. Environment Configuration

Crea `src/environments/environment.ts`:

\`\`\`typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
\`\`\`

Crea `src/environments/environment.prod.ts`:

\`\`\`typescript
export const environment = {
  production: true,
  apiUrl: '/api' // Usa proxy Nginx in produzione
};
\`\`\`

---

### 6. Componenti Da Creare

#### 6.1 Navbar Component

**File:** `src/app/components/navbar/navbar.component.ts`

\`\`\`typescript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  currentUser: User | null = null;

  constructor(
    public authService: AuthService,
    private router: Router
  ) {
    this.authService.currentUser.subscribe(user => {
      this.currentUser = user;
    });
  }

  logout() {
    this.authService.logout();
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }
}
\`\`\`

**File:** `src/app/components/navbar/navbar.component.html`

\`\`\`html
<nav class="navbar" *ngIf="currentUser">
  <div class="navbar-container">
    <div class="navbar-brand">
      <a routerLink="/dashboard">AIA Management</a>
    </div>

    <ul class="navbar-menu">
      <li>
        <a routerLink="/dashboard" routerLinkActive="active">
          Dashboard
        </a>
      </li>
      <li>
        <a routerLink="/stabilimenti" routerLinkActive="active">
          Stabilimenti
        </a>
      </li>
      <li>
        <a routerLink="/prescrizioni" routerLinkActive="active">
          Prescrizioni
        </a>
      </li>
      <li>
        <a routerLink="/scadenze" routerLinkActive="active">
          Scadenze
        </a>
      </li>
      <li>
        <a routerLink="/documenti" routerLinkActive="active">
          Documenti
        </a>
      </li>
      <li *ngIf="isAdmin">
        <a routerLink="/users" routerLinkActive="active">
          Utenti
        </a>
      </li>
    </ul>

    <div class="navbar-user">
      <span class="user-info">
        {{ currentUser.nome }} {{ currentUser.cognome }}
        <span class="user-role">({{ currentUser.ruolo }})</span>
      </span>
      <button class="btn-logout" (click)="logout()">
        Logout
      </button>
    </div>
  </div>
</nav>
\`\`\`

**File:** `src/app/components/navbar/navbar.component.css`

\`\`\`css
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.navbar-brand a {
  color: white;
  font-size: 24px;
  font-weight: 700;
  text-decoration: none;
  padding: 20px 0;
  display: block;
}

.navbar-menu {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 5px;
}

.navbar-menu li a {
  color: white;
  text-decoration: none;
  padding: 20px 16px;
  display: block;
  transition: background 0.3s;
  font-weight: 500;
}

.navbar-menu li a:hover {
  background: rgba(255,255,255,0.1);
}

.navbar-menu li a.active {
  background: rgba(255,255,255,0.2);
  border-bottom: 3px solid white;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  font-weight: 500;
}

.user-role {
  font-size: 12px;
  opacity: 0.8;
}

.btn-logout {
  background: rgba(255,255,255,0.2);
  border: 1px solid white;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-logout:hover {
  background: white;
  color: #667eea;
}
\`\`\`

---

### 7. Dashboard Migliorata con Chart.js

Aggiorna `src/app/components/dashboard/dashboard.component.ts` per includere grafici.

**Installazione Chart.js:**
\`\`\`bash
npm install chart.js ng2-charts
\`\`\`

**Importa nel component:**
\`\`\`typescript
import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { ApiService } from '../../services/api.service';
import { DashboardStats, ConformitaTrend } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  stats: DashboardStats | null = null;
  loading = true;

  // Chart configuration
  public conformitaChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Conformità %',
        borderColor: '#4CAF50',
        backgroundColor: 'rgba(76, 175, 80, 0.2)',
        fill: true,
        tension: 0.4
      }
    ]
  };

  public conformitaChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true },
      title: { display: true, text: 'Trend Conformità Ultimi 12 Mesi' }
    },
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        ticks: { callback: (value) => value + '%' }
      }
    }
  };

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadDashboardData();
    this.loadConformitaTrend();
  }

  loadDashboardData() {
    this.loading = true;
    this.apiService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Errore caricamento stats:', err);
        this.loading = false;
      }
    });
  }

  loadConformitaTrend() {
    this.apiService.getConformitaTrend(12).subscribe({
      next: (trends: ConformitaTrend[]) => {
        this.conformitaChartData.labels = trends.map(t =>
          \`\${t.mese}/\${t.anno}\`
        );
        this.conformitaChartData.datasets[0].data = trends.map(t =>
          t.percentualeConformita
        );
        this.chart?.update();
      },
      error: (err) => console.error('Errore trend:', err)
    });
  }
}
\`\`\`

---

### 8. Componente Upload Documenti

**File:** `src/app/components/documenti/documento-upload.component.ts`

\`\`\`typescript
import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { TipoDocumento } from '../../models/documento.model';

@Component({
  selector: 'app-documento-upload',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: \`
    <div class="upload-modal">
      <div class="modal-content">
        <h2>Upload Documento</h2>
        <form [formGroup]="uploadForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>File</label>
            <input type="file" (change)="onFileSelected($event)" accept=".pdf,.docx,.xlsx,.jpg,.png">
            <small>Max 50MB - PDF, DOCX, XLSX, JPG, PNG</small>
          </div>

          <div class="form-group">
            <label>Stabilimento *</label>
            <select formControlName="stabilimentoId" class="form-control">
              <option value="">Seleziona...</option>
              <!-- Populate with stabilimenti -->
            </select>
          </div>

          <div class="form-group">
            <label>Tipo Documento *</label>
            <select formControlName="tipo" class="form-control">
              <option *ngFor="let tipo of tipiDocumento" [value]="tipo">
                {{ tipo }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>Anno *</label>
            <input type="number" formControlName="anno" class="form-control">
          </div>

          <div class="form-group">
            <label>Descrizione</label>
            <textarea formControlName="descrizione" class="form-control"></textarea>
          </div>

          <div class="form-actions">
            <button type="button" (click)="onCancel()" class="btn btn-secondary">
              Annulla
            </button>
            <button type="submit" [disabled]="!uploadForm.valid || uploading" class="btn btn-primary">
              {{ uploading ? 'Upload...' : 'Upload' }}
            </button>
          </div>

          <div *ngIf="uploadProgress > 0" class="progress-bar">
            <div class="progress-fill" [style.width.%]="uploadProgress"></div>
          </div>
        </form>
      </div>
    </div>
  \`
})
export class DocumentoUploadComponent {
  @Output() uploadComplete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  uploadForm: FormGroup;
  selectedFile: File | null = null;
  uploading = false;
  uploadProgress = 0;
  tipiDocumento = Object.values(TipoDocumento);

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService
  ) {
    this.uploadForm = this.fb.group({
      stabilimentoId: ['', Validators.required],
      tipo: ['', Validators.required],
      anno: [new Date().getFullYear(), Validators.required],
      descrizione: ['']
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  onSubmit() {
    if (!this.selectedFile) {
      alert('Seleziona un file');
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('stabilimentoId', this.uploadForm.value.stabilimentoId);
    formData.append('anno', this.uploadForm.value.anno);
    formData.append('tipo', this.uploadForm.value.tipo);
    if (this.uploadForm.value.descrizione) {
      formData.append('descrizione', this.uploadForm.value.descrizione);
    }

    this.uploading = true;
    this.apiService.uploadDocumento(formData).subscribe({
      next: () => {
        this.uploading = false;
        this.uploadProgress = 100;
        this.uploadComplete.emit();
      },
      error: (err) => {
        console.error('Errore upload:', err);
        this.uploading = false;
        alert('Errore durante upload');
      }
    });
  }

  onCancel() {
    this.cancel.emit();
  }
}
\`\`\`

---

## 📦 Struttura Finale

\`\`\`
src/app/
├── components/
│   ├── auth/
│   │   ├── login.component.ts/html/css ✅
│   │   └── register.component.ts/html/css ⚠️
│   ├── dashboard/
│   │   └── dashboard.component.ts/html/css ✅ (migliorare)
│   ├── navbar/
│   │   └── navbar.component.ts/html/css ⚠️
│   ├── stabilimenti/
│   │   ├── stabilimenti-list.component ⚠️
│   │   └── stabilimento-detail.component ⚠️
│   ├── prescrizioni/
│   │   └── prescrizioni-list.component ⚠️
│   ├── scadenze/
│   │   └── scadenze-list.component ⚠️
│   ├── documenti/
│   │   ├── documenti-list.component ⚠️
│   │   └── documento-upload.component ⚠️
│   └── users/
│       └── users-list.component ⚠️ (solo ADMIN)
├── guards/
│   ├── auth.guard.ts ✅
│   └── admin.guard.ts ✅
├── interceptors/
│   └── jwt.interceptor.ts ✅
├── models/
│   ├── user.model.ts ✅
│   ├── stabilimento.model.ts ✅
│   ├── prescrizione.model.ts ✅
│   ├── scadenza.model.ts ✅
│   ├── dati-ambientali.model.ts ✅
│   ├── documento.model.ts ✅
│   └── dashboard.model.ts ✅
├── services/
│   ├── auth.service.ts ✅
│   └── api.service.ts ✅ (espandere)
├── app.component.ts ✅
├── app.config.ts ⚠️
└── app.routes.ts ⚠️
\`\`\`

**Legenda:**
- ✅ Completato
- ⚠️ Da creare/aggiornare

---

## 🚀 Comandi Utili

\`\`\`bash
# Installare dipendenze
npm install

# Avviare dev server
ng serve

# Build produzione
ng build --configuration production

# Generare component
ng generate component components/nome-component --standalone

# Test
ng test
\`\`\`

---

## 🎯 Prossimi Passi Prioritari

1. **Configurare app.config.ts** con interceptor HTTP
2. **Aggiornare app.routes.ts** con tutte le route e guards
3. **Creare NavbarComponent** per navigazione
4. **Migliorare Dashboard** con Chart.js
5. **Implementare Stabilimenti CRUD** (list + detail)
6. **Creare Upload Documenti** con progress bar
7. **Implementare Toast Notifications** per errori/successo

---

## 📚 Risorse

- [Angular Documentation](https://angular.io/docs)
- [Chart.js Documentation](https://www.chartjs.org/docs/)
- [RxJS Documentation](https://rxjs.dev/)
- [Angular Material](https://material.angular.io/) (opzionale per UI)

---

**Versione:** 1.0.0
**Data:** 8 Febbraio 2025
**Stato:** In Progress (60% completato)
