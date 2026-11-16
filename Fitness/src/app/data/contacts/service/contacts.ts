import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { API } from '../../../core/api';
import { Page } from '../../members/member';
import { ContactRow, ContactStatus } from '../contacts';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({ providedIn: 'root' })
export class ContactsService {
  private http = inject(HttpClient);
  readonly unread = signal(0);                 // shown as a badge in the sidebar

  list(status: string, page: number) {
    let params = new HttpParams().set('page', page).set('size', 20);
    if (status) params = params.set('status', status);
    return this.http.get<Page<ContactRow>>(`${API}/admin/messages`, { params });
  }
  stats() { return this.http.get<Record<string, number>>(`${API}/admin/messages/stats`); }
  setStatus(id: number, value: ContactStatus) {
    return this.http.patch<ContactRow>(`${API}/admin/messages/${id}/status`, null, { params: { value } });
  }
  remove(id: number) { return this.http.delete<void>(`${API}/admin/messages/${id}`); }

  refresh() { this.stats().subscribe({ next: s => this.unread.set(s['NEW'] ?? 0), error: () => {} }); }

envoyerMessage(contactData: any): Observable<any> {
    return this.http.post<any>(`${API}/contact`, contactData); 
  }
}