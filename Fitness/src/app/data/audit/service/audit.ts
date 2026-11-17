import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API } from '../../../core/api';
import { Page } from '../../members/member';
import { AuditRow } from '../audit';

@Injectable({ providedIn: 'root' })
export class AuditApiService {
  private http = inject(HttpClient);

  list(o: { type: string; q: string; from: string; to: string; page: number; size?: number }) {
    let params = new HttpParams().set('page', o.page).set('size', o.size ?? 25);
    if (o.type) params = params.set('type', o.type);
    if (o.q) params = params.set('q', o.q);
    if (o.from) params = params.set('from', o.from);
    if (o.to) params = params.set('to', o.to);
    return this.http.get<Page<AuditRow>>(`${API}/admin/audit`, { params });
  }

  stats(from: string, to: string) {
    return this.http.get<Record<string, number>>(`${API}/admin/audit/stats`, { params: { from, to } });
  }
}