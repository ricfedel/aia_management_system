export interface User {
  id: number;
  username: string;
  nome: string;
  cognome: string;
  email: string;
  ruolo: UserRole;
  attivo: boolean;
  stabilimenti?: number[]; // IDs degli stabilimenti assegnati
}

export enum UserRole {
  ADMIN = 'ADMIN',
  RESPONSABILE = 'RESPONSABILE',
  OPERATORE = 'OPERATORE'
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  nome: string;
  cognome: string;
  email: string;
  ruolo: UserRole;
  stabilimentiIds?: number[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  username: string;
  ruolo: UserRole;
}
