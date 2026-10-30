import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ContactService } from '../../services/contact-service';

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

  // Création du formulaire avec validations
  contactForm = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    sujet: ['', Validators.required],
    message: ['', [Validators.required, Validators.minLength(1)]]
  });

  onSubmit() {
    if (this.contactForm.valid) {
      const { nom, email, message } = this.contactForm.value;
      const body = { nom, email, message };
      const data =body

      this.contactService.envoyerMessage(data).subscribe({
        next: (res: any) => alert('Succès!'),
        error: (err: any) => {
          // err.error contiene el mapa de mensajes: {nom: "...", email: "..."}
          console.log("Erreurs du backend:", err.error);
          alert('Veuillez vérifier les champs du formulaire');
        }
      });
    }
  }
}
