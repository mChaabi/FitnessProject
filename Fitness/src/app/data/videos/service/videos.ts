import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../../../core/api';
import { AuthService } from '../../../core/auth/auth-service';
import { Video } from '../videos';

@Injectable({ providedIn: 'root' })
export class VideosService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  list()      { return this.http.get<Video[]>(`${API}/videos`); }
  adminList() { return this.http.get<Video[]>(`${API}/admin/videos`); }
  save(id: number | null, body: any) {
    return id ? this.http.put<Video>(`${API}/admin/videos/${id}`, body)
              : this.http.post<Video>(`${API}/admin/videos`, body);
  }
  remove(id: number) { return this.http.delete<void>(`${API}/admin/videos/${id}`); }

  /** Upload with a real progress percentage. */
  upload(fd: FormData, onProgress: (percent: number) => void): Observable<Video> {
    return new Observable<Video>(sub => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', `${API}/admin/videos/upload`);
      xhr.withCredentials = true;
      xhr.setRequestHeader('Authorization', `Bearer ${this.auth.token() ?? ''}`);
      xhr.setRequestHeader('X-Requested-With', 'fitforge');
      xhr.upload.onprogress = e => { if (e.lengthComputable) onProgress(Math.round((e.loaded * 100) / e.total)); };
      xhr.onload = () => {
        let body: any = null; try { body = JSON.parse(xhr.responseText); } catch { /* not JSON */ }
        if (xhr.status >= 200 && xhr.status < 300) { sub.next(body); sub.complete(); }
        else sub.error({ status: xhr.status, error: body });
      };
      xhr.onerror = () => sub.error({ status: 0, error: { message: 'Réseau indisponible' } });
      xhr.send(fd);
      return () => xhr.abort();
    });
  }
}