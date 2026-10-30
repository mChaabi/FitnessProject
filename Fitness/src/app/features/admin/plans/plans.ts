import { CurrencyPipe } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { inject } from '@angular/core'; // <-- ¡Corregido desde '@angular/core'!
import { BillingService } from '../../../services/billing';
import { Plan } from '../../../models/billing';

@Component({ selector: 'app-plans', standalone: true, imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './plans.html' })
export class PlansComponent {
  private svc = inject(BillingService);
  private fb = inject(FormBuilder);

  plans = signal<Plan[]>([]);
  editingId = signal<number | null>(null);
  error = signal('');

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(80)]],
    price: [0, [Validators.required, Validators.min(0.01)]],
    durationDays: [30, [Validators.required, Validators.min(1), Validators.max(3650)]],
    description: [''],
    active: [true]
  });

  constructor() { this.load(); }
  load() { this.svc.allPlans().subscribe(p => this.plans.set(p)); }

  edit(p: Plan) { this.editingId.set(p.id); this.form.patchValue({ ...p, description: p.description ?? '' }); }
  reset() { this.editingId.set(null); this.error.set(''); this.form.reset({ name: '', price: 0, durationDays: 30, description: '', active: true }); }

  save() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const body = this.form.getRawValue();
    const id = this.editingId();
    (id ? this.svc.updatePlan(id, body) : this.svc.createPlan(body)).subscribe({
      next: () => { this.reset(); this.load(); },
      error: e => this.error.set(e.error?.message ?? 'Erreur')
    });
  }

  toggle(p: Plan) { this.svc.updatePlan(p.id, { ...p, active: !p.active }).subscribe(() => this.load()); }
}