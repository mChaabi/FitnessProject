import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CoachingService } from '../../../services/coaching';
import { Member } from '../../../models/member';
@Component({ selector: 'app-clients', standalone: true, imports: [RouterLink], templateUrl: './clients.html' })
export class Clients {
  private svc = inject(CoachingService);
  clients = signal<Member[]>([]);
  loading = signal(true);
  constructor() { this.svc.clients().subscribe({ next: p => { this.clients.set(p.content); this.loading.set(false); },
                                                  error: () => this.loading.set(false) }); }
}