import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ClassSession, ClassTemplate, Attendee, BookingResult } from '../classes';

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
    return this.http.get<Attendee[]>(`${this.api}/sessions/${id}/bookings`);        // was /attendees (404)
  }

    book(id: number, memberId: number | null): Observable<BookingResult> {
    return this.http.post<BookingResult>(`${this.api}/sessions/${id}/book`, memberId ? { memberId } : {});
  }

    cancelBooking(id: number): Observable<void> {
    return this.http.post<void>(`${this.api}/bookings/${id}/cancel`, {});           // was /api/bookings/... (404)
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

    /** AI assistant: returns a short reply and the ids of suggested sessions. */
  suggest(message: string): Observable<{ reply: string; sessionIds: number[]; ai: boolean }> {
    return this.http.post<{ reply: string; sessionIds: number[]; ai: boolean }>(
      'http://localhost:8080/agents/class-suggestions', { message });
  }
}