import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Member, MemberRequest, MemberStatus, Page } from '../models/member';

@Injectable({ providedIn: 'root' })
export class MemberService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080/members';

  list(q: string, status: MemberStatus | '', page: number, size = 10) {
    let params = new HttpParams().set('page', page).set('size', size);
    if (q) params = params.set('q', q);
    if (status) params = params.set('status', status);
    return this.http.get<Page<Member>>(this.api, { params });
  }
  get(id: number)                   { return this.http.get<Member>(`${this.api}/${id}`); }
  create(m: MemberRequest)          { return this.http.post<Member>(this.api, m); }
  update(id: number, m: MemberRequest) { return this.http.put<Member>(`${this.api}/${id}`, m); }
  deactivate(id: number)            { return this.http.delete<void>(`${this.api}/${id}`); }
  qr(id: number)                    { return this.http.get(`${this.api}/${id}/qr`, { responseType: 'blob' }); }
}