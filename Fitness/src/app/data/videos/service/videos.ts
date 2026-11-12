
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API } from '../../../core/api';
import { Video } from '../videos';

@Injectable({ providedIn: 'root' })
export class VideosService {
  private http = inject(HttpClient);

  list()      { return this.http.get<Video[]>(`${API}/videos`); }
  adminList() { return this.http.get<Video[]>(`${API}/admin/videos`); }
  save(id: number | null, body: any) {
    return id ? this.http.put<Video>(`${API}/admin/videos/${id}`, body)
              : this.http.post<Video>(`${API}/admin/videos`, body);
  }
  remove(id: number) { return this.http.delete<void>(`${API}/admin/videos/${id}`); }
}