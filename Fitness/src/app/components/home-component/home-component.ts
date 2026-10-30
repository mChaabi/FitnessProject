import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { Plan } from '../../models/billing';
import { Exercice } from '../../models/exercice';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './home-component.html',
  styleUrl: './home-component.css'
})
export class HomeComponent {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/';

  plans = signal<Plan[]>([]);
  featured = signal<Exercice[]>([]);

  readonly features = [
    { icon: '🏋️', title: 'Coachs certifiés', text: 'Un suivi personnalisé par des professionnels diplômés.' },
    { icon: '📋', title: 'Programmes sur mesure', text: 'Des séances adaptées à votre objectif et à votre niveau.' },
    { icon: '📈', title: 'Suivi de progression', text: 'Mesures et courbes pour voir vos résultats évoluer.' },
    { icon: '📱', title: 'Accès par QR code', text: 'Entrez en un scan, sans carte ni file d\'attente.' },
  ];

  constructor() {
    this.http.get<Plan[]>(`${this.API}/plans/public`).subscribe({ next: p => this.plans.set(p), error: () => {} });
    this.http.get<Exercice[]>(`${this.API}/exercices`).subscribe({
      next: e => this.featured.set(e.slice(0, 3)), error: () => {} });
  }
}