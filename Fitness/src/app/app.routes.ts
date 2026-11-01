import { Routes } from '@angular/router';
import { DashboardLayoutComponent } from './layouts/dashboard-layout/dashboard-layout';
import { PublicLayoutComponent } from './layouts/public-layout/public-layout';
import { guestGuard, roleGuard } from './core/guards-guard';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayoutComponent,
    loadChildren: () => import('./features/public/public.routes').then((m) => m.PUBLIC_ROUTES),
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login-component/login-component').then((m) => m.LoginComponent),
  },
  {
    path: 'admin',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('ADMIN', 'RECEPTION')],
    loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  {
    path: 'reception',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('ADMIN', 'RECEPTION')],
    loadChildren: () => import('./features/reception/reception.routes').then((m) => m.RECEPTION_ROUTES),
  },
  {
    path: 'coach',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('COACH')],
    loadChildren: () => import('./features/coach/coach.routes').then((m) => m.COACH_ROUTES),
  },
  {
    path: 'member',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('MEMBER')],
    loadChildren: () => import('./features/member/member.routes').then((m) => m.MEMBER_ROUTES),
  },
  { path: '**', redirectTo: 'home' },
];