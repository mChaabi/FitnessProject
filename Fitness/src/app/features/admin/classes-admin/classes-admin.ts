import { Component } from '@angular/core';
import {  inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CoachingService } from '../../../services/coaching';
import { ClassTemplate } from '../../../models/classes';
import { Coach, DAYS } from '../../../models/coaching';
import { ymd } from '../../../core/dates';
import { ClassesService } from '../../../services/classes';

@Component({ selector: 'app-classes-admin', standalone: true, imports: [ReactiveFormsModule], templateUrl: './classes-admin.html' })
export class ClassesAdmin {
  private svc = inject(ClassesService);
  private coaching = inject(CoachingService);
  private fb = inject(FormBuilder);

  days = DAYS;
  templates = signal<ClassTemplate[]>([]);
  coaches = signal<Coach[]>([]);
  editingId = signal<number | null>(null);
  message = signal<{ ok: boolean; text: string } | null>(null);

  tForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]], description: [''],
    coachId: [0, Validators.min(1)], dayOfWeek: [1], startTime: ['18:00', Validators.required],
    durationMinutes: [60, [Validators.min(15), Validators.max(240)]],
    capacity: [15, [Validators.min(1), Validators.max(200)]], room: [''], active: [true]
  });

  sForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]], description: [''],
    coachId: [0, Validators.min(1)], date: [ymd(new Date()), Validators.required], startTime: ['18:00', Validators.required],
    durationMinutes: [60, [Validators.min(15), Validators.max(240)]],
    capacity: [15, [Validators.min(1), Validators.max(200)]], room: ['']
  });

  constructor() {
    this.load();
    this.coaching.coaches().subscribe(c => this.coaches.set(c.filter(x => x.active)));
  }
  load() { this.svc.templates().subscribe(t => this.templates.set(t)); }

  dayLabel(n: number) { return DAYS.find(d => d.n === n)?.label ?? ''; }

  edit(t: ClassTemplate) {
    this.editingId.set(t.id);
    this.tForm.patchValue({ ...t, description: t.description ?? '', room: t.room ?? '', startTime: t.startTime.substring(0, 5) });
  }
  reset() { this.editingId.set(null); this.tForm.reset({ name: '', description: '', coachId: 0, dayOfWeek: 1,
    startTime: '18:00', durationMinutes: 60, capacity: 15, room: '', active: true }); }

  saveTemplate() {
    if (this.tForm.invalid) { this.tForm.markAllAsTouched(); return; }
    const v = this.tForm.getRawValue();
    this.svc.saveTemplate(this.editingId(), { ...v, description: v.description || null, room: v.room || null }).subscribe({
      next: () => { this.reset(); this.load(); this.say(true, 'Modèle enregistré, séances générées'); },
      error: e => this.say(false, this.errorText(e))
    });
  }
  toggle(t: ClassTemplate) {
    this.svc.saveTemplate(t.id, { ...t, startTime: t.startTime.substring(0, 5), active: !t.active }).subscribe(() => this.load());
  }

  saveSession() {
    if (this.sForm.invalid) { this.sForm.markAllAsTouched(); return; }
    const v = this.sForm.getRawValue();
    this.svc.createSession({ ...v, description: v.description || null, room: v.room || null }).subscribe({
      next: () => this.say(true, 'Séance créée'),
      error: e => this.say(false, this.errorText(e))
    });
  }

  generate() { this.svc.generate().subscribe(r => this.say(true, `${r.created} séance(s) créée(s)`)); }

  private say(ok: boolean, text: string) { this.message.set({ ok, text }); }
  private errorText(e: any) { return e.error?.fields ? Object.values(e.error.fields).join(' · ') : (e.error?.message ?? 'Erreur'); }
}