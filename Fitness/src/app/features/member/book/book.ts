import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookingResult, ClassSession } from '../../../data/classes/classes';
import { ClassesService } from '../../../data/classes/service/classes';
import { addDays, ymd } from '../../../core/dates';

@Component({
  selector: 'app-book', standalone: true, imports: [RouterLink, DatePipe],
  templateUrl: './book.html', styleUrl: './book.css'
})
export class BookPage {
  private svc = inject(ClassesService);

  labels = ['Cours', 'Séance', 'Confirmation'];
  step = signal<1 | 2 | 3 | 4>(1);
  all = signal<ClassSession[]>([]);
  loading = signal(true);
  busy = signal(false);
  error = signal('');
  name = signal<string | null>(null);      // chosen class type, e.g. "Body Attack"
  coach = signal('');                      // '' = every coach
  picked = signal<ClassSession | null>(null);
  result = signal<BookingResult | null>(null);
    // ---------- AI assistant ----------
  ask = signal('');
  asking = signal(false);
  aiReply = signal<{ text: string; ai: boolean } | null>(null);
  aiError = signal('');
  picks = signal<ClassSession[]>([]);
  ideas = ['Un cours de cardio le soir', 'Du yoga demain matin', 'Un cours pour me défouler ce week-end'];

  private bookable = computed(() => this.all().filter(s => s.bookable));

  /** One card per class name, with the next date and the coaches who teach it. */
  types = computed(() => {
    const m = new Map<string, { name: string; count: number; coaches: Set<string>; next: ClassSession }>();
    for (const s of this.bookable()) {
      const t = m.get(s.name) ?? { name: s.name, count: 0, coaches: new Set<string>(), next: s };
      t.count++; t.coaches.add(s.coachName); m.set(s.name, t);
    }
    return [...m.values()].map(t => ({ ...t, coaches: [...t.coaches] }));
  });

  coachesOf = computed(() => this.types().find(t => t.name === this.name())?.coaches ?? []);

  days = computed(() => {
    const n = this.name(), c = this.coach();
    const by = new Map<string, ClassSession[]>();
    this.bookable().filter(s => s.name === n && (!c || s.coachName === c))
      .forEach(s => by.set(s.date, [...(by.get(s.date) ?? []), s]));
    return [...by.entries()].map(([key, items]) => ({
      key, items,
      label: new Date(key + 'T00:00:00').toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })
    }));
  });

  constructor() { this.load(); }

  load() {
    const today = new Date();
    this.loading.set(true);
    this.svc.sessions(ymd(today), ymd(addDays(today, 13))).subscribe({
      next: s => { this.all.set(s); this.loading.set(false); },
      error: e => { this.error.set(e.error?.message ?? 'Chargement impossible'); this.loading.set(false); }
    });
  }

  pickType(n: string) { this.name.set(n); this.coach.set(''); this.error.set(''); this.step.set(2); }
  pickSession(s: ClassSession) { this.picked.set(s); this.error.set(''); this.step.set(3); }
  back() { this.error.set(''); this.step.set(this.step() === 3 ? 2 : 1); }

  confirm() {
    const s = this.picked();
    if (!s || this.busy()) return;                 // one click at a time: no double booking
    this.busy.set(true); this.error.set('');
    this.svc.book(s.id, null).subscribe({
      next: r => { this.result.set(r); this.busy.set(false); this.step.set(4); },
      error: e => { this.busy.set(false); this.error.set(e.error?.message ?? 'Réservation impossible'); }
    });
  }

  restart() {
    this.name.set(null); this.picked.set(null); this.result.set(null); this.error.set('');
    this.step.set(1); this.load();
  }

  free(s: ClassSession) { return Math.max(s.capacity - s.confirmed, 0); }
  fill(s: ClassSession) { return Math.min(100, Math.round((s.confirmed * 100) / s.capacity)); }
  time(t: string) { return t.substring(0, 5); }
  initials(n: string) { return n.split(' ').filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase(); }

  askAssistant(text: string = this.ask()) {
    const q = text.trim();
    if (q.length < 3 || this.asking()) return;
    this.ask.set(q);
    this.asking.set(true); this.aiError.set(''); this.aiReply.set(null); this.picks.set([]);

    this.svc.suggest(q).subscribe({
      next: r => {
        // The AI sends ids only: details come from the sessions this page already loaded.
        const byId = new Map(this.bookable().map(s => [s.id, s]));
        this.picks.set(r.sessionIds.map(id => byId.get(id)).filter((s): s is ClassSession => !!s));
        this.aiReply.set({ text: r.reply, ai: r.ai });
        this.asking.set(false);
      },
      error: e => {
        this.asking.set(false);
        this.aiError.set(e.error?.message ?? "L'assistant est indisponible. Utilisez la liste ci-dessous.");
      }
    });
  }

  pickSuggestion(s: ClassSession) {
    this.name.set(s.name); this.coach.set('');   // so "Retour" lands on the right class
    this.pickSession(s);                         // goes to step 3: the member still confirms
  }
}