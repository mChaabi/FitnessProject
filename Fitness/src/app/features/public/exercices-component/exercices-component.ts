import { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Exercice } from '../../../data/public/exercice';
import { API } from '../../../core/api';

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

  all = signal<Exercice[]>([]);
  loading = signal(true);
  error = signal(false);
  filter = signal('ALL');
  query = signal('');
  broken = signal<Set<number>>(new Set());

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
    this.http.get<Exercice[]>(`${API}/exercices`).subscribe({
      next: d => { this.all.set(d); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }

  label(c: string) { return c === 'ALL' ? 'Tous' : (LABELS[c] ?? c); }
  icon(c: string)  { return ICONS[String(c).toUpperCase()] ?? '💪'; }

  src(e: Exercice): string | null {
    if (this.broken().has(e.id)) return null;

    if (e.image) {
      return e.image.startsWith('http') ? e.image : `/exercices/${e.image}`;
    }

    const name = e.nom.toLowerCase();
    let filename = '';

    // Coincidencia exacta con los nombres de tus archivos reales en public/images/
    if (name.includes('burpee')) {
      filename = 'workout-hiit.jpg';
    } else if (name.includes('pompes')) {
      filename = 'Pompes classiques.jpg';
    } else if (name.includes('squat')) {
      filename = 'Squats libres.jpg';
    } else if (name.includes('tapis') || name.includes('course')) {
      filename = 'Course sur tapis.jpg';
    } else if (name.includes('traction')) {
      filename = 'Tractions à la barre fixe.jpg';
    } else if (name.includes('gainage') || name.includes('planche')) {
      filename = 'Gainage ventral (Planche).jpg';
    } else {
      // Respaldo por categoría usando las imágenes de workout que tienes
      const categoryImages: Record<string, string> = {
        CARDIO: 'workout-cardio.jpg',
        MUSCULATION: 'workout-musculation.jpg',
        YOGA: 'workout-yoga.jpg',
        HIIT: 'workout-hiit.jpg',
        GAINAGE: 'workout-musculation.jpg'
      };
      filename = categoryImages[String(e.categorie).toUpperCase()] ?? 'workout-musculation.jpg';
    }

    // encodeURI convierte los espacios y tildes (à) en formato web válido (ej. %20)
    return encodeURI(`/images/${filename}`);
  }

  onImgError(id: number) { 
    console.warn(`Error cargando imagen real para el ejercicio ID: ${id}`);
    this.broken.update(s => new Set(s).add(id)); 
  }
}