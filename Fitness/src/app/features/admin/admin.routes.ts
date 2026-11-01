import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards-guard';

const onlyAdmin = [roleGuard('ADMIN')];

export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'members' },

  // ---- solo ADMIN ----
  { path: 'dashboard', canActivate: onlyAdmin, loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard) },
  { path: 'coaches', canActivate: onlyAdmin, loadComponent: () => import('./coaches/coaches').then((m) => m.CoachesComponent) },
  { path: 'plans', canActivate: onlyAdmin, loadComponent: () => import('./plans/plans').then((m) => m.PlansComponent) },
  { path: 'payments', canActivate: onlyAdmin, loadComponent: () => import('./payments/payments').then((m) => m.PaymentsComponent) },
  { path: 'outbox', canActivate: onlyAdmin, loadComponent: () => import('./outbox/outbox').then((m) => m.OutboxPage) },
  { path: 'classes/manage', canActivate: onlyAdmin, loadComponent: () => import('./classes-admin/classes-admin').then((m) => m.ClassesAdmin) },
  { path: 'members/:id/progress', canActivate: onlyAdmin, loadComponent: () => import('../progress/progress').then((m) => m.ProgressPage) },

  // ---- ADMIN y RECEPTION ----
  { path: 'classes', loadComponent: () => import('../schedule/schedule').then((m) => m.SchedulePage) },
  { path: 'members', loadComponent: () => import('./members/members').then((m) => m.MembersComponent) },
  { path: 'members/new', loadComponent: () => import('./member-form/member-form').then((m) => m.MemberFormComponent) },
  { path: 'members/:id', loadComponent: () => import('./member-detail/member-detail').then((m) => m.MemberDetailComponent) },
  { path: 'members/:id/edit', loadComponent: () => import('./member-form/member-form').then((m) => m.MemberFormComponent) },
];