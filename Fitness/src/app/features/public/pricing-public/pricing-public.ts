import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Plan } from '../../../data/billing/billing';
import { PublicCatalogService } from '../../../data/public/services/public-catalog';
import { Reveal } from '../../../core/reveal';

@Component({
  selector: 'app-pricing-public', standalone: true, imports: [CurrencyPipe, RouterLink, Reveal],
  template: `
  <section class="container page" reveal>
    <h1>Nos <span>abonnements</span></h1>
    <p class="lead">Des tarifs clairs, sans frais cachés. Inscription à l'accueil ou en ligne.</p>
    <div class="grid">
      @for (p of plans(); track p.id) {
        <article class="card plan hover-lift">
          <h3>{{ p.name }}</h3>
          <p class="price">{{ p.price | currency:'MAD ':'symbol':'1.0-0' }}</p>
          <small class="muted">{{ p.durationDays }} jour{{ p.durationDays > 1 ? 's' : '' }}</small>
          <p>{{ p.description }}</p>
          <a class="btn btn-primary" routerLink="/contact"
             [queryParams]="{ plan: p.name }">Je m'inscris</a>
        </article>
      } @empty { <p class="muted">Aucun abonnement disponible.</p> }
    </div>
    <p class="center"><a routerLink="/planning" class="btn btn-ghost">Voir les cours disponibles →</a></p>
  </section>`,
  styles: [`
    .page { padding: 3rem 0 4rem; } h1, .lead, .center { text-align: center; } h1 span { color: var(--primary); }
    .lead, .muted { color: var(--text-muted); }
    .grid { display: grid; gap: 1.25rem; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); margin: 2rem 0; }
    .plan { display: grid; gap: .4rem; align-content: start; text-align: center; }
    .price { font: 800 2.4rem var(--font-display); color: var(--primary); margin: 0; }
    .plan p:last-of-type { flex: 1; color: var(--text-muted); }
  `]
})
export class PricingPublic {
  plans = signal<Plan[]>([]);
  constructor() { inject(PublicCatalogService).plans().subscribe({ next: p => this.plans.set(p), error: () => {} }); }
}