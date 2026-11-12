import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CoachingService } from '../../../data/coaching/service/coaching';
import { DAYS, ProgramItem } from '../../../data/coaching/coaching';
import { Exercice } from '../../../data/public/exercice';

@Component({ selector: 'app-program-form', standalone: true, imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './program-form.html' })
export class ProgramForm {
  private fb = inject(FormBuilder);
  private svc = inject(CoachingService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  id = Number(this.route.snapshot.paramMap.get('id')) || null;
  memberId = Number(this.route.snapshot.queryParamMap.get('memberId')) || null;
  days = DAYS;
  exercices = signal<Exercice[]>([]);
  error = signal('');
  saving = signal(false);
    levels = [{ k: 'BEGINNER', l: 'Débutant' }, { k: 'INTERMEDIATE', l: 'Intermédiaire' }, { k: 'ADVANCED', l: 'Avancé' }];
  ai = this.fb.nonNullable.group({ level: ['BEGINNER'], minutes: [60, [Validators.min(20), Validators.max(120)]] });
  aiDays = signal<number[]>([1, 3, 5]);
  aiBusy = signal(false);
  aiMsg = signal<{ ok: boolean; text: string } | null>(null);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    goal: [''],
    startDate: [new Date().toISOString().substring(0, 10), Validators.required],
    active: [true],
    items: new FormArray<FormGroup>([])
  });
  get items() { return this.form.controls.items; }

  constructor() {
    this.svc.exercices().subscribe(e => this.exercices.set(e));
    if (this.id) {
      this.svc.program(this.id).subscribe(p => {
        this.memberId = p.memberId;
        this.form.patchValue({ name: p.name, goal: p.goal ?? '', startDate: p.startDate, active: p.active });
        p.items.forEach(i => this.addItem(i));
      });
    } else { this.addItem(); }
  }

  addItem(v: Partial<ProgramItem> = {}) {
    this.items.push(this.fb.nonNullable.group({
      exerciceId: [v.exerciceId ?? 0, [Validators.required, Validators.min(1)]],
      dayOfWeek: [v.dayOfWeek ?? 1],
      sets: [v.sets ?? 3, [Validators.required, Validators.min(1), Validators.max(20)]],
      reps: [v.reps ?? 10, [Validators.required, Validators.min(1), Validators.max(100)]],
      restSeconds: [v.restSeconds ?? 60, [Validators.required, Validators.min(0), Validators.max(600)]],
      notes: [v.notes ?? '']
    }));
  }
  removeItem(i: number) { this.items.removeAt(i); }


    toggleDay(n: number) {
    const d = this.aiDays();
    this.aiDays.set(d.includes(n) ? d.filter(x => x !== n) : [...d, n].sort());
  }
  generate() {
    if (!this.memberId || this.aiBusy() || this.ai.invalid || !this.aiDays().length || this.aiDays().length > 6) return;
    this.aiBusy.set(true); this.aiMsg.set(null);
    const v = this.ai.getRawValue();
    this.svc.draft({ memberId: this.memberId, level: v.level, days: this.aiDays(), minutesPerSession: v.minutes }).subscribe({
      next: r => {
        this.aiBusy.set(false);
        if (!r.ready) { this.aiMsg.set({ ok: false, text: 'Brouillon non valide : ' + r.errors.join(' ; ') }); return; }
        this.form.patchValue({ name: r.name ?? '', goal: r.goal ?? '' });
        this.items.clear();
        r.items.forEach(i => this.addItem({ ...i, notes: i.notes ?? undefined }));
        this.aiMsg.set({ ok: true, text: `Brouillon généré (${r.attempts} essai(s)). Relisez et modifiez avant d'enregistrer.` });
      },
      error: e => { this.aiBusy.set(false);
        this.aiMsg.set({ ok: false, text: e.error?.message ?? 'Génération impossible, créez le programme manuellement.' }); }
    });}
  save() {
    if (this.form.invalid || this.items.length === 0 || this.saving() || !this.memberId) {
      this.form.markAllAsTouched(); return;
    }
    this.saving.set(true); this.error.set('');
    const v = this.form.getRawValue();
    const body = { memberId: this.memberId, name: v.name, goal: v.goal || null, startDate: v.startDate,
                   active: v.active, items: v.items.map((i: any) => ({ ...i, notes: i.notes || null })) };
    this.svc.saveProgram(this.id, body).subscribe({
      next: () => this.router.navigate(['/coach/clients']),
      error: e => { this.saving.set(false); this.error.set(e.error?.message ?? 'Erreur'); }
    });
  }
}