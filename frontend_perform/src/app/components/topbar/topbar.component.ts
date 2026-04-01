import { Component, inject, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { TranslationService, Language } from '../../services/translation.service';
import { SidebarService } from '../../services/sidebar.service';
import { User } from '../../models/user.model';
import { environment } from '../../../environments/environment';
import { IconsModule } from '../../icons.module';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, IconsModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent {
  private readonly authService        = inject(AuthService);
  private readonly translationService = inject(TranslationService);
  readonly sidebarService             = inject(SidebarService);
  private readonly destroyRef         = inject(DestroyRef);

  currentUser     = signal<User | null>(null);
  currentLanguage = signal<Language>('it');

  logo  = environment.logo;
  ente  = environment.ente;

  userInitials = computed(() => {
    const u = this.currentUser();
    if (!u) return '?';
    return ((u.nome?.[0] ?? '') + (u.cognome?.[0] ?? '')).toUpperCase();
  });

  constructor() {
    this.authService.currentUser
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => this.currentUser.set(user));

    this.translationService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(lang => this.currentLanguage.set(lang));
  }

  onLogoError(event: Event) {
    (event.target as HTMLImageElement).style.display = 'none';
  }

  logout() {
    this.authService.logout();
  }

  switchLanguage(lang: Language) {
    this.translationService.setLanguage(lang);
  }
}
