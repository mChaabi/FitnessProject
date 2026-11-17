import { DatePipe, DecimalPipe, isPlatformBrowser } from '@angular/common';
import { Component, DestroyRef, ElementRef, PLATFORM_ID, computed, effect, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CountUp } from '../../../core/count-up';
import { applyChartTheme } from '../../../core/chart-theme';
import { ThemeService } from '../../../core/theme';
import { waLink, waMessages } from '../../../core/whatsapp';
import { ICONS } from '../../../layouts/dashboard-layout/nav.config';
import { AuditRow, actionLabel, actionTone } from '../../../data/audit/audit';
import { AuditApiService } from '../../../data/audit/service/audit';
import { AtRiskRow, DashboardData, ExpiringRow } from '../../../data/dashboard/dashboard';
import { DashboardService } from '../../../data/dashboard/service/dashboard';

const STATUS_LABEL: Record<string, string> = { ACTIVE: 'Actifs', FROZEN: 'Gelés', EXPIRED: 'Expirés', INACTIVE: 'Inactifs' };
const DAYS_FR = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];
const FIRST_HOUR = 6, LAST_HOUR = 23;                      // the heatmap shows 06h to 23h

@Component({
  selector: 'app-dashboard', standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, CountUp],
  templateUrl: './dashboard.html', styleUrl: './dashboard.css'
})
export class Dashboard {
  private svc = inject(DashboardService);
  private audit = inject(AuditApiService);
  private theme = inject(ThemeService);
  private platformId = inject(PLATFORM_ID);

  icons = ICONS;
  actionLabel = actionLabel;
  actionTone = actionTone;
  periods = [{ d: 7, l: '7 jours' }, { d: 30, l: '30 jours' }, { d: 90, l: '90 jours' }];

  data = signal<DashboardData | null>(null);
  recent = signal<AuditRow[]>([]);
  days = signal(30);
  loading = signal(true);
  error = signal(false);
  refreshing = signal(false);

  revCv = viewChild<ElementRef<HTMLCanvasElement>>('rev');
  visCv = viewChild<ElementRef<HTMLCanvasElement>>('vis');
  planCv = viewChild<ElementRef<HTMLCanvasElement>>('plan');
  growCv = viewChild<ElementRef<HTMLCanvasElement>>('grow');
  statCv = viewChild<ElementRef<HTMLCanvasElement>>('stat');
  private charts: any[] = [];
  private drawnKey = '';

  /** Heatmap: rows = weekdays, columns = hours, v = intensity 0..1 relative to the busiest cell. */
  heat = computed(() => {
    const d = this.data();
    if (!d) return null;
    const hours = Array.from({ length: LAST_HOUR - FIRST_HOUR + 1 }, (_, i) => i + FIRST_HOUR);
    let max = 0, best = { day: 0, hour: FIRST_HOUR };
    d.heatmap.forEach((row, day) => hours.forEach(h => { if (row[h] > max) { max = row[h]; best = { day, hour: h }; } }));
    const rows = d.heatmap.map((row, day) => ({
      label: DAYS_FR[day].slice(0, 3),
      cells: hours.map(h => ({ h, n: row[h], v: max ? row[h] / max : 0 }))
    }));
    return { hours, rows, empty: max === 0, peak: max ? `${DAYS_FR[best.day]} à ${best.hour} h (${max} entrées)` : '' };
  });

  totalPlanRevenue = computed(() => (this.data()?.planStats ?? []).reduce((a, p) => a + p.revenue, 0));

  constructor() {
    this.load();
    this.audit.list({ type: '', q: '', from: '', to: '', page: 0, size: 6 }).subscribe({ next: p => this.recent.set(p.content), error: () => {} });

    if (isPlatformBrowser(this.platformId)) {
      const timer = setInterval(() => { if (!document.hidden) this.load(true); }, 60_000);
      inject(DestroyRef).onDestroy(() => { clearInterval(timer); this.charts.forEach(c => c.destroy()); });
    }

    effect(() => {
      const d = this.data(), r = this.revCv(), v = this.visCv(), p = this.planCv(), g = this.growCv(), s = this.statCv();
      this.theme.dark();                                  // redraw when the theme changes
      if (d && r && v && p && g && s && isPlatformBrowser(this.platformId))
        this.draw(d, { rev: r.nativeElement, vis: v.nativeElement, plan: p.nativeElement, grow: g.nativeElement, stat: s.nativeElement });
    });
  }

  load(silent = false) {
    if (silent) this.refreshing.set(true); else { this.loading.set(true); this.error.set(false); }
    this.svc.load(this.days()).subscribe({
      next: d => { this.data.set(d); this.loading.set(false); this.refreshing.set(false); },
      error: () => { this.refreshing.set(false); if (!silent) { this.error.set(true); this.loading.set(false); } }
    });
  }

  setDays(n: number) { if (n !== this.days()) { this.days.set(n); this.load(true); } }

  /** "+12 %" against the previous period of the same length. */
  delta(cur: number, prev: number): { text: string; dir: 'up' | 'down' | 'flat' } {
    if (!prev) return { text: cur ? 'Nouveau' : '—', dir: cur ? 'up' : 'flat' };
    const p = Math.round(((cur - prev) / prev) * 100);
    return { text: `${p > 0 ? '+' : ''}${p} %`, dir: p > 0 ? 'up' : p < 0 ? 'down' : 'flat' };
  }

  share(v: number) { const t = this.totalPlanRevenue(); return t ? Math.round((v / t) * 100) : 0; }
  fill(s: { confirmed: number; capacity: number }) { return s.capacity ? Math.min(100, Math.round(s.confirmed * 100 / s.capacity)) : 0; }
  time(t: string) { return t.substring(0, 5); }
  statusLabel(s: string) { return STATUS_LABEL[s] ?? s; }

  // ---------- WhatsApp (Idea 1) ----------
  private first(name: string) { return name.split(' ')[0]; }
  private fr(d: string) { return `${d.slice(8, 10)}/${d.slice(5, 7)}/${d.slice(0, 4)}`; }
  renewLink(e: ExpiringRow) { return waLink(e.phone, waMessages.renewal(this.first(e.memberName), e.plan, this.fr(e.endDate))); }
  backLink(r: AtRiskRow) { return waLink(r.phone, waMessages.comeback(this.first(r.memberName), r.daysSince ?? null)); }

  // ---------- charts ----------
  private async draw(d: DashboardData, el: Record<string, HTMLCanvasElement>) {
    const { Chart } = await import('chart.js/auto');
    const key = `${this.theme.dark()}|${d.period.days}`;
    const animate = this.charts.length === 0 || key !== this.drawnKey;   // animate on first draw, theme change or new period
    this.drawnKey = key;

    Object.values(el).forEach(c => Chart.getChart(c)?.destroy());        // never two charts on one canvas
    const { tok, primary, grid } = applyChartTheme(Chart);

    const month = (m: string) => new Date(+m.slice(0, 4), +m.slice(5, 7) - 1, 1).toLocaleDateString('fr-FR', { month: 'short' });
    const day = (s: string) => `${s.slice(8, 10)}/${s.slice(5, 7)}`;
    const base = { responsive: true, maintainAspectRatio: false,
                   animation: animate ? { duration: 1100, easing: 'easeOutQuart' as const } : false as const };
    const scales = { x: { grid: { display: false } }, y: { beginAtZero: true, grid: { color: grid }, ticks: { precision: 0 } } };
    const many = d.visits.length > 31;

    this.charts = [
      new Chart(el['rev'], { type: 'bar',
        data: { labels: d.revenue.map(r => month(r.month)),
                datasets: [{ data: d.revenue.map(r => r.amount), backgroundColor: primary, borderRadius: 6, maxBarThickness: 44 }] },
        options: { ...base, scales, plugins: { legend: { display: false },
          tooltip: { callbacks: { label: (c: any) => ` ${(c.parsed.y ?? 0).toFixed(2)} MAD` } } } } }),

      new Chart(el['vis'], { type: 'line',
        data: { labels: d.visits.map(v => day(v.day)),
                datasets: [{ data: d.visits.map(v => v.visits), borderColor: primary, backgroundColor: primary + '26',
                             fill: true, tension: .35, pointRadius: many ? 0 : 3, pointHitRadius: 10 }] },
        options: { ...base, scales, plugins: { legend: { display: false } } } }),

      new Chart(el['plan'], { type: 'doughnut',
        data: { labels: d.plans.map(p => p.plan),
                datasets: [{ data: d.plans.map(p => p.count), borderWidth: 0,
                  backgroundColor: [primary, tok('--info', '#0284c7'), tok('--success', '#16a34a'),
                                    tok('--warning', '#d97706'), tok('--danger', '#dc2626'), '#94a3b8'] }] },
        options: { ...base, cutout: '62%', plugins: { legend: { position: 'bottom' } } } }),

      new Chart(el['grow'], { type: 'line',
        data: { labels: d.memberGrowth.map(m => month(m.month)),
                datasets: [{ data: d.memberGrowth.map(m => m.count), borderColor: tok('--info', '#0284c7'),
                             backgroundColor: tok('--info', '#0284c7') + '26', fill: true, tension: .35, pointRadius: 4 }] },
        options: { ...base, scales, plugins: { legend: { display: false } } } }),

      new Chart(el['stat'], { type: 'doughnut',
        data: { labels: d.memberStatus.map(s => STATUS_LABEL[s.status] ?? s.status),
                datasets: [{ data: d.memberStatus.map(s => s.count), borderWidth: 0,
                  backgroundColor: [tok('--success', '#16a34a'), tok('--info', '#0284c7'), tok('--warning', '#d97706'), '#94a3b8'] }] },
        options: { ...base, cutout: '62%', plugins: { legend: { position: 'bottom' } } } }),
    ];
  }
}