import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { Plan } from '../../../data/billing/billing';
import { Exercice } from '../../../data/public/exercice';
import { API } from '../../../core/api';
import { Reveal } from '../../../core/reveal';

@Component({
  selector: 'app-home', standalone: true, imports: [RouterLink, CurrencyPipe, Reveal],
  templateUrl: './home-component.html', styleUrl: './home-component.css'
})
export class HomeComponent {
  private http = inject(HttpClient);

  plans = signal<Plan[]>([]);
  featured = signal<Exercice[]>([]);

  readonly stats = [
    { value: '5+', label: 'Disciplines' }, { value: '7j/7', label: 'Ouvert' }, { value: '100%', label: 'Suivi personnalisé' },
  ];
  readonly features = [
    { icon: '🏋️', title: 'Coachs certifiés', text: 'Un suivi personnalisé par des professionnels diplômés.' },
    { icon: '📋', title: 'Programmes sur mesure', text: 'Des séances adaptées à votre objectif et à votre niveau.' },
    { icon: '📈', title: 'Suivi de progression', text: 'Mesures et courbes pour voir vos résultats évoluer.' },
    { icon: '📱', title: 'Accès par QR code', text: 'Entrez en un scan, sans carte ni file d\'attente.' },
  ];
  readonly shortcuts = [
    { icon: '🗓️', title: 'Réservez votre cours', text: 'Horaires et places libres en direct.', link: '/planning' },
    { icon: '🏅', title: 'Rencontrez nos coachs', text: 'Des experts à votre écoute.', link: '/coachs' },
    { icon: '💪', title: 'Découvrez les exercices', text: 'Cardio, musculation, HIIT et plus.', link: '/exercices' },
  ];

  constructor() {
    this.http.get<Plan[]>(`${API}/plans/public`).subscribe({ next: p => this.plans.set(p.slice(0, 3)), error: () => {} });
    this.http.get<Exercice[]>(`${API}/exercices`).subscribe({ next: e => this.featured.set(e.slice(0, 3)), error: () => {} });
  }
}