import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const currentUser = this.authService.currentUserValue;

    if (currentUser && this.authService.isAuthenticated) {
      // Controlla se la route richiede un ruolo specifico
      const requiredRoles = route.data['roles'] as string[];

      if (requiredRoles && requiredRoles.length > 0) {
        // Verifica se l'utente ha uno dei ruoli richiesti
        if (!requiredRoles.includes(currentUser.ruolo)) {
          // Utente autenticato ma senza i permessi necessari
          this.router.navigate(['/unauthorized']);
          return false;
        }
      }

      // Utente autenticato e autorizzato
      return true;
    }

    // Utente non autenticato, redirect al login
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
}
