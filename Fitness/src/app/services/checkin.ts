import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CheckInResult, CheckInRow, TodayStats } from '../models/checkin';


@Injectable({ providedIn: 'root' })
export class CheckinService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080/checkins';

  byQr(qrCode: string)     { return this.http.post<CheckInResult>(this.api, { qrCode }); }
  byMember(memberId: number) { return this.http.post<CheckInResult>(this.api, { memberId }); }
  today()  { return this.http.get<CheckInRow[]>(`${this.api}/today`); }
  stats()  { return this.http.get<TodayStats>(`${this.api}/stats`); }
}