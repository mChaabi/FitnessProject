import { Routes } from '@angular/router';

export const RECEPTION_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'checkin' },
  { path: 'checkin', loadComponent: () => import('./checkin').then((m) => m.Checkin) },
];