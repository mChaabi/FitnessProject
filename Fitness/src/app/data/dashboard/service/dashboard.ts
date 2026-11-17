import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardData } from '../dashboard';
import { API } from '../../../core/api';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  load(days = 30): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${API}/dashboard`, { params: { days } });
  }
}