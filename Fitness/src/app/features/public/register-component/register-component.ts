import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../../../core/auth/auth-service';
import { Plan } from '../../../data/billing/billing';
import { PublicCatalogService, PublicCoach } from '../../../data/public/services/public-catalog';

@Component({
  selector: 'app-register', standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './register-component.html', styleUrl: './register-component.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private catalog = inject(PublicCatalogService);

  plans = signal<Plan[]>([]);
  coaches = signal<PublicCoach[]>([]);
  planId = signal<number | null>(null);
  coachId = signal<number | null>(null);
  loading = signal(false);
  error = signal('');

  form = this.fb.nonNullable.group({
    prenom: ['', [Validators.required, Validators.maxLength(50)]],
    nom: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', Validators.pattern(/^[+0-9 ()-]{6,20}$/)],
    motDePasse: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  constructor() {
    const q = this.route.snapshot.queryParamMap;
    const wantedPlan = Number(q.get('plan')) || null;       // /inscription?plan=3&coach=2
    const wantedCoach = Number(q.get('coach')) || null;

    this.catalog.plans().subscribe({
      next: p => { this.plans.set(p); if (p.some(x => x.id === wantedPlan)) this.planId.set(wantedPlan); },
      error: () => {}
    });
    this.catalog.coaches().subscribe({
      next: c => { this.coaches.set(c); if (c.some(x => x.id === wantedCoach)) this.coachId.set(wantedCoach); },
      error: () => {}
    });
  }

  togglePlan(id: number) { this.planId.set(this.planId() === id ? null : id); }

  submit() {
    if (this.form.invalid || this.loading()) { this.form.markAllAsTouched(); return; }
    this.loading.set(true); this.error.set('');
    const v = this.form.getRawValue();

    this.auth.register({
      ...v, phone: v.phone || undefined,
      planId: this.planId() ?? undefined, coachId: this.coachId() ?? undefined
    }).pipe(
      switchMap(() => this.auth.login({ email: v.email, motDePasse: v.motDePasse }))   // log in right away
    ).subscribe({
      next: () => this.router.navigateByUrl(this.target()),
      error: e => { this.loading.set(false); this.error.set(this.message(e)); }
    });
  }

  private target(): string {
    const ret = this.route.snapshot.queryParamMap.get('returnUrl');
    return !!ret && ret.startsWith('/') && !ret.startsWith('//') ? ret : '/member/home';
  }

  private message(e: any): string {
    if (e.status === 429) return 'Trop de tentatives. Réessayez dans quelques minutes.';
    if (e.error?.message) return e.error.message;
    if (e.error && typeof e.error === 'object') return Object.values(e.error).join(' · ');   // validation errors
    return 'Erreur inattendue';
  }
}