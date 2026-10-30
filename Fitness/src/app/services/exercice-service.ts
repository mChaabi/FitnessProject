import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Exercice } from '../models/exercice';

@Injectable({ providedIn: 'root' })
export class ExerciceService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/exercices';

  // Signal pour stocker la liste de manière réactive
  exercices = signal<Exercice[]>([]);

  chargerTout() {
    this.http.get<Exercice[]>(this.apiUrl).subscribe((data: Exercice[]) => {
      this.exercices.set(data);
    });
  }
}
