import { Component, DestroyRef, HostListener, PLATFORM_ID, computed, effect, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Exercice, ExerciceDetail } from '../../../data/public/exercice';
import { PublicSession } from '../../../data/public/public-catalog';
import { AuthService } from '../../../core/auth/auth-service';
import { API } from '../../../core/api';

const LABELS: Record<string, string> = {
  CARDIO: 'Cardio', MUSCULATION: 'Musculation', YOGA: 'Yoga', HIIT: 'HIIT', GAINAGE: 'Gainage', STRETCHING: 'Étirements'
};
const ICONS: Record<string, string> = {
  CARDIO: '🏃', MUSCULATION: '🏋️', YOGA: '🧘', HIIT: '🔥', GAINAGE: '🧱', STRETCHING: '🤸'
};
const CATEGORY_IMG: Record<string, string> = {
  CARDIO: 'workout-cardio.jpg', MUSCULATION: 'workout-musculation.jpg', YOGA: 'workout-yoga.jpg',
  HIIT: 'workout-hiit.jpg', GAINAGE: 'workout-musculation.jpg',
};
const NAME_IMG: [string, string][] = [
  ['burpee', 'workout-hiit.jpg'], ['pompes', 'Pompes classiques.jpg'], ['squat', 'Squats libres.jpg'],
  ['tapis', 'Course sur tapis.jpg'], ['course', 'Course sur tapis.jpg'],
  ['traction', 'Tractions à la barre fixe.jpg'], ['gainage', 'Gainage ventral (Planche).jpg'],
  ['planche', 'Gainage ventral (Planche).jpg'],
];

@Component({
  selector: 'app-exercices',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './exercices-component.html',
  styleUrl: './exercices-component.css'
})
export class ExercicesComponent {
  private http = inject(HttpClient);
  private browser = isPlatformBrowser(inject(PLATFORM_ID));
  auth = inject(AuthService);

  all = signal<Exercice[]>([]);
  loading = signal(true);
  error = signal(false);
  filter = signal('ALL');
  query = signal('');
  private failed = signal<Record<number, number>>({});

  // ---------- detail panel ----------
  selected = signal<Exercice | null>(null);
  detail = signal<ExerciceDetail | null>(null);
  detailLoading = signal(false);
  detailError = signal(false);

  steps = computed(() => this.split(this.detail()?.instructions, '\n'));
  muscleList = computed(() => this.split(this.detail()?.muscles, ','));

  categories = computed(() =>
    ['ALL', ...Array.from(new Set(this.all().map(e => String(e.categorie).toUpperCase())))]);

  visible = computed(() => {
    const f = this.filter(), q = this.query().trim().toLowerCase();
    return this.all().filter(e =>
      (f === 'ALL' || String(e.categorie).toUpperCase() === f) &&
      (!q || e.nom.toLowerCase().includes(q) || (e.description ?? '').toLowerCase().includes(q)));
  });

  constructor() {
    this.load();
    effect(() => {                                   // lock the page scroll while the panel is open
      const open = this.selected() !== null;
      if (this.browser) document.body.style.overflow = open ? 'hidden' : '';
    });
    inject(DestroyRef).onDestroy(() => { if (this.browser) document.body.style.overflow = ''; });
  }

  load() {
    this.loading.set(true); this.error.set(false);
    this.http.get<Exercice[]>(`${API}/exercices`).subscribe({
      next: d => { this.all.set(d); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
  }

  open(e: Exercice) {
    this.selected.set(e); this.detail.set(null);
    this.detailError.set(false); this.detailLoading.set(true);
    this.http.get<ExerciceDetail>(`${API}/exercices/${e.id}`).subscribe({
      next: d => { if (this.selected()?.id !== e.id) return; this.detail.set(d); this.detailLoading.set(false); },
      error: () => { if (this.selected()?.id !== e.id) return; this.detailError.set(true); this.detailLoading.set(false); }
    });
  }

  @HostListener('document:keydown.escape')
  close() { this.selected.set(null); }

  when(s: PublicSession) {
    return new Date(s.date + 'T00:00:00').toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' });
  }
  time(t: string) { return t.substring(0, 5); }

  private split(text: string | null | undefined, sep: string): string[] {
    return (text ?? '').split(sep).map(s => s.trim()).filter(Boolean);
  }

  label(c: string) { return c === 'ALL' ? 'Tous' : (LABELS[c] ?? c); }
  icon(c: string) { return ICONS[String(c).toUpperCase()] ?? '💪'; }

  // ---------- images: DB image, then by name, then by category, then emoji ----------
  private candidates(e: Exercice): string[] {
    const list: string[] = [];
    const img = (e.image ?? '').trim();
    if (img) list.push(/^(https?:)?\/\//.test(img) || img.startsWith('/') ? img : `/images/${img}`);
    const name = e.nom.toLowerCase();
    const byName = NAME_IMG.find(([key]) => name.includes(key));
    if (byName) list.push(`/images/${byName[1]}`);
    list.push(`/images/${CATEGORY_IMG[String(e.categorie).toUpperCase()] ?? 'workout-musculation.jpg'}`);
    return [...new Set(list.map(encodeURI))];
  }

  src(e: Exercice): string | null { return this.candidates(e)[this.failed()[e.id] ?? 0] ?? null; }
  onImgError(id: number) { this.failed.update(f => ({ ...f, [id]: (f[id] ?? 0) + 1 })); }
}