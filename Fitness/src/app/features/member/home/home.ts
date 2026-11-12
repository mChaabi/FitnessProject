import { DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CoachingService } from '../../../data/coaching/service/coaching';
import { DAYS, Program } from '../../../data/coaching/coaching';
import { MemberOverview } from '../../../data/me/me';
import { MeService } from '../../../data/me/service/me';
import { PublicCatalogService, PublicCoach } from '../../../data/public/services/public-catalog';
import { Video, categoryLabel, thumbOf } from '../../../data/videos/videos';
import { VideosService } from '../../../data/videos/service/videos';

@Component({
  selector: 'app-member-home', standalone: true, imports: [RouterLink, DatePipe],
  templateUrl: './home.html', styleUrl: './home.css'
})
export class Home {
  private me = inject(MeService);
  private coaching = inject(CoachingService);
  private catalog = inject(PublicCatalogService);

  overview = signal<MemberOverview | null>(null);
  programs = signal<Program[]>([]);
  coaches = signal<PublicCoach[]>([]);
  loading = signal(true);
  openId = signal<number | null>(null);
  showQr = signal(false);
  qrUrl = signal<string | null>(null);

    private videosSvc = inject(VideosService);
  latest = signal<Video[]>([]);
  thumb = thumbOf;
  label = categoryLabel;

  /** Share of the subscription already used. */
  percent = computed(() => {
    const o = this.overview();
    if (!o || !o.totalDays || o.daysLeft == null) return 0;
    return Math.max(0, Math.min(100, Math.round(((o.totalDays - o.daysLeft) / o.totalDays) * 100)));
  });

  constructor() {
    this.me.overview().subscribe({
      next: o => { this.overview.set(o); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
        this.videosSvc.list().subscribe({ next: v => this.latest.set(v.slice(0, 4)), error: () => {} });   // in the constructor
    this.coaching.myPrograms().subscribe({ next: p => this.programs.set(p), error: () => {} });
    this.catalog.coaches().subscribe({ next: c => this.coaches.set(c), error: () => {} });
    inject(DestroyRef).onDestroy(() => { const u = this.qrUrl(); if (u) URL.revokeObjectURL(u); });
  }

  toggle(id: number) { this.openId.set(this.openId() === id ? null : id); }

  toggleQr() {
    this.showQr.set(!this.showQr());
    if (this.showQr() && !this.qrUrl())
      this.me.qr().subscribe(b => this.qrUrl.set(URL.createObjectURL(b)));
  }

  byDay(p: Program) {
    return DAYS.map(d => ({ label: d.label, items: p.items.filter(i => i.dayOfWeek === d.n) }))
               .filter(g => g.items.length > 0);
  }

  initials(name: string) {
    return name.split(' ').filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }
}