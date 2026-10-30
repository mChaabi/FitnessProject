import { Component, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CoachingService } from '../../../services/coaching';
import { DAYS, ProgramItem } from '../../../models/coaching';
import { Exercice } from '../../../models/exercice';

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