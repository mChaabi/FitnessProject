import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API } from '../../../core/api';
import { FreezeResult, FreezeRow } from '../freeze';

@Injectable({ providedIn: 'root' })
export class FreezeApiService {
  private http = inject(HttpClient);
  history(memberId: number) { return this.http.get<FreezeRow[]>(`${API}/members/${memberId}/freezes`); }
  freeze(memberId: number, days: number, reason: string) {
    return this.http.post<FreezeResult>(`${API}/members/${memberId}/freeze`, { days, reason: reason.trim() || null });
  }
  unfreeze(memberId: number) { return this.http.post<FreezeResult>(`${API}/members/${memberId}/unfreeze`, null); }
}