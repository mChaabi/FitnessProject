import { Component, DestroyRef, HostListener, PLATFORM_ID, computed, effect, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../core/auth/auth-service';
import { ICONS, NAV, NavItem, SECTION_ORDER } from './nav.config';
import { ThemeService } from '../../core/theme';

const ROLE_LABEL: Record<string, string> = {
  ADMIN: 'Administrateur', RECEPTION: 'Réception', COACH: 'Coach', MEMBER: 'Membre',
};

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.css',
})
export class DashboardLayoutComponent {
  auth = inject(AuthService);
  private router = inject(Router);
  private browser = isPlatformBrowser(inject(PLATFORM_ID));   // keep above the signals that read storage
    theme = inject(ThemeService);

  icons = ICONS;
  open = signal(false);                                   // mobile drawer
  menu = signal(false);                                   // user dropdown
  collapsed = signal(this.read('fitforge_collapsed') === '1');
  dark = signal(this.read('fitforge_theme') === 'dark');

  private url = toSignal(
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd), map(() => this.router.url)),
    { initialValue: this.router.url },
  );

  initials = computed(() => {
    const u = this.auth.user();
    return u ? (u.prenom[0] + u.nom[0]).toUpperCase() : '?';
  });
  roleLabel = computed(() => ROLE_LABEL[this.auth.role() ?? ''] ?? '');

  private items = computed<NavItem[]>(() => {
    const role = this.auth.role();
    return role ? NAV.filter((i) => i.roles.includes(role)) : [];
  });

  sections = computed(() => {
    const by = new Map<string, NavItem[]>();
    this.items().forEach((i) => by.set(i.section, [...(by.get(i.section) ?? []), i]));
    return SECTION_ORDER.filter((s) => by.has(s)).map((name) => ({ name, items: by.get(name)! }));
  });

  /** Current page = the longest menu link that matches the URL (used by the breadcrumb). */
  current = computed(() => {
    const path = this.url().split('?')[0];
    return this.items()
      .filter((i) => path === i.link || path.startsWith(i.link + '/'))
      .sort((a, b) => b.link.length - a.link.length)[0] ?? null;
  });

    constructor() {
    this.theme.enter();
    inject(DestroyRef).onDestroy(() => this.theme.leave());
    effect(() => this.write('fitforge_collapsed', this.collapsed() ? '1' : '0'));
    effect(() => { this.url(); this.open.set(false); this.menu.set(false); });   // close menus on navigation
  }

  @HostListener('document:keydown.escape')
  closeAll() { this.open.set(false); this.menu.set(false); }

  private read(key: string): string | null {
    if (!this.browser) return null;
    try { return localStorage.getItem(key); } catch { return null; }
  }
  private write(key: string, value: string) {
    if (!this.browser) return;
    try { localStorage.setItem(key, value); } catch { /* private mode */ }
  }
}