import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import {
  User,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  UserRole
} from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public get isAuthenticated(): boolean {
    return !!this.getToken();
  }

  public get isAdmin(): boolean {
    return this.currentUserValue?.ruolo === UserRole.ADMIN;
  }

  public get isResponsabile(): boolean {
    return this.currentUserValue?.ruolo === UserRole.RESPONSABILE;
  }

  public get isOperatore(): boolean {
    return this.currentUserValue?.ruolo === UserRole.OPERATORE;
  }

  login(username: string, password: string): Observable<AuthResponse> {
    const request: LoginRequest = { username, password };

    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          // Salva i token
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);

          // Crea oggetto User da AuthResponse
          const user: User = {
            id: response.userId,
            username: response.username,
            ruolo: response.ruolo,
            nome: '', // Verrà caricato con /me
            cognome: '',
            email: '',
            attivo: true
          };

          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);

          // Carica i dati completi dell'utente
          this.loadCurrentUser().subscribe();
        })
      );
  }

  register(request: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, request);
  }

  logout(): void {
    // Rimuovi tutti i dati dall'archiviazione locale
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');

    // Aggiorna il BehaviorSubject
    this.currentUserSubject.next(null);

    // Redirect al login
    this.router.navigate(['/login']);
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem('refreshToken');

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
        })
      );
  }

  loadCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`)
      .pipe(
        tap(user => {
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        })
      );
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  hasAccessToStabilimento(stabilimentoId: number): boolean {
    const user = this.currentUserValue;

    if (!user) {
      return false;
    }

    // ADMIN ha accesso a tutto
    if (user.ruolo === UserRole.ADMIN) {
      return true;
    }

    // Altri ruoli: controlla se lo stabilimento è assegnato
    return user.stabilimenti?.includes(stabilimentoId) || false;
  }

  canEdit(): boolean {
    const user = this.currentUserValue;
    return user?.ruolo === UserRole.ADMIN || user?.ruolo === UserRole.RESPONSABILE;
  }

  canDelete(): boolean {
    return this.isAdmin;
  }
}
