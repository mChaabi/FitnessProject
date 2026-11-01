import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { NAV, NavItem, SECTION_ORDER } from './nav.config';

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

  initials = computed(() => {
    const u = this.auth.user();
    return u ? (u.prenom[0] + u.nom[0]).toUpperCase() : '?';
  });

  sections = computed(() => {
    const role = this.auth.role();
    if (!role) return [];
    const bySection = new Map<string, NavItem[]>();
    NAV.filter((i) => i.roles.includes(role)).forEach((i) =>
      bySection.set(i.section, [...(bySection.get(i.section) ?? []), i]),
    );
    return SECTION_ORDER.filter((s) => bySection.has(s)).map((name) => ({ name, items: bySection.get(name)! }));
  });
}