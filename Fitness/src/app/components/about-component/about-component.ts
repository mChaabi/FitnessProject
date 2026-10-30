import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-about', standalone: true, imports: [RouterLink],
  templateUrl: './about-component.html', styleUrl: './about-component.css'
})
export class AboutComponent {
  values = [
    { icon: '🎯', title: 'Résultats', text: 'Chaque programme part de votre objectif et se mesure.' },
    { icon: '🤝', title: 'Accompagnement', text: 'Un coach dédié, disponible et à l\'écoute.' },
    { icon: '🔒', title: 'Transparence', text: 'Des tarifs clairs, sans frais cachés.' },
  ];
  team = [
    { name: 'Dr. Sarah', role: 'Coach Nutrition', icon: '🥗' },
    { name: 'Marc Fitness', role: 'Expert Musculation', icon: '🏋️' },
    { name: 'Léa Zen', role: 'Professeur Yoga', icon: '🧘' },
  ];
}