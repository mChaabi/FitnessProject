import { Component, HostListener, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth-service';

interface Entry { label: string; icon: string; link: string; }
interface Group { title: string; items: Entry[]; }

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.css'
})
export class PublicLayoutComponent {
  auth = inject(AuthService);
  scrolled = signal(false);
  menuOpen = signal(false);
  manageOpen = signal(false);

  readonly manage: Group[] = [
    { title: 'Gestion', items: [
      { label: 'Membres',  icon: '👥', link: '/admin/members' },
      { label: 'Check-in', icon: '✅', link: '/reception/checkin' } ] },
    { title: 'Finance', items: [
      { label: 'Abonnements', icon: '💳', link: '/admin/plans' },
      { label: 'Paiements',   icon: '🧾', link: '/admin/payments' } ] },
    { title: 'Coaching', items: [
      { label: 'Mes clients', icon: '🏋️', link: '/coach/clients' } ] },
    { title: 'Espace', items: [
      { label: 'Mon espace', icon: '🏠', link: '/member/home' } ] },
  ];

  @HostListener('window:scroll')
  onScroll() { this.scrolled.set(window.scrollY > 24); }

  @HostListener('document:click', ['$event'])
  onDocClick(e: Event) {
    if (!(e.target as HTMLElement).closest('.dropdown')) this.manageOpen.set(false);
  }

  closeAll() { this.menuOpen.set(false); this.manageOpen.set(false); }
}