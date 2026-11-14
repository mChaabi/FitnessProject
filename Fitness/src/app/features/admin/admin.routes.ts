import { Routes } from '@angular/router';
import { homeGuard, roleGuard } from '../../core/guards-guard';

const onlyAdmin = [roleGuard('ADMIN')];

export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', canActivate: [homeGuard], children: [] },   // admin -> dashboard, reception -> check-in

  // ---- ADMIN only ----
  { path: 'dashboard', title: 'Tableau de bord', canActivate: onlyAdmin, loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard) },
  { path: 'coaches', title: 'Coachs', canActivate: onlyAdmin, loadComponent: () => import('./coaches/coaches').then((m) => m.CoachesComponent) },
  { path: 'plans', title: 'Abonnements', canActivate: onlyAdmin, loadComponent: () => import('./plans/plans').then((m) => m.PlansComponent) },
  { path: 'payments', title: 'Paiements', canActivate: onlyAdmin, loadComponent: () => import('./payments/payments').then((m) => m.PaymentsComponent) },
  { path: 'outbox', title: 'Emails', canActivate: onlyAdmin, loadComponent: () => import('./outbox/outbox').then((m) => m.OutboxPage) },
  { path: 'videos', title: 'Vidéos', canActivate: onlyAdmin, loadComponent: () => import('./videos-admin/videos-admin').then((m) => m.VideosAdmin) },
  { path: 'classes/manage', title: 'Gérer les cours', canActivate: onlyAdmin, loadComponent: () => import('./classes-admin/classes-admin').then((m) => m.ClassesAdmin) },
  { path: 'members/:id/progress', title: 'Progression', canActivate: onlyAdmin, loadComponent: () => import('../progress/progress').then((m) => m.ProgressPage) },

  // ---- ADMIN and RECEPTION ----
  { path: 'enrollments', title: 'Inscriptions', loadComponent: () => import('./enrollments/enrollments').then((m) => m.EnrollmentsPage) },
  { path: 'classes', title: 'Planning', loadComponent: () => import('../schedule/schedule').then((m) => m.SchedulePage) },
  { path: 'members', title: 'Membres', loadComponent: () => import('./members/members').then((m) => m.MembersComponent) },
  { path: 'members/new', title: 'Nouveau membre', loadComponent: () => import('./member-form/member-form').then((m) => m.MemberFormComponent) },
  { path: 'members/:id', title: 'Fiche membre', loadComponent: () => import('./member-detail/member-detail').then((m) => m.MemberDetailComponent) },
  { path: 'members/:id/edit', title: 'Modifier le membre', loadComponent: () => import('./member-form/member-form').then((m) => m.MemberFormComponent) },
];