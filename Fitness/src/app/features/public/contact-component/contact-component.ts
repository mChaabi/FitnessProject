import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ContactsService } from '../../../data/contacts/service/contacts';

@Component({
  selector: 'app-contact', standalone: true, imports: [ReactiveFormsModule],
  templateUrl: './contact-component.html', styleUrl: './contact-component.css'
})
export class ContactComponent {
  private fb = inject(FormBuilder);
  private contactService = inject(ContactsService);
  private route = inject(ActivatedRoute);

  state = signal<'idle' | 'sending' | 'sent' | 'error'>('idle');
  errorMsg = signal('');

  contactForm = this.fb.nonNullable.group({
    nom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    sujet: ['', Validators.required],
    message: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
    website: ['']                                     // honeypot (hidden): must stay empty
  });

  constructor() {
    const plan = this.route.snapshot.queryParamMap.get('plan');
    if (plan) this.contactForm.patchValue({ message: `Bonjour, je souhaite m'inscrire : ${plan}.` });
  }

  onSubmit() {
    if (this.contactForm.invalid || this.state() === 'sending') { this.contactForm.markAllAsTouched(); return; }
    this.state.set('sending'); this.errorMsg.set('');
    this.contactService.envoyerMessage(this.contactForm.getRawValue()).subscribe({
      next: () => { this.state.set('sent'); this.contactForm.reset({ sujet: '' }); },
      error: (e: any) => {
        this.state.set('error');
        this.errorMsg.set(e.status === 429 ? (e.error?.message ?? 'Trop de messages. Réessayez plus tard.')
          : e.error && typeof e.error === 'object' && !e.error.message ? Object.values(e.error).join(' · ')
          : 'Envoi impossible. Vérifiez les champs et réessayez.');
      }
    });
  }
}