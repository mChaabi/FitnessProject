import { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Exercice } from '../../../data/public/exercice';
import { API } from '../../../core/api';

const LABELS: Record<string, string> = {
  CARDIO: 'Cardio',
  MUSCULATION: 'Musculation',
  YOGA: 'Yoga',
  HIIT: 'HIIT',
  GAINAGE: 'Gainage',
};

const ICONS: Record<string, string> = {
  CARDIO: '🏃',
  MUSCULATION: '🏋️',
  YOGA: '🧘',
  HIIT: '🔥',
  GAINAGE: '🧱',
};

const CATEGORY_IMG: Record<string, string> = {
  CARDIO: 'workout-cardio.jpg',
  MUSCULATION: 'workout-musculation.jpg',
  YOGA: 'workout-yoga.jpg',
  HIIT: 'workout-hiit.jpg',
  GAINAGE: 'workout-musculation.jpg',
};

const NAME_IMG: [string, string][] = [
  ['burpee', 'workout-hiit.jpg'],
  ['pompes', 'Pompes classiques.jpg'],
  ['squat', 'Squats libres.jpg'],
  ['tapis', 'Course sur tapis.jpg'],
  ['course', 'Course sur tapis.jpg'],
  ['traction', 'Tractions à la barre fixe.jpg'],
  ['gainage', 'Gainage ventral (Planche).jpg'],
  ['planche', 'Gainage ventral (Planche).jpg'],
];

@Component({
  selector: 'app-exercices',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './exercices-component.html',
  styleUrl: './exercices-component.css',
})
export class ExercicesComponent {
  private http = inject(HttpClient);

  all = signal<Exercice[]>([]);
  loading = signal(true);
  error = signal(false);
  filter = signal('ALL');
  query = signal('');
  failed = signal<Record<number, number>>({});
  broken = signal<Set<number>>(new Set());

  categories = computed(() => [
    'ALL',
    ...Array.from(new Set(this.all().map((e) => String(e.categorie).toUpperCase()))),
  ]);

  visible = computed(() => {
    const f = this.filter(),
      q = this.query().trim().toLowerCase();
    return this.all().filter(
      (e) =>
        (f === 'ALL' || String(e.categorie).toUpperCase() === f) &&
        (!q || e.nom.toLowerCase().includes(q) || (e.description ?? '').toLowerCase().includes(q)),
    );
  });

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set(false);
    this.http.get<Exercice[]>(`${API}/exercices`).subscribe({
      next: (d) => {
        this.all.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  private candidates(e: Exercice): string[] {
    const list: string[] = [];

    const img = (e.image ?? '').trim();
    if (img) {
      if (/^(https?:)?\/\//.test(img) || img.startsWith('/')) list.push(img);
      else list.push(`/images/${img}`);
    }

    const name = e.nom.toLowerCase();
    const byName = NAME_IMG.find(([key]) => name.includes(key));
    if (byName) list.push(`/images/${byName[1]}`);

    const cat = String(e.categorie).toUpperCase();
    list.push(`/images/${CATEGORY_IMG[cat] ?? 'workout-musculation.jpg'}`);

    return [...new Set(list.map(encodeURI))];
  }

  label(c: string) {
    return c === 'ALL' ? 'Tous' : (LABELS[c] ?? c);
  }

  icon(c: string) {
    return ICONS[String(c).toUpperCase()] ?? '💪';
  }

  src(e: Exercice): string | null {
    return this.candidates(e)[this.failed()[e.id] ?? 0] ?? null;
  }

  onImgError(id: number) {
    console.warn(`Error cargando imagen real para el ejercicio ID: ${id}`);
    this.failed.update((f) => ({ ...f, [id]: (f[id] ?? 0) + 1 }));
    this.broken.update((s) => new Set(s).add(id));
  }
}