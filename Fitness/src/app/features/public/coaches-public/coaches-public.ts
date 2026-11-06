import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PublicCatalogService, PublicCoach } from '../../../data/public/services/public-catalog';
import { Reveal } from '../../../core/reveal';

@Component({
  selector: 'app-coaches-public', 
  standalone: true, 
  imports: [RouterLink, Reveal],
  template: `
  <section class="container page" reveal>
    <h1>Nos <span>coachs</span></h1>
    <p class="lead">Des professionnels pour vous accompagner à chaque étape.</p>
    <div class="grid">
      @for (c of coaches(); track c.id) {
        <article class="card hover-lift">
          <div class="avatar">{{ c.name.charAt(0) }}</div>
          <h3>{{ c.name }}</h3>
          <span class="badge badge-info">{{ c.specialties || 'Coach' }}</span>
          <p>{{ c.bio }}</p>
        </article>
      } @empty { <p class="lead">Nos coachs seront bientôt présentés ici.</p> }
    </div>
    <p class="center"><a routerLink="/planning" class="btn btn-primary">Voir les cours</a></p>
  </section>`,
  styles: [`
    .page { padding: 3rem 0 4rem; } h1, .lead, .center { text-align: center; } h1 span { color: var(--primary); }
    .lead, p { color: var(--text-muted); }
    .grid { display: grid; gap: 1.25rem; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); margin: 2rem 0; }
    .card { text-align: center; display: grid; gap: .5rem; justify-items: center; }
    .avatar { width: 72px; height: 72px; border-radius: 50%; background: var(--primary-soft); color: var(--primary);
      display: grid; place-items: center; font: 800 1.8rem var(--font-display); transition: transform .3s; }
    .card:hover .avatar { transform: scale(1.1) rotate(-4deg); }
  `]
})
export class CoachesPublic {
  coaches = signal<PublicCoach[]>([]);
  constructor() { 
    inject(PublicCatalogService).coaches().subscribe({ 
      next: c => this.coaches.set(c), 
      error: () => {} 
    }); 
  }
}