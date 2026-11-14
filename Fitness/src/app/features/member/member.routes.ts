import { Routes } from '@angular/router';
import { activeMemberGuard } from '../../core/guards-guard';

export const MEMBER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', title: 'Mon espace', loadComponent: () => import('./home/home').then((m) => m.Home) },
  { path: 'book', title: 'Réserver un cours', canActivate: [activeMemberGuard], loadComponent: () => import('./book/book').then((m) => m.BookPage) },
  { path: 'classes', title: 'Mon planning', loadComponent: () => import('../schedule/schedule').then((m) => m.SchedulePage) },
  { path: 'progress', title: 'Ma progression', loadComponent: () => import('../progress/progress').then((m) => m.ProgressPage) },
  { path: 'videos', title: 'Vidéos', loadComponent: () => import('./videos/videos').then((m) => m.VideosPage) },
];