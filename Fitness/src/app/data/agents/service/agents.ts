import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API } from '../../../core/api';
import { AnalysisReply } from '../agents';

@Injectable({ providedIn: 'root' })
export class AgentsService {
  private http = inject(HttpClient);
  analyze(question: string) { return this.http.post<AnalysisReply>(`${API}/agents/analyst`, { question }); }
}