import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardData } from '../dashboard';
import { API } from '../../../core/api';


@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private api = `${API}/dashboard`; 

  load(): Observable<DashboardData> {
    return this.http.get<DashboardData>(this.api);
  }
}