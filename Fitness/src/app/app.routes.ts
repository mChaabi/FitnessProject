import { Routes } from '@angular/router';
import { DashboardLayoutComponent } from './layouts/dashboard-layout/dashboard-layout';
import { PublicLayoutComponent } from './layouts/public-layout/public-layout';
import { roleGuard } from './core/guards-guard';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      {
        path: 'home',
        loadComponent: () =>
          import('./components/home-component/home-component').then((m) => m.HomeComponent),
      },
      {
        path: 'exercices',
        loadComponent: () =>
          import('./components/exercices-component/exercices-component').then(
            (m) => m.ExercicesComponent,
          ),
      },
      {
        path: 'contact',
        loadComponent: () =>
          import('./components/contact-component/contact-component').then(
            (m) => m.ContactComponent,
          ),
      },
      {
        path: 'about',
        loadComponent: () =>
          import('./components/about-component/about-component').then((m) => m.AboutComponent),
      },
      {
        path: 'unsubscribe',
        loadComponent: () =>
          import('./components/unsubscribe-page/unsubscribe-page').then((m) => m.UnsubscribePage),
      },
      {
        path: 'coachs',
        loadComponent: () =>
          import('./components/coaches-public/coaches-public').then((m) => m.CoachesPublic),
      },
      {
        path: 'tarifs',
        loadComponent: () =>
          import('./components/pricing-public/pricing-public').then((m) => m.PricingPublic),
      },
    ],
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./components/login-component/login-component').then((m) => m.LoginComponent),
  },

  {
    path: 'admin',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('ADMIN', 'RECEPTION')], // el grupo deja entrar a los dos
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'members' },

      // --- Solo ADMIN ---
      {
        path: 'dashboard',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'plans',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () => import('./features/admin/plans/plans').then((m) => m.PlansComponent),
      },
      {
        path: 'payments',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/admin/payments/payments').then((m) => m.PaymentsComponent),
      },
      {
        path: 'coaches',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/admin/coaches/coaches').then((m) => m.CoachesComponent),
      },
      {
        path: 'members/:id/progress',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/shared/progress/progress').then((m) => m.ProgressPage),
      },

      {
        path: 'classes',
        canActivate: [roleGuard('ADMIN', 'RECEPTION')],
        loadComponent: () =>
          import('./features/shared/schedule/schedule').then((m) => m.SchedulePage),
      },
      {
        path: 'classes/manage',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/admin/classes-admin/classes-admin').then((m) => m.ClassesAdmin),
      },

      {
        path: 'outbox',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () => import('./features/admin/outbox/outbox').then((m) => m.OutboxPage),
      },

      // --- ADMIN y RECEPTION ---
      {
        path: 'members',
        loadComponent: () =>
          import('./features/admin/members/members').then((m) => m.MembersComponent),
      },
      {
        path: 'members/new',
        loadComponent: () =>
          import('./features/admin/member-form/member-form').then((m) => m.MemberFormComponent),
      },
      {
        path: 'members/:id',
        loadComponent: () =>
          import('./features/admin/member-detail/member-detail').then(
            (m) => m.MemberDetailComponent,
          ),
      },
      {
        path: 'members/:id/edit',
        loadComponent: () =>
          import('./features/admin/member-form/member-form').then((m) => m.MemberFormComponent),
      },
    ],
  },
  {
    path: 'reception',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('ADMIN', 'RECEPTION')],
    children: [
      {
        path: 'checkin',
        loadComponent: () => import('./features/reception/checkin/checkin').then((m) => m.Checkin),
      },
    ],
  },
  {
    path: 'coach',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('COACH')],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'clients' },
      {
        path: 'clients',
        loadComponent: () => import('./features/coach/clients/clients').then((m) => m.Clients),
      },
      {
        path: 'programs/new',
        loadComponent: () =>
          import('./features/coach/program-form/program-form').then((m) => m.ProgramForm),
      },
      {
        path: 'clients/:id/progress',
        loadComponent: () =>
          import('./features/shared/progress/progress').then((m) => m.ProgressPage),
      },
      {
        path: 'programs/:id',
        loadComponent: () =>
          import('./features/coach/program-form/program-form').then((m) => m.ProgramForm),
      },
      {
        path: 'classes',
        loadComponent: () =>
          import('./features/shared/schedule/schedule').then((m) => m.SchedulePage),
      },
    ],
  },
  {
    path: 'member',
    component: DashboardLayoutComponent,
    canActivate: [roleGuard('MEMBER')],
    children: [
      {
        path: 'home',
        loadComponent: () => import('./features/member/home/home').then((m) => m.Home),
      },
      {
        path: 'progress',
        loadComponent: () =>
          import('./features/shared/progress/progress').then((m) => m.ProgressPage),
      },
      {
        path: 'classes',
        loadComponent: () =>
          import('./features/shared/schedule/schedule').then((m) => m.SchedulePage),
      },
    ],
  },
  { path: '**', redirectTo: 'home' },
];
