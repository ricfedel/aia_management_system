import { Component, inject, signal, computed, effect, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { TranslationService, Language } from '../../services/translation.service';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  private readonly authService       = inject(AuthService);
  private readonly translationService = inject(TranslationService);
  private readonly router            = inject(Router);
  private readonly destroyRef        = inject(DestroyRef);

  currentUser     = signal<User | null>(null);
  currentLanguage = signal<Language>('it');
  collapsed       = signal<boolean>(false);

  /** ID del gruppo accordion attualmente aperto (null = tutti chiusi). */
  openGroup = signal<string | null>(null);

  isAdmin = computed(() => this.currentUser()?.ruolo === 'ADMIN');

  constructor() {
    this.authService.currentUser
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => this.currentUser.set(user));

    this.translationService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(lang => this.currentLanguage.set(lang));

    // Apri il gruppo corretto al caricamento iniziale
    this.syncOpenGroupToUrl(this.router.url);

    // Aggiorna il gruppo aperto ad ogni navigazione
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(e => this.syncOpenGroupToUrl(e.urlAfterRedirects));

    // Sincronizza la CSS variable usata da app.component per il margin-left del contenuto
    effect(() => {
      const w = this.collapsed() ? '64px' : '240px';
      document.documentElement.style.setProperty('--sidebar-width', w);
    });
  }

  toggle() {
    this.collapsed.update(v => !v);
  }

  /**
   * Apre/chiude il gruppo accordion cliccato.
   * Se la sidebar è collassata, la espande automaticamente.
   */
  toggleGroup(id: string) {
    if (this.collapsed()) {
      this.collapsed.set(false);
      this.openGroup.set(id);
    } else {
      this.openGroup.update(current => current === id ? null : id);
    }
  }

  /**
   * Restituisce true se almeno uno dei percorsi passati corrisponde alla URL corrente.
   * Usato nel template per evidenziare il gruppo che contiene la pagina attiva.
   */
  isGroupActive(routes: string[]): boolean {
    const url = this.router.url.split('?')[0]; // ignora query params
    return routes.some(r => url === r || url.startsWith(r + '/'));
  }

  /**
   * Determina quale gruppo deve risultare aperto in base alla URL corrente
   * e aggiorna il signal openGroup di conseguenza.
   */
  private syncOpenGroupToUrl(url: string) {
    const path = url.split('?')[0]; // ignora query params

    const ROUTE_TO_GROUP: Record<string, string> = {
      '/stabilimenti':       'gestione-aia',
      '/prescrizioni':       'gestione-aia',
      '/scadenze':           'gestione-aia',
      '/anagrafica-camini':  'monitoraggio',
      '/punti-monitoraggio': 'monitoraggio',
      '/dati-ambientali':    'monitoraggio',
      '/conformita':         'monitoraggio',
      '/produzione-consumi': 'produzione',
      '/rifiuti':            'produzione',
      '/relazione-annuale':  'reportistica',
      '/comunicazioni':      'reportistica',
      '/documenti':          'documenti',
      '/processi':           'documenti',
      '/workflow':           'documenti',
      '/utenti':             'amministrazione',
    };

    // Cerca la corrispondenza anche per sotto-percorsi (es. /stabilimenti/1/edit)
    const match = Object.entries(ROUTE_TO_GROUP).find(
      ([route]) => path === route || path.startsWith(route + '/')
    );

    this.openGroup.set(match ? match[1] : null);
  }

  logout() {
    this.authService.logout();
  }

  switchLanguage(lang: Language) {
    this.translationService.setLanguage(lang);
  }
}
