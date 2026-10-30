import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  // Signals declared properly inside the class
  loading = signal(false);
  error = signal('');

  // Formulaire de login basé sur votre modèle UserCredentials
  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(6)]],
  });

  onLogin() {
    if (this.loginForm.invalid || this.loading()) return;

    this.loading.set(true);
    this.error.set('');

    this.authService.login(this.loginForm.getRawValue() as any).subscribe({
      next: (res) => {
        // 3. Récupère et sécurise le returnUrl
        const ret = this.route.snapshot.queryParamMap.get('returnUrl');
        const safe = ret && ret.startsWith('/') && !ret.startsWith('//'); // évite les redirections externes

        // 4. Redirige vers le returnUrl s'il existe et est sûr, sinon vers la page d'accueil du rôle
        const targetUrl = safe ? ret! : this.authService.homeFor(res.user.role);
        this.router.navigate([this.authService.homeFor(res.user.role)]);
      },
      error: (e) => {
        this.error.set(
          e.status === 429
            ? 'Trop de tentatives. Réessayez dans quelques minutes.'
            : 'Email ou mot de passe incorrect.',
        );
        this.loading.set(false);
      },
    });
  }
}
