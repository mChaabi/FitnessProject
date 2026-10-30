import { Component } from '@angular/core';

import { inject, signal } from '@angular/core';
import { PublicCatalogService, PublicCoach } from '../../services/public-catalog';

@Component({
  selector: 'app-coaches-public', standalone: true,
  template: `
  <section class="container" style="padding:3rem 0">
    <h1>Nos coachs</h1>
    <div style="display:grid;gap:1rem;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));margin-top:1.5rem">
      @for (c of coaches(); track c.id) {
        <div class="card" style="text-align:center">
          <div class="avatar" style="margin:0 auto .75rem">{{ c.prenom[0] }}{{ c.nom[0] }}</div>
          <strong>{{ c.prenom }} {{ c.nom }}</strong>
        </div>
      } @empty { <p class="muted">Aucun coach pour le moment.</p> }
    </div>
  </section>`
})
export class CoachesPublic {
  coaches = signal<PublicCoach[]>([]);
  constructor() { inject(PublicCatalogService).coaches().subscribe(c => this.coaches.set(c)); }
}