import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Measurement } from '../models/progress'; // Ajusta la ruta de tu modelo si es necesario

@Injectable({
  providedIn: 'root'
})
export class ProgressService {
  private http = inject(HttpClient);
  private API = 'http://localhost:8080'; 

  /** memberId null = el propio miembro conectado */
  list(memberId: number | null): Observable<Measurement[]> {
    const url = memberId ? `${this.API}/members/${memberId}/measurements` : `${this.API}/progress/me`;
    return this.http.get<Measurement[]>(url);
  }

  add(memberId: number | null, body: any): Observable<Measurement> {
    const url = memberId ? `${this.API}/members/${memberId}/measurements` : `${this.API}/progress/me`;
    return this.http.post<Measurement>(url, body);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/measurements/${id}`);
  }
}