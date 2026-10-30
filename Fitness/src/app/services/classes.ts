import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ClassSession, ClassTemplate, Attendee, BookingResult } from '../models/classes';

@Injectable({ providedIn: 'root' })
export class ClassesService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080/classes';

  sessions(from: string, to: string): Observable<ClassSession[]> {
    return this.http.get<ClassSession[]>(`${this.api}/sessions`, { params: { from, to } });
  }

  createSession(b: any): Observable<ClassSession> {
    return this.http.post<ClassSession>(`${this.api}/sessions`, b);
  }

  cancelSession(id: number): Observable<void> {
    return this.http.post<void>(`${this.api}/sessions/${id}/cancel`, {});
  }

  attendees(id: number): Observable<Attendee[]> {
    return this.http.get<Attendee[]>(`${this.api}/sessions/${id}/attendees`);
  }

  /** memberId solo lo usa el personal; el miembro reserva siempre para sí mismo. */
  book(id: number, memberId: number | null): Observable<BookingResult> {
    const url = memberId ? `${this.api}/sessions/${id}/book?memberId=${memberId}` : `${this.api}/sessions/${id}/book`;
    return this.http.post<BookingResult>(url, {});
  }

  cancelBooking(id: number): Observable<void> {
    return this.http.post<void>(`http://localhost:8080/api/bookings/${id}/cancel`, {});
  }

  templates(): Observable<ClassTemplate[]> {
    return this.http.get<ClassTemplate[]>(`${this.api}/templates`);
  }

  saveTemplate(id: number | null, b: any): Observable<ClassTemplate> {
    return id ? this.http.put<ClassTemplate>(`${this.api}/templates/${id}`, b) : this.http.post<ClassTemplate>(`${this.api}/templates`, b);
  }

  generate(): Observable<{ created: number }> {
    return this.http.post<{ created: number }>(`${this.api}/generate`, {});
  }
}