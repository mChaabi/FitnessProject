import { Routes } from '@angular/router';
import { guestGuard } from '../../core/guards-guard';

export const PUBLIC_ROUTES: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', loadComponent: () => import('./home-component/home-component').then((m) => m.HomeComponent) },
  { path: 'about', loadComponent: () => import('./about-component/about-component').then((m) => m.AboutComponent) },
  { path: 'exercices', loadComponent: () => import('./exercices-component/exercices-component').then((m) => m.ExercicesComponent) },
  { path: 'coachs', loadComponent: () => import('./coaches-public/coaches-public').then((m) => m.CoachesPublic) },
  { path: 'tarifs', loadComponent: () => import('./pricing-public/pricing-public').then((m) => m.PricingPublic) },
  { path: 'planning', loadComponent: () => import('./planning-public/planning-public').then((m) => m.PlanningPublic) },
  { path: 'contact', loadComponent: () => import('./contact-component/contact-component').then((m) => m.ContactComponent) },
  { path: 'unsubscribe', loadComponent: () => import('./unsubscribe-page/unsubscribe-page').then((m) => m.UnsubscribePage) },
    { path: 'inscription', canActivate: [guestGuard],
    loadComponent: () => import('./register-component/register-component').then((m) => m.RegisterComponent) },
];