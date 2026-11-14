import { Routes } from '@angular/router';

export const RECEPTION_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'checkin' },
  { path: 'checkin', title: 'Check-in', loadComponent: () => import('./checkin/checkin').then((m) => m.Checkin) },
];