import { Component, HostListener, PLATFORM_ID, computed, effect, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Slide { title: string; tagline: string; bullets: string[]; image: string; device?: 'phone'; }

const SLIDES: Slide[] = [
  { title: 'Gym Management System', tagline: 'Members · Coaches · Classes · Bookings · Plans · AI',
    bullets: ['Public website and member space', 'Reception and admin tools', 'Built with Spring Boot and Angular'], image: '/demo/01-home.png' },
  { title: 'Dashboard', tagline: 'Your gym at a glance',
    bullets: ['Live KPIs that count up', 'Revenue, visits and plan charts', 'Renewals and at-risk members'], image: '/demo/02-dashboard.png' },
  { title: 'Online Sign-up', tagline: 'From visitor to member',
    bullets: ['Choose a plan and a coach', 'Account and member file created together', 'Welcome email sent automatically'], image: '/demo/03-signup.png' },
  { title: 'Members', tagline: 'Every member in one table',
    bullets: ['Search and filter by status', 'QR code and barcode card', 'Subscription history and receipts'], image: '/demo/04-members.png' },
  { title: 'Smart Check-in', tagline: 'Three ways in',
    bullets: ['QR code or barcode scanner', 'Face recognition with confirmation', 'Expired and frozen plans refused'], image: '/demo/05-checkin.png' },
  { title: 'Class Booking', tagline: 'Three simple steps',
    bullets: ['Pick a class, then a coach and a time', 'Waiting list with automatic promotion', 'Cancel up to 2 hours before'], image: '/demo/06-booking.png' },
  { title: 'AI Booking Assistant', tagline: 'Ask in plain words',
    bullets: ['"A cardio class tonight"', 'Suggests real sessions only', 'You always confirm'], image: '/demo/07-assistant.png' },
  { title: 'AI Business Analyst', tagline: 'Answers backed by your data',
    bullets: ['Revenue, retention and occupancy', 'Every insight shows its real numbers', 'Anonymous data only'], image: '/demo/08-analyst.png' },
  { title: 'Video Library', tagline: 'Learn the moves before class',
    bullets: ['Cardio, strength, CrossFit, yoga', 'Filter and search', 'Plays in a focused player'], image: '/demo/09-videos.png' },
  { title: 'Class Planning', tagline: 'The whole week, organized',
    bullets: ['Recurring classes generate sessions', 'Attendees and capacity per session', 'One click to cancel and notify'], image: '/demo/10-planning.png' },
  { title: 'Plans & Payments', tagline: 'Simple, transparent billing',
    bullets: ['Create and activate plans', 'Cash, card or transfer', 'PDF receipt for every payment'], image: '/demo/11-plans.png' },
  { title: 'Fully Responsive', tagline: 'Works on phones too',
    bullets: ['Collapsible sidebar menu', 'Stacked cards and actions', 'Same features on mobile'], image: '/demo/12-mobile.png', device: 'phone' },
];

const DURATION = 5000;

@Component({
  selector: 'app-demo', standalone: true, imports: [RouterLink],
  templateUrl: './demo.html', styleUrl: './demo.css'
})
export class DemoPage {
  slides = SLIDES;
  total = SLIDES.length;
  duration = DURATION;
  index = signal(0);
  playing = signal(true);
  ui = signal(true);
  failed = signal<Record<string, boolean>>({});
  current = computed(() => SLIDES[this.index()]);
  private browser = isPlatformBrowser(inject(PLATFORM_ID));

  constructor() {
    effect((onCleanup) => {                       // restarts whenever the slide changes or play/pause toggles
      const i = this.index();
      if (!this.browser || !this.playing()) return;
      const t = setTimeout(() => this.go(i + 1), DURATION);
      onCleanup(() => clearTimeout(t));
    });
  }

  go(n: number) { this.index.set((n + this.total) % this.total); }
  fail(src: string) { this.failed.update(f => ({ ...f, [src]: true })); }
  pad(n: number) { return String(n).padStart(2, '0'); }

  fullscreen() {
    if (!this.browser) return;
    if (document.fullscreenElement) document.exitFullscreen();
    else document.documentElement.requestFullscreen?.();
  }

  @HostListener('document:keydown', ['$event'])
  key(e: KeyboardEvent) {
    if (e.key === 'ArrowRight') this.go(this.index() + 1);
    else if (e.key === 'ArrowLeft') this.go(this.index() - 1);
    else if (e.key === ' ') { e.preventDefault(); this.playing.set(!this.playing()); }
    else if (e.key.toLowerCase() === 'h') this.ui.set(!this.ui());
    else if (e.key.toLowerCase() === 'f') this.fullscreen();
  }
}