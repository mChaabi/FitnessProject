import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

// Define API URL or import it from your environment file
const API = 'http://localhost:8080'; 

@Component({
  selector: 'app-unsubscribe',
  standalone: true,
  templateUrl: './unsubscribe-page.html'
})
export class UnsubscribePage {
  private http = inject(HttpClient);
  token: string | null = inject(ActivatedRoute).snapshot.queryParamMap.get('t');
  state = signal<'idle' | 'loading' | 'done' | 'error'>(this.token ? 'idle' : 'error');

  confirm() {
    if (!this.token) return;
    this.state.set('loading');
    this.http.post<void>(`${API}/notifications/unsubscribe`, null, { params: { t: this.token } })
      .subscribe({
        next: () => this.state.set('done'),
        error: () => this.state.set('error')
      });
  }
}