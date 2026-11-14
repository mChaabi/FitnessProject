import { Routes } from '@angular/router';
import { guestGuard } from '../../core/guards-guard';

export const PUBLIC_ROUTES: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', title: 'Accueil', loadComponent: () => import('./home-component/home-component').then((m) => m.HomeComponent) },
  { path: 'about', title: 'À propos', loadComponent: () => import('./about-component/about-component').then((m) => m.AboutComponent) },
  { path: 'exercices', title: 'Exercices', loadComponent: () => import('./exercices-component/exercices-component').then((m) => m.ExercicesComponent) },
  { path: 'coachs', title: 'Nos coachs', loadComponent: () => import('./coaches-public/coaches-public').then((m) => m.CoachesPublic) },
  { path: 'tarifs', title: 'Abonnements', loadComponent: () => import('./pricing-public/pricing-public').then((m) => m.PricingPublic) },
  { path: 'planning', title: 'Cours', loadComponent: () => import('./planning-public/planning-public').then((m) => m.PlanningPublic) },
  { path: 'contact', title: 'Contact', loadComponent: () => import('./contact-component/contact-component').then((m) => m.ContactComponent) },
  { path: 'unsubscribe', title: 'Désabonnement', loadComponent: () => import('./unsubscribe-page/unsubscribe-page').then((m) => m.UnsubscribePage) },
  { path: 'inscription', title: 'Inscription', canActivate: [guestGuard],
    loadComponent: () => import('./register-component/register-component').then((m) => m.RegisterComponent) },
];