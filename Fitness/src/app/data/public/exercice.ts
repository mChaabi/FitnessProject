import { PublicSession } from './public-catalog';

export interface Exercice {
  id: number;
  nom: string;
  description: string;
  duree: number; // minutes
  categorie: 'Cardio' | 'Musculation' | 'Yoga';
  image: string;
  objectif?: string | null;
  instructions?: string | null;   // one step per line
  muscles?: string | null;        // "Pectoraux, Triceps"
}

export interface ExerciceDetail extends Exercice {
  sessions: PublicSession[];
}