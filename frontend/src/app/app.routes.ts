import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { LoginComponent } from './components/auth/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { StabilimentiListComponent } from './components/stabilimenti/stabilimenti-list.component';
import { DocumentiListComponent } from './components/documenti/documenti-list.component';
import { PrescrizioniListComponent } from './components/prescrizioni/prescrizioni-list.component';
import { ScadenzeListComponent } from './components/scadenze/scadenze-list.component';
import { DatiAmbientaliListComponent } from './components/dati-ambientali/dati-ambientali-list.component';
import { UtentiListComponent } from './components/utenti/utenti-list.component';
import { ProcessiListComponent } from './components/processi/processi-list.component';
import { WorkflowListComponent } from './components/workflow-list/workflow-list.component';
import { PuntiMonitoraggioComponent } from './components/punti-monitoraggio/punti-monitoraggio.component';
import { ProduzioneConsumiComponent } from './components/produzione-consumi/produzione-consumi.component';
import { RifiutiComponent } from './components/rifiuti/rifiuti.component';
import { ConformitaComponent } from './components/conformita/conformita.component';
import { ComunicazioniComponent } from './components/comunicazioni/comunicazioni.component';
import { RelazioneAnnualeComponent } from './components/relazione-annuale/relazione-annuale.component';
import { AnagraficaCaminiComponent } from './components/anagrafica-camini/anagrafica-camini.component';

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
    component: StabilimentiListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'documenti',
    component: DocumentiListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'processi',
    component: ProcessiListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'workflow',
    component: WorkflowListComponent,
    canActivate: [AuthGuard]
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
    path: 'dati-ambientali',
    component: DatiAmbientaliListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'utenti',
    component: UtentiListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'punti-monitoraggio',
    component: PuntiMonitoraggioComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'produzione-consumi',
    component: ProduzioneConsumiComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'rifiuti',
    component: RifiutiComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'conformita',
    component: ConformitaComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'comunicazioni',
    component: ComunicazioniComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'relazione-annuale',
    component: RelazioneAnnualeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'anagrafica-camini',
    component: AnagraficaCaminiComponent,
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '/dashboard' }
];
