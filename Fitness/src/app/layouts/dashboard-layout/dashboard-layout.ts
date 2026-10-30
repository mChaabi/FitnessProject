import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../models/auth';

interface NavItem {
  label: string;
  icon: string;
  link: string;
  roles: Role[];
  section: string;
}

const NAV: NavItem[] = [
  // ADMIN
  {
    section: 'Pilotage',
    label: 'Tableau de bord',
    icon: '📊',
    link: '/admin/dashboard',
    roles: ['ADMIN'],
  },
  {
    section: 'Accueil',
    label: 'Check-in',
    icon: '✅',
    link: '/reception/checkin',
    roles: ['ADMIN', 'RECEPTION'],
  },
  {
    section: 'Personnes',
    label: 'Membres',
    icon: '👥',
    link: '/admin/members',
    roles: ['ADMIN', 'RECEPTION'],
  },
  { section: 'Personnes', label: 'Coachs', icon: '🏅', link: '/admin/coaches', roles: ['ADMIN'] },
  { section: 'Finance', label: 'Abonnements', icon: '💳', link: '/admin/plans', roles: ['ADMIN'] },
  { section: 'Finance', label: 'Paiements', icon: '🧾', link: '/admin/payments', roles: ['ADMIN'] },
  { section: 'Pilotage', label: 'Emails', icon: '📧', link: '/admin/outbox', roles: ['ADMIN'] },

  // COACH
  {
    section: 'Coaching',
    label: 'Mes clients',
    icon: '🏋️',
    link: '/coach/clients',
    roles: ['COACH'],
  },

  // MEMBER
  {
    section: 'Mon espace',
    label: 'Mon programme',
    icon: '📋',
    link: '/member/home',
    roles: ['MEMBER'],
  },

  {
    section: 'Mon espace',
    label: 'Ma progression',
    icon: '📈',
    link: '/member/progress',
    roles: ['MEMBER'],
  },

  {
    section: 'Accueil',
    label: 'Planning',
    icon: '🗓️',
    link: '/admin/classes',
    roles: ['ADMIN', 'RECEPTION'],
  },
  {
    section: 'Accueil',
    label: 'Gérer les cours',
    icon: '⚙️',
    link: '/admin/classes/manage',
    roles: ['ADMIN'],
  },
  { section: 'Coaching', label: 'Planning', icon: '🗓️', link: '/coach/classes', roles: ['COACH'] },
  {
    section: 'Mon espace',
    label: 'Cours collectifs',
    icon: '🗓️',
    link: '/member/classes',
    roles: ['MEMBER'],
  },
];

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.css',
})
export class DashboardLayoutComponent {
  auth = inject(AuthService);
  open = signal(false);
  items = computed(() =>
    NAV.filter((i) => this.auth.role() && i.roles.includes(this.auth.role()!)),
  );
  initials = computed(() => {
    const u = this.auth.user();
    return u ? (u.prenom[0] + u.nom[0]).toUpperCase() : '?';
  });
  sections = computed(() => {
    const role = this.auth.role();
    if (!role) return [];
    const visible = NAV.filter((i) => i.roles.includes(role));
    const map = new Map<string, NavItem[]>();
    visible.forEach((i) => map.set(i.section, [...(map.get(i.section) ?? []), i]));
    return [...map.entries()].map(([name, items]) => ({ name, items }));
  });
}
