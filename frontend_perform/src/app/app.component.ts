import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { TopbarComponent } from './components/topbar/topbar.component';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, NavbarComponent, TopbarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'AIA Management System';

  constructor() {
    // Applica il tema CSS da environment (come fa PerformPlus)
    const theme = environment.theme;
    Object.keys(theme).forEach(key => {
      const k = key as keyof typeof theme;
      document.documentElement.style.setProperty(k, theme[k]);
    });
  }
}
