import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth-service';
import { PublicCatalogService } from '../../../data/public/services/public-catalog';
import { addDays, ymd } from '../../../core/dates';
import { Reveal } from '../../../core/reveal';
import { PublicSession } from '../../../data/public/public-catalog';

@Component({
  selector: 'app-planning-public', standalone: true, imports: [RouterLink, Reveal],
  templateUrl: './planning-public.html', styleUrl: './planning-public.css'
})
export class PlanningPublic {
  private svc = inject(PublicCatalogService);
  auth = inject(AuthService);

  sessions = signal<PublicSession[]>([]);
  loading = signal(true);
  error = signal(false);

  days = computed(() => Array.from({ length: 7 }, (_, i) => {
    const d = addDays(new Date(), i), key = ymd(d);
    return { key, label: d.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'short' }),
             items: this.sessions().filter(s => s.date === key) };
  }));

  constructor() {
    const today = new Date();
    this.svc.sessions(ymd(today), ymd(addDays(today, 6))).subscribe({
      next: s => { this.sessions.set(s); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }

  time(t: string) { return t.substring(0, 5); }
}