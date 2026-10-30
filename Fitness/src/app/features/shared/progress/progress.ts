import { Component } from '@angular/core';
import {  ElementRef, PLATFORM_ID, computed, effect, inject, signal, viewChild } from '@angular/core';
import { DatePipe, DecimalPipe, isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { METRICS, Measurement, MetricKey } from '../../../models/progress';
import { ProgressService } from '../../../services/progress';

@Component({
  selector: 'app-progress', standalone: true,
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  templateUrl: './progress.html', styleUrl: './progress.css'
})
export class ProgressPage {
  private svc = inject(ProgressService);
  private fb = inject(FormBuilder);
  private platformId = inject(PLATFORM_ID);

  memberId = Number(inject(ActivatedRoute).snapshot.paramMap.get('id')) || null;
  metrics = METRICS;
  rows = signal<Measurement[]>([]);
  metric = signal<MetricKey>('weightKg');
  loading = signal(true);
  error = signal('');
  saving = signal(false);
  canvas = viewChild<ElementRef<HTMLCanvasElement>>('cv');
  private chart: any = null;

  form = this.fb.nonNullable.group({
    measuredOn: [new Date().toISOString().substring(0, 10)],
    weightKg: [null as number | null], bodyFatPercent: [null as number | null],
    waistCm: [null as number | null], chestCm: [null as number | null],
    armCm: [null as number | null], notes: ['']
  });

  current = computed(() => METRICS.find(m => m.key === this.metric())!);

  /** Primera y última medida que tienen valor para la métrica elegida. */
  summary = computed(() => {
    const k = this.metric();
    const vals = this.rows().filter(r => r[k] != null);
    if (vals.length === 0) return null;
    const first = vals[0][k] as number, last = vals[vals.length - 1][k] as number;
    return { first, last, delta: Math.round((last - first) * 10) / 10, count: vals.length };
  });

  constructor() {
    this.load();
    effect(() => {                                   // redibuja al cambiar datos, métrica o canvas
      const el = this.canvas(), data = this.rows(), key = this.metric();
      if (el && isPlatformBrowser(this.platformId)) this.draw(el.nativeElement, data, key);
    });
  }

  load() {
    this.svc.list(this.memberId).subscribe({
      next: r => { this.rows.set(r); this.loading.set(false); },
      error: e => { this.error.set(e.error?.message ?? 'Chargement impossible'); this.loading.set(false); }
    });
  }

  save() {
    if (this.saving()) return;
    this.saving.set(true); this.error.set('');
    const v = this.form.getRawValue();
    const body = { ...v, notes: v.notes || null };
    this.svc.add(this.memberId, body).subscribe({
      next: () => { this.saving.set(false); this.form.patchValue({ weightKg: null, bodyFatPercent: null,
        waistCm: null, chestCm: null, armCm: null, notes: '' }); this.load(); },
      error: e => { this.saving.set(false);
        this.error.set(e.error?.fields ? Object.values(e.error.fields).join(' · ') : (e.error?.message ?? 'Erreur')); }
    });
  }

  remove(r: Measurement) {
    if (confirm('Supprimer cette mesure ?')) this.svc.remove(r.id).subscribe(() => this.load());
  }

  private async draw(canvas: HTMLCanvasElement, data: Measurement[], key: MetricKey) {
    const { Chart } = await import('chart.js/auto');
    this.chart?.destroy();
    const css = getComputedStyle(document.documentElement);
    const color = css.getPropertyValue('--primary').trim() || '#f97316';
    const m = this.current();
    this.chart = new Chart(canvas, {
      type: 'line',
      data: {
        labels: data.map(r => r.measuredOn),
        datasets: [{ label: `${m.label} (${m.unit})`, data: data.map(r => r[key] ?? null),
          borderColor: color, backgroundColor: color + '33', fill: true, tension: .3,
          pointRadius: 4, spanGaps: true }]
      },
      options: { responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } }, scales: { y: { beginAtZero: false } } }
    });
  }
}
