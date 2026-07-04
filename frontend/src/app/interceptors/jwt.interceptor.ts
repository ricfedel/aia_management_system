import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Aggiungi il token se esiste e non è una richiesta di login/refresh
  const token = authService.getToken();
  const isAuthRequest = req.url.includes('/auth/login') ||
                        req.url.includes('/auth/register') ||
                        req.url.includes('/auth/refresh');

  let authReq = req;
  if (token && !isAuthRequest) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Se errore 401 (Unauthorized) e non è già una richiesta auth, fai logout
      if (error.status === 401 && !isAuthRequest) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
