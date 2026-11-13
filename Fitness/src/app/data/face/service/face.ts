import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API } from '../../../core/api';

export interface FaceStatus { enrolled: boolean; consentAt?: string | null; }
export interface FaceMatch { matched: boolean; memberId?: number | null; memberName?: string | null; confidence: number; message: string; }

@Injectable({ providedIn: 'root' })
export class FaceService {
  private http = inject(HttpClient);

  status(memberId: number) { return this.http.get<FaceStatus>(`${API}/face/members/${memberId}`); }

  enroll(memberId: number, photo: Blob, consent: boolean) {
    const fd = new FormData();                       // the browser sets the multipart Content-Type itself
    fd.append('consent', String(consent));
    fd.append('photo', photo, 'photo.jpg');
    return this.http.post<FaceStatus>(`${API}/face/members/${memberId}`, fd);
  }

  remove(memberId: number) { return this.http.delete<void>(`${API}/face/members/${memberId}`); }

  identify(photo: Blob) {
    const fd = new FormData();
    fd.append('photo', photo, 'photo.jpg');
    return this.http.post<FaceMatch>(`${API}/face/identify`, fd);
  }
}