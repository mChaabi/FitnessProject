import { Component } from '@angular/core';

import { CurrencyPipe } from '@angular/common';
import { inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Plan } from '../../models/billing';
import { PublicCatalogService } from '../../services/public-catalog';

@Component({
  selector: 'app-pricing-public', standalone: true, imports: [CurrencyPipe, RouterLink],
  template: `
  <section class="container" style="padding:3rem 0">
    <h1>Nos abonnements</h1>
    <div style="display:grid;gap:1rem;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));margin-top:1.5rem">
      @for (p of plans(); track p.id) {
        <div class="card">
          <h3>{{ p.name }}</h3>
          <p style="font-size:1.8rem;font-weight:800">{{ p.price | currency:'MAD ':'symbol':'1.0-0' }}</p>
          <p class="muted">{{ p.durationDays }} jours</p>
          <p>{{ p.description }}</p>
          <a class="btn btn-primary" routerLink="/contact">Je m'inscris</a>
        </div>
      } @empty { <p class="muted">Aucun abonnement disponible.</p> }
    </div>
  </section>`
})
export class PricingPublic {
  plans = signal<Plan[]>([]);
  constructor() { inject(PublicCatalogService).plans().subscribe(p => this.plans.set(p)); }
}