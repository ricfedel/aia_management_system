import { Component, inject, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { TranslationService, Language } from '../../services/translation.service';
import { SidebarService } from '../../services/sidebar.service';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { User } from '../../models/user.model';
import { IconsModule } from '../../icons.module';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslatePipe, IconsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  private readonly authService        = inject(AuthService);
  private readonly translationService = inject(TranslationService);
  private readonly router             = inject(Router);
  private readonly destroyRef         = inject(DestroyRef);
  readonly sidebar                    = inject(SidebarService);

  currentUser = signal<User | null>(null);
  openGroup   = signal<string | null>(null);
  isAdmin     = computed(() => this.currentUser()?.ruolo === 'ADMIN');

  constructor() {
    this.authService.currentUser
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => this.currentUser.set(user));

    this.syncOpenGroupToUrl(this.router.url);

    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(e => this.syncOpenGroupToUrl(e.urlAfterRedirects));
  }

  toggleGroup(id: string) {
    if (this.sidebar.collapsed()) {
      this.sidebar.collapsed.set(false);
      this.openGroup.set(id);
    } else {
      this.openGroup.update(current => current === id ? null : id);
    }
  }

  isGroupActive(routes: string[]): boolean {
    const url = this.router.url.split('?')[0];
    return routes.some(r => url === r || url.startsWith(r + '/'));
  }

  private syncOpenGroupToUrl(url: string) {
    const path = url.split('?')[0];
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
    const match = Object.entries(ROUTE_TO_GROUP).find(
      ([route]) => path === route || path.startsWith(route + '/')
    );
    this.openGroup.set(match ? match[1] : null);
  }
}
