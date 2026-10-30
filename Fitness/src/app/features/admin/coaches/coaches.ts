import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CoachingService } from '../../../services/coaching';
import { Coach } from '../../../models/coaching';

@Component({ selector: 'app-coaches', standalone: true, imports: [ReactiveFormsModule], templateUrl: './coaches.html' })
export class CoachesComponent {
  private svc = inject(CoachingService);
  private fb = inject(FormBuilder);
  coaches = signal<Coach[]>([]);
  error = signal('');

  form = this.fb.nonNullable.group({
    prenom: ['', Validators.required], nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(8)]],
    specialties: [''], bio: ['']
  });

  constructor() { this.load(); }
  load() { this.svc.coaches().subscribe(c => this.coaches.set(c)); }

  save() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.error.set('');
    this.svc.createCoach(this.form.getRawValue()).subscribe({
      next: () => { this.form.reset(); this.load(); },
      error: e => this.error.set(e.error?.fields ? Object.values(e.error.fields).join(' · ') : (e.error?.message ?? 'Erreur'))
    });
  }

  toggle(c: Coach) {
    const msg = c.active ? 'Désactiver ce coach ? Ses clients seront libérés.' : 'Réactiver ce coach ?';
    if (confirm(msg)) this.svc.setCoachActive(c.id, !c.active).subscribe(() => this.load());
  }
}