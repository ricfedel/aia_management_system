import { Component, inject, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { TranslationService, Language } from '../../services/translation.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.css'
})
export class TopbarComponent {
  private readonly authService       = inject(AuthService);
  private readonly translationService = inject(TranslationService);
  private readonly destroyRef        = inject(DestroyRef);

  currentUser     = signal<User | null>(null);
  currentLanguage = signal<Language>('it');

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

  logout() {
    this.authService.logout();
  }

  switchLanguage(lang: Language) {
    this.translationService.setLanguage(lang);
  }
}
