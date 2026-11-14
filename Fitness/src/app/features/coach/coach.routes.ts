import { Routes } from '@angular/router';

export const COACH_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clients' },
  { path: 'clients', title: 'Mes clients', loadComponent: () => import('./clients/clients').then((m) => m.Clients) },
  { path: 'clients/:id/progress', title: 'Progression', loadComponent: () => import('../progress/progress').then((m) => m.ProgressPage) },
  { path: 'programs/new', title: 'Nouveau programme', loadComponent: () => import('./program-form/program-form').then((m) => m.ProgramForm) },
  { path: 'programs/:id', title: 'Programme', loadComponent: () => import('./program-form/program-form').then((m) => m.ProgramForm) },
  { path: 'classes', title: 'Planning', loadComponent: () => import('../schedule/schedule').then((m) => m.SchedulePage) },
  { path: 'videos', title: 'Vidéos', loadComponent: () => import('../member/videos/videos').then((m) => m.VideosPage) },
];