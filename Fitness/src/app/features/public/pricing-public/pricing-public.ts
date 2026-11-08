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
   .page { padding: 4rem 0 5rem; } h1, .lead, .center { text-align: center; } h1 span { color: var(--primary); }
.lead, p { color: var(--text-muted); }
.grid { display: grid; gap: 1.5rem; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); margin: 2.5rem 0; }
.card { text-align: center; display: grid; gap: .6rem; justify-items: center; padding-top: 2rem; }
.avatar { width: 88px; height: 88px; border-radius: 50%; color: #fff; display: grid; place-items: center;
  font: 800 2.2rem var(--font-display); background: linear-gradient(135deg, var(--primary), #fb923c);
  box-shadow: 0 10px 24px rgba(249,115,22,.35); transition: transform .4s cubic-bezier(.2,.8,.2,1); }
.card:hover .avatar { transform: scale(1.12) rotate(-6deg); }
  `]
})
export class PricingPublic {
  plans = signal<Plan[]>([]);
  constructor() { inject(PublicCatalogService).plans().subscribe({ next: p => this.plans.set(p), error: () => {} }); }
}