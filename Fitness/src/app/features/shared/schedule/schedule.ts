import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, debounceTime, distinctUntilChanged } from 'rxjs';
import { AuthService } from '../../../services/auth-service';
import { MemberService } from '../../../services/member';
import { Member } from '../../../models/member';
import { Attendee, BookingResult, ClassSession } from '../../../models/classes';
import { addDays, mondayOf, ymd } from '../../../core/dates';
import { ClassesService } from '../../../services/classes';

@Component({
  selector: 'app-schedule', standalone: true, imports: [ReactiveFormsModule],
  templateUrl: './schedule.html', styleUrl: './schedule.css'
})
export class SchedulePage {
  auth = inject(AuthService);
  private svc = inject(ClassesService);
  private members = inject(MemberService);

  weekStart = signal(mondayOf(new Date()));
  sessions = signal<ClassSession[]>([]);
  loading = signal(true);
  busy = signal<number | null>(null);
  message = signal<{ ok: boolean; text: string } | null>(null);

  open = signal<ClassSession | null>(null);        // sesión cuyo panel de participantes está abierto
  attendees = signal<Attendee[]>([]);
  found = signal<Member[]>([]);
  search = new FormControl('', { nonNullable: true });

  isMember = computed(() => this.auth.role() === 'MEMBER');
  isStaff = computed(() => ['ADMIN', 'RECEPTION'].includes(this.auth.role() ?? ''));
  isAdmin = computed(() => this.auth.role() === 'ADMIN');
  canSeeAttendees = computed(() => ['ADMIN', 'RECEPTION', 'COACH'].includes(this.auth.role() ?? ''));

  title = computed(() => {
    const f = (d: Date) => d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
    return `${f(this.weekStart())} – ${f(addDays(this.weekStart(), 6))}`;
  });

  days = computed(() => Array.from({ length: 7 }, (_, i) => {
    const d = addDays(this.weekStart(), i), key = ymd(d);
    return { key, label: d.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'short' }),
             today: key === ymd(new Date()), sessions: this.sessions().filter(s => s.date === key) };
  }));

  constructor() {
    this.load();
    this.search.valueChanges.pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed()).subscribe(q => {
      if (q.trim().length < 2) { this.found.set([]); return; }
      this.members.list(q.trim(), '', 0, 5).subscribe(p => this.found.set(p.content));
    });
  }

  load() {
    const from = ymd(this.weekStart()), to = ymd(addDays(this.weekStart(), 6));
    this.loading.set(true);
    this.svc.sessions(from, to).subscribe({
      next: s => { this.sessions.set(s); this.loading.set(false); },
      error: e => { this.loading.set(false); this.message.set({ ok: false, text: e.error?.message ?? 'Chargement impossible' }); }
    });
  }
  shift(n: number) { this.weekStart.set(addDays(this.weekStart(), n * 7)); this.load(); }
  thisWeek() { this.weekStart.set(mondayOf(new Date())); this.load(); }

  // ---------- acciones del miembro ----------
  book(s: ClassSession) {
    this.run(s.id, this.svc.book(s.id, null),
      (r: BookingResult) => r.status === 'CONFIRMED' ? 'Réservation confirmée' : `Liste d'attente : position ${r.position}`);
  }
  cancel(s: ClassSession) {
    const waiting = s.myStatus === 'WAITLIST';
    if (!confirm(waiting ? 'Quitter la liste d\'attente ?' : 'Annuler cette réservation ?')) return;
    this.run(s.id, this.svc.cancelBooking(s.myBookingId!), () => waiting ? 'Vous avez quitté la liste' : 'Réservation annulée');
  }

  // ---------- acciones del personal ----------
  showAttendees(s: ClassSession) { this.open.set(s); this.found.set([]); this.refreshAttendees(); }
  closePanel() { this.open.set(null); }
  refreshAttendees() { const s = this.open(); if (s) this.svc.attendees(s.id).subscribe(a => this.attendees.set(a)); }

  addMember(m: Member) {
    const s = this.open(); if (!s) return;
    this.found.set([]); this.search.setValue('', { emitEvent: false });
    this.run(s.id, this.svc.book(s.id, m.id),
      (r: BookingResult) => `${m.firstName} : ${r.status === 'CONFIRMED' ? 'confirmé' : 'en liste d\'attente'}`);
  }
  removeAttendee(a: Attendee) {
    if (confirm(`Retirer ${a.memberName} ?`)) this.run(this.open()!.id, this.svc.cancelBooking(a.bookingId), () => 'Inscription retirée');
  }
  cancelSession(s: ClassSession) {
    if (!confirm(`Annuler « ${s.name} » ? Toutes les réservations seront annulées.`)) return;
    this.run(s.id, this.svc.cancelSession(s.id), () => 'Séance annulée');
  }

  /** Un solo clic a la vez: evita dobles reservas. */
  private run<T>(id: number, call: Observable<T>, ok: (r: T) => string) {
    if (this.busy() !== null) return;
    this.busy.set(id); this.message.set(null);
    call.subscribe({
      next: r => { this.busy.set(null); this.message.set({ ok: true, text: ok(r) }); this.load(); this.refreshAttendees(); },
      error: e => { this.busy.set(null); this.message.set({ ok: false, text: e.error?.message ?? 'Erreur' }); }
    });
  }

  time(t: string) { return t.substring(0, 5); }
  free(s: ClassSession) { return Math.max(s.capacity - s.confirmed, 0); }
}