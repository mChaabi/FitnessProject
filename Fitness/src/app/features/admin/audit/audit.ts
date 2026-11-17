import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { addDays, ymd } from '../../../core/dates';
import { AUDIT_TYPES, AuditRow, actionLabel, actionTone } from '../../../data/audit/audit';
import { AuditApiService } from '../../../data/audit/service/audit';
import { Page } from '../../../data/members/member';

@Component({ selector: 'app-audit', standalone: true, imports: [DatePipe, RouterLink, ReactiveFormsModule],
  templateUrl: './audit.html', styleUrl: './audit.css' })
export class AuditPage {
  private api = inject(AuditApiService);

  types = AUDIT_TYPES;
  label = actionLabel;
  tone = actionTone;
  presets = [{ d: 0, l: "Aujourd'hui" }, { d: 6, l: '7 jours' }, { d: 29, l: '30 jours' }, { d: 89, l: '90 jours' }];

  search = new FormControl('', { nonNullable: true });
  type = signal('');
  from = signal(ymd(addDays(new Date(), -29)));
  to = signal(ymd(new Date()));
  page = signal(0);
  data = signal<Page<AuditRow> | null>(null);
  counts = signal<Record<string, number>>({});
  loading = signal(true);
  error = signal(false);

  total = computed(() => Object.values(this.counts()).reduce((a, b) => a + b, 0));

  groups = computed(() => {
    const by = new Map<string, AuditRow[]>();
    for (const r of this.data()?.content ?? []) {
      const k = r.createdAt.slice(0, 10);
      by.set(k, [...(by.get(k) ?? []), r]);
    }
    return [...by.entries()].map(([key, items]) => ({ key, label: this.dayLabel(key), items }));
  });

  constructor() {
    this.load();
    this.search.valueChanges.pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => { this.page.set(0); this.load(); });
  }

  load() {
    this.loading.set(true); this.error.set(false);
    this.api.list({ type: this.type(), q: this.search.value.trim(), from: this.from(), to: this.to(), page: this.page() }).subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
    this.api.stats(this.from(), this.to()).subscribe({ next: s => this.counts.set(s), error: () => {} });
  }

  setType(k: string) { this.type.set(k); this.page.set(0); this.load(); }
  setRange(days: number) { this.from.set(ymd(addDays(new Date(), -days))); this.to.set(ymd(new Date())); this.page.set(0); this.load(); }
  setFrom(v: string) { if (v) { this.from.set(v); this.page.set(0); this.load(); } }
  setTo(v: string) { if (v) { this.to.set(v); this.page.set(0); this.load(); } }
  go(p: number) { this.page.set(p); this.load(); }
  count(k: string) { return k ? (this.counts()[k] ?? 0) : this.total(); }

  private dayLabel(key: string) {
    if (key === ymd(new Date())) return "Aujourd'hui";
    if (key === ymd(addDays(new Date(), -1))) return 'Hier';
    return new Date(key + 'T00:00:00').toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
  }
}