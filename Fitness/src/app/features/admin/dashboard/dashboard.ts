import { Component } from '@angular/core';

import { DestroyRef, ElementRef, PLATFORM_ID, effect, inject, signal, viewChild } from '@angular/core';
import { CurrencyPipe, DatePipe, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardData } from '../../../models/dashboard';
import { DashboardService } from '../../../services/dashboard';

@Component({
  selector: 'app-dashboard', standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './dashboard.html', styleUrl: './dashboard.css'
})
export class Dashboard {
  private svc = inject(DashboardService);
  private platformId = inject(PLATFORM_ID);

  data = signal<DashboardData | null>(null);
  loading = signal(true);
  error = signal(false);
  refreshing = signal(false);

  revCv = viewChild<ElementRef<HTMLCanvasElement>>('rev');
  visCv = viewChild<ElementRef<HTMLCanvasElement>>('vis');
  planCv = viewChild<ElementRef<HTMLCanvasElement>>('plan');
  private charts: any[] = [];

  constructor() {
    this.load();

    if (isPlatformBrowser(this.platformId)) {            // en el servidor (SSR) no hay temporizador
      const timer = setInterval(() => { if (!document.hidden) this.load(true); }, 60_000);
      inject(DestroyRef).onDestroy(() => { clearInterval(timer); this.charts.forEach(c => c.destroy()); });
    }

    effect(() => {
      const d = this.data(), r = this.revCv(), v = this.visCv(), p = this.planCv();
      if (d && r && v && p && isPlatformBrowser(this.platformId)) this.draw(d, r.nativeElement, v.nativeElement, p.nativeElement);
    });
  }

  load(silent = false) {
    if (silent) this.refreshing.set(true); else { this.loading.set(true); this.error.set(false); }
    this.svc.load().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); this.refreshing.set(false); },
      // en un refresco automático fallido se conservan los datos que ya se veían
      error: () => { this.refreshing.set(false); if (!silent) { this.error.set(true); this.loading.set(false); } }
    });
  }

  fill(s: { confirmed: number; capacity: number }) { return Math.min(100, Math.round(s.confirmed * 100 / s.capacity)); }
  time(t: string) { return t.substring(0, 5); }

  private async draw(d: DashboardData, rev: HTMLCanvasElement, vis: HTMLCanvasElement, plan: HTMLCanvasElement) {
    const { Chart } = await import('chart.js/auto');
    this.charts.forEach(c => c.destroy());

    const css = getComputedStyle(document.documentElement);
    const tok = (n: string, f: string) => css.getPropertyValue(n).trim() || f;
    const primary = tok('--primary', '#f97316'), grid = tok('--border', '#e5e9f0');
    Chart.defaults.font.family = tok('--font-body', 'Inter, sans-serif');
    Chart.defaults.color = tok('--text-muted', '#64748b');

    const month = (m: string) => new Date(+m.slice(0, 4), +m.slice(5, 7) - 1, 1)
        .toLocaleDateString('fr-FR', { month: 'short' });
    const day = (s: string) => `${s.slice(8, 10)}/${s.slice(5, 7)}`;
    const base = { responsive: true, maintainAspectRatio: false };
    const scales = { x: { grid: { display: false } }, y: { beginAtZero: true, grid: { color: grid }, ticks: { precision: 0 } } };

    this.charts = [
      new Chart(rev, { type: 'bar',
        data: { labels: d.revenue.map(r => month(r.month)),
                datasets: [{ data: d.revenue.map(r => r.amount), backgroundColor: primary, borderRadius: 6, maxBarThickness: 44 }] },
        options: { ...base, scales, plugins: { legend: { display: false },
          tooltip: { callbacks: { label: c => ` ${(c.parsed.y ?? 0).toFixed(2)} MAD` } } } } }),

      new Chart(vis, { type: 'line',
        data: { labels: d.visits.map(v => day(v.day)),
                datasets: [{ data: d.visits.map(v => v.visits), borderColor: primary, backgroundColor: primary + '26',
                             fill: true, tension: .35, pointRadius: 3 }] },
        options: { ...base, scales, plugins: { legend: { display: false } } } }),

      new Chart(plan, { type: 'doughnut',
        data: { labels: d.plans.map(p => p.plan),
                datasets: [{ data: d.plans.map(p => p.count), borderWidth: 0,
                  backgroundColor: [primary, tok('--info', '#0284c7'), tok('--success', '#16a34a'),
                                    tok('--warning', '#d97706'), tok('--danger', '#dc2626'), '#94a3b8'] }] },
        options: { ...base, cutout: '62%', plugins: { legend: { position: 'bottom' } } } }),
    ];
  }
}
