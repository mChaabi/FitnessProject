export interface Exercice {
  id: number;
  nom: string;
  description: string;
  duree: number; // minutes
  categorie: 'Cardio' | 'Musculation' | 'Yoga';
  image: string;
}
