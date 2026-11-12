import { Routes } from '@angular/router';

export const MEMBER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', loadComponent: () => import('./home/home').then((m) => m.Home) },
  { path: 'progress', loadComponent: () => import('../progress/progress').then((m) => m.ProgressPage) },
  { path: 'classes', loadComponent: () => import('../schedule/schedule').then((m) => m.SchedulePage) },
    { path: 'book', loadComponent: () => import('./book/book').then((m) => m.BookPage) },
      { path: 'videos', loadComponent: () => import('./videos/videos').then((m) => m.VideosPage) },
];