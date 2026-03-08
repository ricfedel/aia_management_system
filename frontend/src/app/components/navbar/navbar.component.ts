import { Component, inject, signal, computed, effect, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
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
  private readonly authService      = inject(AuthService);
  private readonly translationService = inject(TranslationService);
  private readonly destroyRef       = inject(DestroyRef);

  currentUser     = signal<User | null>(null);
  currentLanguage = signal<Language>('it');
  collapsed       = signal<boolean>(false);

  isAdmin = computed(() => this.currentUser()?.ruolo === 'ADMIN');

  constructor() {
    this.authService.currentUser
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(user => this.currentUser.set(user));

    this.translationService.currentLanguage$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(lang => this.currentLanguage.set(lang));

    // Sincronizza la CSS variable usata da app.component per il margin-left del contenuto
    effect(() => {
      const w = this.collapsed() ? '64px' : '240px';
      document.documentElement.style.setProperty('--sidebar-width', w);
    });
  }

  toggle() {
    this.collapsed.update(v => !v);
  }

  logout() {
    this.authService.logout();
  }

  switchLanguage(lang: Language) {
    this.translationService.setLanguage(lang);
  }
}
