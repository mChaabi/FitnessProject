import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API } from '../../../core/api';
import { MemberOverview } from '../me';

@Injectable({ providedIn: 'root' })
export class MeService {
  private http = inject(HttpClient);
  overview() { return this.http.get<MemberOverview>(`${API}/me/overview`); }
  qr()       { return this.http.get(`${API}/me/qr`, { responseType: 'blob' }); }
}