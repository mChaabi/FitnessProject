import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ContactService } from '../../../data/public/services/contact-service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './contact-component.html',
  styleUrl: './contact-component.css'
})
export class ContactComponent {
  private fb = inject(FormBuilder);
  private contactService = inject(ContactService);
  private route = inject(ActivatedRoute);

  // Création du formulaire avec validations
  contactForm = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    sujet: ['', Validators.required],
    message: ['', [Validators.required, Validators.minLength(1)]]
  });

  

  constructor() {
    // Read the query parameter 'plan' from the URL
    const plan = this.route.snapshot.queryParamMap.get('plan');
    
    // If 'plan' exists, automatically patch the message field
    if (plan) {
      this.contactForm.patchValue({
        message: `Bonjour, je souhaite m'inscrire : ${plan}.`
      });
    }
  }

 onSubmit() {
  if (this.contactForm.valid) {
    // 👇 Incluye 'sujet' aquí
    const { nom, email, sujet, message } = this.contactForm.value;
    const data = { nom, email, sujet, message };

    this.contactService.envoyerMessage(data).subscribe({
      next: (res: any) => {
        alert('Succès! Message envoyé.');
        this.contactForm.reset(); // Opcional: limpia el formulario
      },
      error: (err: any) => {
        console.log("Erreurs du backend:", err.error);
        alert('Veuillez vérifier les champs du formulaire');
      }
    });
  }
}
}
