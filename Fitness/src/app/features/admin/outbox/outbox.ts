import { DatePipe } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Page } from '../../../models/member';

export interface OutboxRow { id: number; type: string; recipient: string; status: string; attempts: number;
  createdAt: string; sentAt?: string; aiGenerated?: boolean; lastError?: string; }

  const API = 'http://localhost:8080';

@Component({ selector: 'app-outbox', standalone: true, imports: [DatePipe], templateUrl: './outbox.html' ,styleUrls: ['./outbox.css']})
export class OutboxPage {
  private http = inject(HttpClient);
  data = signal<Page<OutboxRow> | null>(null);
  stats = signal<Record<string, number>>({});
  status = signal(''); page = signal(0);
  statuses = ['PENDING', 'SENDING', 'SENT', 'FAILED', 'SKIPPED'];

  constructor() { this.load(); }
  load() {
    let params = new HttpParams().set('page', this.page());
    if (this.status()) params = params.set('status', this.status());
    this.http.get<Page<OutboxRow>>(`${API}/admin/outbox`, { params }).subscribe(d => this.data.set(d));
    this.http.get<Record<string, number>>(`${API}/admin/outbox/stats`).subscribe(s => this.stats.set(s));
  }
  filter(s: string) { this.status.set(s); this.page.set(0); this.load(); }
  go(p: number) { this.page.set(p); this.load(); }
  retry(r: OutboxRow) { this.http.post<void>(`${API}/admin/outbox/${r.id}/retry`, {}).subscribe(() => this.load()); }
  badge(s: string) { return ({ SENT: 'badge-success', FAILED: 'badge-danger', PENDING: 'badge-warning',
                               SENDING: 'badge-info', SKIPPED: 'badge-info' } as any)[s]; }
}
