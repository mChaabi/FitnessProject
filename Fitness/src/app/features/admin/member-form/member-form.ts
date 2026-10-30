import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MemberService } from '../../../services/member';

@Component({
  selector: 'app-member-form', standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './member-form.html', styleUrl: './member-form.css'
})
export class MemberFormComponent {
  private fb = inject(FormBuilder);
  private svc = inject(MemberService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id = Number(this.route.snapshot.paramMap.get('id')) || null;
  saving = signal(false);
  serverError = signal('');
  fieldErrors = signal<Record<string, string>>({});

  form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName:  ['', [Validators.required, Validators.maxLength(50)]],
    email:     ['', [Validators.required, Validators.email]],
    phone:     [''],
    birthDate: [''],
    sex:       ['' as '' | 'MALE' | 'FEMALE' | 'OTHER'],
    goal:      ['']
  });

  constructor() {
    if (this.id) this.svc.get(this.id).subscribe(m => this.form.patchValue({
      ...m, phone: m.phone ?? '', birthDate: m.birthDate ?? '', sex: m.sex ?? '', goal: m.goal ?? '' }));
  }

  save() {
    if (this.form.invalid || this.saving()) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.serverError.set(''); this.fieldErrors.set({});

    const v = this.form.getRawValue();
    const body = { ...v, phone: v.phone || undefined, birthDate: v.birthDate || undefined,
                   sex: v.sex || undefined, goal: v.goal || undefined } as any;

    const req = this.id ? this.svc.update(this.id, body) : this.svc.create(body);
    req.subscribe({
      next: m => this.router.navigate(['/admin/members', m.id]),
      error: e => {
        this.saving.set(false);
        this.fieldErrors.set(e.error?.fields ?? {});
        this.serverError.set(e.error?.message ?? 'Erreur inattendue');
      }
    });
  }
}
