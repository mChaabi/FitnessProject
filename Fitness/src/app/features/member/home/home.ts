import { Component, inject, signal } from '@angular/core';
import { CoachingService } from '../../../services/coaching';
import { DAYS, Program } from '../../../models/coaching';

@Component({ selector: 'app-member-home', standalone: true, templateUrl: './home.html' })
export class Home {
  private svc = inject(CoachingService);
  programs = signal<Program[]>([]);
  loading = signal(true);
  constructor() { this.svc.myPrograms().subscribe({ next: p => { this.programs.set(p); this.loading.set(false); },
                                                     error: () => this.loading.set(false) }); }

  byDay(p: Program) {
    return DAYS.map(d => ({ label: d.label, items: p.items.filter(i => i.dayOfWeek === d.n) }))
               .filter(g => g.items.length > 0);
  }
}
