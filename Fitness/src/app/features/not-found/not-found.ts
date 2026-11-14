import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';

@Component({
  selector: 'app-not-found', standalone: true, imports: [RouterLink],
  template: `
  <section class="nf">
    <span class="code">404</span>
    <h1>Page introuvable</h1>
    <p>Cette adresse n'existe pas ou a été déplacée.</p>
    <a class="btn btn-primary btn-lg" [routerLink]="home()">
      {{ auth.isLoggedIn() ? 'Retour à mon espace' : "Retour à l'accueil" }}</a>
  </section>`,
  styles: [`
    .nf { min-height: 100vh; display: grid; place-content: center; justify-items: center; gap: .8rem; text-align: center; padding: 2rem; }
    .code { font: 800 7rem/1 var(--font-display); color: var(--primary); animation: pop .6s var(--ease) both; }
    h1 { margin: 0; } p { margin: 0 0 1rem; color: var(--text-muted); }
  `]
})
export class NotFoundPage {
  auth = inject(AuthService);
  home = () => this.auth.isLoggedIn() ? this.auth.homeFor(this.auth.role()) : '/home';
}