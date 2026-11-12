import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { API } from '../../../core/api';

export interface EnrollmentRow {
  id: number; memberId: number; memberName: string; email: string; phone?: string | null;
  planId?: number | null; planName?: string | null; price?: number | null;
  coachName?: string | null; createdAt: string;
}

@Component({ selector: 'app-enrollments', standalone: true, imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './enrollments.html' })
export class EnrollmentsPage {
  private http = inject(HttpClient);
  rows = signal<EnrollmentRow[]>([]);
  loading = signal(true);

  constructor() {
    this.http.get<EnrollmentRow[]>(`${API}/enrollments/pending`).subscribe({
      next: r => { this.rows.set(r); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}