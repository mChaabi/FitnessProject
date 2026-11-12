import { Role } from '../../core/auth/auth';

export interface NavItem {
  label: string;
  icon: string;
  link: string;
  roles: Role[];
  section: string;
  exact?: boolean; // true when another item's link starts with this one
}

/** Single-path SVG icons (24x24, stroke style). */
export const ICONS: Record<string, string> = {
  dashboard: 'M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z',
  mail: 'M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z M22 6l-10 7L2 6',
  scan: 'M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4L12 14.01l-3-3',
  users:
    'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75',
  award: 'M12 15a7 7 0 1 0 0-14 7 7 0 0 0 0 14z M8.21 13.89L7 23l5-3 5 3-1.21-9.12',
  calendar: 'M3 4h18v18H3z M16 2v4 M8 2v4 M3 10h18',
  settings: 'M4 21v-7 M4 10V3 M12 21v-9 M12 8V3 M20 21v-5 M20 12V3 M1 14h6 M9 8h6 M17 16h6',
  card: 'M1 4h22v16H1z M1 10h22',
  receipt: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8',
  clipboard: 'M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2 M9 2h6v4H9z',
  trending: 'M23 6l-9.5 9.5-5-5L1 18 M17 6h6v6',
  // interface icons
  menu: 'M3 12h18 M3 6h18 M3 18h18',
  moon: 'M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z',
  sun: 'M12 17a5 5 0 1 0 0-10 5 5 0 0 0 0 10z M12 1v2 M12 21v2 M4.22 4.22l1.42 1.42 M18.36 18.36l1.42 1.42 M1 12h2 M21 12h2 M4.22 19.78l1.42-1.42 M18.36 5.64l1.42-1.42',
  chevronLeft: 'M15 18l-6-6 6-6',
  chevronDown: 'M6 9l6 6 6-6',
  logout: 'M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9',
  external: 'M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6 M15 3h6v6 M10 14L21 3',
  play: 'M5 3l14 9-14 9z',
  userPlus:
    'M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M8.5 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M20 8v6 M23 11h-6',
};

export const SECTION_ORDER = [
  'Pilotage',
  'Accueil',
  'Personnes',
  'Coaching',
  'Cours',
  'Finance',
  'Mon espace',
];

export const NAV: NavItem[] = [
  {
    section: 'Pilotage',
    label: 'Tableau de bord',
    icon: 'dashboard',
    link: '/admin/dashboard',
    roles: ['ADMIN'],
  },
  { section: 'Pilotage', label: 'Emails', icon: 'mail', link: '/admin/outbox', roles: ['ADMIN'] },

  {
    section: 'Accueil',
    label: 'Check-in',
    icon: 'scan',
    link: '/reception/checkin',
    roles: ['ADMIN', 'RECEPTION'],
  },

  {
    section: 'Personnes',
    label: 'Membres',
    icon: 'users',
    link: '/admin/members',
    roles: ['ADMIN', 'RECEPTION'],
  },
  {
    section: 'Personnes',
    label: 'Coachs',
    icon: 'award',
    link: '/admin/coaches',
    roles: ['ADMIN'],
  },

  {
    section: 'Coaching',
    label: 'Mes clients',
    icon: 'users',
    link: '/coach/clients',
    roles: ['COACH'],
  },

  {
    section: 'Cours',
    label: 'Planning',
    icon: 'calendar',
    link: '/admin/classes',
    roles: ['ADMIN', 'RECEPTION'],
    exact: true,
  },
  {
    section: 'Cours',
    label: 'Gérer les cours',
    icon: 'settings',
    link: '/admin/classes/manage',
    roles: ['ADMIN'],
  },
  {
    section: 'Cours',
    label: 'Réserver un cours',
    icon: 'calendar',
    link: '/member/book',
    roles: ['MEMBER'],
  },
  {
    section: 'Cours',
    label: 'Mon planning',
    icon: 'clipboard',
    link: '/member/classes',
    roles: ['MEMBER'],
  },

  {
    section: 'Finance',
    label: 'Abonnements',
    icon: 'card',
    link: '/admin/plans',
    roles: ['ADMIN'],
  },
  {
    section: 'Finance',
    label: 'Paiements',
    icon: 'receipt',
    link: '/admin/payments',
    roles: ['ADMIN'],
  },

  {
    section: 'Mon espace',
    label: 'Mon programme',
    icon: 'clipboard',
    link: '/member/home',
    roles: ['MEMBER'],
  },
  {
    section: 'Mon espace',
    label: 'Ma progression',
    icon: 'trending',
    link: '/member/progress',
    roles: ['MEMBER'],
  },

  { section: 'Cours', label: 'Vidéos', icon: 'play', link: '/admin/videos', roles: ['ADMIN'] },
  {
    section: 'Mon espace',
    label: 'Vidéos',
    icon: 'play',
    link: '/member/videos',
    roles: ['MEMBER'],
  },
  {
    section: 'Accueil',
    label: 'Inscriptions',
    icon: 'userPlus',
    link: '/admin/enrollments',
    roles: ['ADMIN', 'RECEPTION'],
  },
];
