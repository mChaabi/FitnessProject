import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Plan } from '../../billing/billing';

const API = 'http://localhost:8080';

// ✅ Interfaz correcta que coincide con el record de Java
export interface PublicCoach { 
  id: number; 
  name: string; 
  specialties?: string; 
  bio?: string; 
}

export interface PublicSession {
  id: number;
  name: string;
  description?: string;
  coachName: string;
  date: string;
  startTime: string;
  endTime: string;
  room?: string;
  capacity: number;
  freeSpots: number;
}

@Injectable({ providedIn: 'root' })
export class PublicCatalogService {
  private http = inject(HttpClient);
  
  plans() { 
    return this.http.get<Plan[]>(`${API}/plans/public`); 
  }
  
  sessions(from: string, to: string) { 
    return this.http.get<PublicSession[]>(`${API}/public/classes`, { params: { from, to } }); 
  }
  
  coaches() { 
    return this.http.get<PublicCoach[]>(`${API}/public/coaches`); 
  }
}