import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SidebarService {
  collapsed = signal<boolean>(false);

  constructor() {
    // Sincronizza la CSS var usata dal main-content per il margin-left
    effect(() => {
      const w = this.collapsed() ? '64px' : '240px';
      document.documentElement.style.setProperty('--sidebar-width', w);
    });
  }

  toggle() {
    this.collapsed.update(v => !v);
  }
}
