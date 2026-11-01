import { Role } from '../../core/auth/auth';

export interface NavItem { label: string; icon: string; link: string; roles: Role[]; section: string; }

/** Orden en que aparecen las secciones en el menú. */
export const SECTION_ORDER = ['Pilotage', 'Accueil', 'Personnes', 'Coaching', 'Cours', 'Finance', 'Mon espace'];

export const NAV: NavItem[] = [
  // ---- Pilotage ----
  { section: 'Pilotage', label: 'Tableau de bord', icon: '📊', link: '/admin/dashboard', roles: ['ADMIN'] },
  { section: 'Pilotage', label: 'Emails',          icon: '📧', link: '/admin/outbox',    roles: ['ADMIN'] },

  // ---- Accueil (la entrada del gimnasio) ----
  { section: 'Accueil', label: 'Check-in', icon: '✅', link: '/reception/checkin', roles: ['ADMIN', 'RECEPTION'] },

  // ---- Personnes ----
  { section: 'Personnes', label: 'Membres', icon: '👥', link: '/admin/members',   roles: ['ADMIN', 'RECEPTION'] },
  { section: 'Personnes', label: 'Coachs',  icon: '🏅', link: '/admin/coaches',   roles: ['ADMIN'] },

  // ---- Coaching ----
  { section: 'Coaching', label: 'Mes clients', icon: '🏋️', link: '/coach/clients', roles: ['COACH'] },

  // ---- Cours (el mismo tema, un enlace por rol) ----
  { section: 'Cours', label: 'Planning',          icon: '🗓️', link: '/admin/classes',        roles: ['ADMIN', 'RECEPTION'] },
  { section: 'Cours', label: 'Gérer les cours',   icon: '⚙️', link: '/admin/classes/manage', roles: ['ADMIN'] },
  { section: 'Cours', label: 'Planning',          icon: '🗓️', link: '/coach/classes',        roles: ['COACH'] },
  { section: 'Cours', label: 'Réserver un cours', icon: '🗓️', link: '/member/classes',       roles: ['MEMBER'] },

  // ---- Finance ----
  { section: 'Finance', label: 'Abonnements', icon: '💳', link: '/admin/plans',    roles: ['ADMIN'] },
  { section: 'Finance', label: 'Paiements',   icon: '🧾', link: '/admin/payments', roles: ['ADMIN'] },

  // ---- Mon espace ----
  { section: 'Mon espace', label: 'Mon programme',  icon: '📋', link: '/member/home',     roles: ['MEMBER'] },
  { section: 'Mon espace', label: 'Ma progression', icon: '📈', link: '/member/progress', roles: ['MEMBER'] },
];