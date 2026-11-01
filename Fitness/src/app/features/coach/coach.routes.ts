import { Routes } from '@angular/router';

export const COACH_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clients' },
  { path: 'clients', loadComponent: () => import('./clients/clients').then((m) => m.Clients) },
  { path: 'clients/:id/progress', loadComponent: () => import('../progress/progress').then((m) => m.ProgressPage) },
  { path: 'programs/new', loadComponent: () => import('./program-form/program-form').then((m) => m.ProgramForm) },
  { path: 'programs/:id', loadComponent: () => import('./program-form/program-form').then((m) => m.ProgramForm) },
  { path: 'classes', loadComponent: () => import('../schedule/schedule').then((m) => m.SchedulePage) },
];