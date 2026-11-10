import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PublicCatalogService, PublicCoach } from '../../../data/public/services/public-catalog';
import { Reveal } from '../../../core/reveal';

@Component({
  selector: 'app-coaches-public',
  standalone: true,
  imports: [RouterLink, Reveal],
  template: ` <section class="container page" reveal>
    <h1>Nos <span>coachs</span></h1>
    <p class="lead">Des professionnels pour vous accompagner à chaque étape.</p>
    <div class="grid">
      @for (c of coaches(); track c.id) {
        <article class="card hover-lift">
          <div class="coach-photo">
            <img src="/images/coach-default.jpg" [alt]="c.name" loading="lazy" />
          </div>
          <h3>{{ c.name }}</h3>
          <span class="badge badge-info">{{ c.specialties || 'Coach' }}</span>
          <p>{{ c.bio }}</p>
        </article>
      } @empty {
        <p class="lead">Nos coachs seront bientôt présentés ici.</p>
      }
    </div>
    <p class="center"><a routerLink="/planning" class="btn btn-primary">Voir les cours</a></p>
  </section>`,
  styles: [
    `
      .page {
        padding: 4rem 0 5rem;
      }
      h1,
      .lead,
      .center {
        text-align: center;
      }
      h1 span {
        color: var(--primary);
      }
      .lead,
      p {
        color: var(--text-muted);
      }
      .grid {
        display: grid;
        gap: 1.5rem;
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        margin: 2.5rem 0;
      }
      .card {
        text-align: center;
        display: grid;
        gap: 0.6rem;
        justify-items: center;
        padding-top: 2rem;
      }
      .avatar {
        width: 88px;
        height: 88px;
        border-radius: 50%;
        color: #fff;
        display: grid;
        place-items: center;
        font: 800 2.2rem var(--font-display);
        background: linear-gradient(135deg, var(--primary), #fb923c);
        box-shadow: 0 10px 24px rgba(249, 115, 22, 0.35);
        transition: transform 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
      }
      .card:hover .avatar {
        transform: scale(1.12) rotate(-6deg);
      }
    `,
  ],
})
export class CoachesPublic {
  coaches = signal<PublicCoach[]>([]);
  constructor() {
    inject(PublicCatalogService)
      .coaches()
      .subscribe({
        next: (c) => this.coaches.set(c),
        error: () => {},
      });
  }
}
