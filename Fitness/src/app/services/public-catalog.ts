import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Plan } from '../models/billing';

const API = 'http://localhost:8080';
export interface PublicCoach { id: number; prenom: string; nom: string; }

@Injectable({ providedIn: 'root' })
export class PublicCatalogService {
  private http = inject(HttpClient);
  plans()   { return this.http.get<Plan[]>(`${API}/plans/public`); }
  coaches() { return this.http.get<PublicCoach[]>(`${API}/coaches/public`); }
}