import { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Exercice } from '../../models/exercice';

const LABELS: Record<string, string> = {
  CARDIO: 'Cardio', MUSCULATION: 'Musculation', YOGA: 'Yoga', HIIT: 'HIIT', GAINAGE: 'Gainage'
};
const ICONS: Record<string, string> = {
  CARDIO: '🏃', MUSCULATION: '🏋️', YOGA: '🧘', HIIT: '🔥', GAINAGE: '🧱'
};

@Component({
  selector: 'app-exercices',
  standalone: true,
  templateUrl: './exercices-component.html',
  styleUrl: './exercices-component.css'
})
export class ExercicesComponent {
  private http = inject(HttpClient);
    private readonly API = 'http://localhost:8080';

  all = signal<Exercice[]>([]);
  loading = signal(true);
  error = signal(false);
  filter = signal('ALL');
  query = signal('');
  broken = signal<Set<number>>(new Set());     // imágenes que no cargaron

  // los filtros salen de los datos reales
  categories = computed(() =>
    ['ALL', ...Array.from(new Set(this.all().map(e => String(e.categorie).toUpperCase())))]);

  visible = computed(() => {
    const f = this.filter(), q = this.query().trim().toLowerCase();
    return this.all().filter(e =>
      (f === 'ALL' || String(e.categorie).toUpperCase() === f) &&
      (!q || e.nom.toLowerCase().includes(q) || (e.description ?? '').toLowerCase().includes(q)));
  });

  constructor() { this.load(); }

  load() {
    this.loading.set(true); this.error.set(false);
    this.http.get<Exercice[]>(`${this.API}/exercices`).subscribe({
      next: d => { this.all.set(d); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }

  label(c: string) { return c === 'ALL' ? 'Tous' : (LABELS[c] ?? c); }
  icon(c: string)  { return ICONS[String(c).toUpperCase()] ?? '💪'; }

  src(e: Exercice): string | null {
    if (!e.image || this.broken().has(e.id)) return null;
    return e.image.startsWith('http') ? e.image : `/exercices/${e.image}`;
  }
  onImgError(id: number) { this.broken.update(s => new Set(s).add(id)); }
}