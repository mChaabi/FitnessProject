export type Role = 'ADMIN' | 'RECEPTION' | 'COACH' | 'MEMBER';

export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  user: User;
}