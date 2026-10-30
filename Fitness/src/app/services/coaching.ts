import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Coach, Program, ProgramSummary } from '../models/coaching';
import { Member, Page } from '../models/member';
import { Exercice } from '../models/exercice';

@Injectable({ providedIn: 'root' })
export class CoachingService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080';

  coaches()                      { return this.http.get<Coach[]>(`${this.api}/coaches`); }
  createCoach(c: any)            { return this.http.post<Coach>(`${this.api}/coaches`, c); }
  setCoachActive(id: number, v: boolean) { return this.http.patch<void>(`${this.api}/coaches/${id}/active`, null, { params: { value: v } }); }
  assign(memberId: number, coachId: number | null) { return this.http.put<void>(`${this.api}/members/${memberId}/coach`, { coachId }); }
  linkAccount(memberId: number, email: string)     { return this.http.post<void>(`${this.api}/members/${memberId}/account`, { email }); }

  clients(page = 0)              { return this.http.get<Page<Member>>(`${this.api}/coach/clients`, { params: { page } }); }
  exercices()                    { return this.http.get<Exercice[]>(`${this.api}/exercices`); }

  program(id: number)            { return this.http.get<Program>(`${this.api}/programs/${id}`); }
  programsOf(memberId: number)   { return this.http.get<ProgramSummary[]>(`${this.api}/members/${memberId}/programs`); }
  myPrograms()                   { return this.http.get<Program[]>(`${this.api}/programs/me`); }
  saveProgram(id: number | null, body: any) {
    return id ? this.http.put<Program>(`${this.api}/programs/${id}`, body)
              : this.http.post<Program>(`${this.api}/programs`, body); }
  deleteProgram(id: number)      { return this.http.delete<void>(`${this.api}/programs/${id}`); }
}
