import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, firstValueFrom, of, shareReplay, tap } from 'rxjs';
import { AuthResponse, Role } from '../models/auth';
import { API } from '../core/api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  private session = signal<AuthResponse | null>(null);
  private refreshing: Observable<AuthResponse> | null = null;

  readonly user = computed(() => this.session()?.user ?? null);
  readonly token = computed(() => this.session()?.token ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly role = computed<Role | null>(() => this.user()?.role ?? null);

  constructor() {
    if (isPlatformBrowser(this.platformId)) sessionStorage.removeItem('fitforge_session');  // limpia el formato antiguo
  }

  login(credentials: { email: string; motDePasse: string }) {
    return this.http.post<AuthResponse>(`${API}/auth/login`, credentials)
      .pipe(tap(r => this.session.set(r)));
  }

  /** Renueva el access token usando la cookie. Varias llamadas simultáneas comparten UNA petición. */
  refresh(): Observable<AuthResponse> {
    if (!this.refreshing) {
      this.refreshing = this.http.post<AuthResponse>(`${API}/auth/refresh`, null).pipe(
        tap(r => this.session.set(r)),
        finalize(() => (this.refreshing = null)),
        shareReplay(1));
    }
    return this.refreshing;
  }

  /** Se ejecuta al arrancar la app: si hay cookie válida, el usuario sigue dentro. Nunca falla. */
  restore(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) return Promise.resolve();
    return firstValueFrom(this.refresh().pipe(catchError(() => of(null)))).then(() => undefined);
  }

  logout() {
    this.http.post(`${API}/auth/logout`, null).pipe(catchError(() => of(null)))
      .subscribe(() => { this.session.set(null); this.router.navigate(['/login']); });
  }

  /** La sesión no se pudo renovar: se sale a /login recordando dónde estaba. */
  expire() {
    this.session.set(null);
    this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }

  homeFor(role: Role | null): string {
    switch (role) {
      case 'ADMIN': return '/admin/dashboard';
      case 'RECEPTION': return '/reception/checkin';
      case 'COACH': return '/coach/clients';
      case 'MEMBER': return '/member/home';
      default: return '/login';
    }
  }
}