import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MemberService } from '../../../services/member';
import { Member, MemberStatus, Page } from '../../../models/member';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-members', standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DatePipe],
  templateUrl: './members.html', styleUrl: './members.css'
})
export class MembersComponent {
  private svc = inject(MemberService);

  search = new FormControl('', { nonNullable: true });
  status = signal<MemberStatus | ''>('');
  page = signal(0);
  data = signal<Page<Member> | null>(null);
  loading = signal(false);

  constructor() {
    this.search.valueChanges.pipe(
      debounceTime(300), distinctUntilChanged(), takeUntilDestroyed()
    ).subscribe(() => { this.page.set(0); this.load(); });
    this.load();
  }

  load() {
    this.loading.set(true);
    this.svc.list(this.search.value.trim(), this.status(), this.page()).subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  setStatus(s: string) { this.status.set(s as MemberStatus | ''); this.page.set(0); this.load(); }
  go(p: number) { this.page.set(p); this.load(); }

  badge(s: MemberStatus) {
    return { ACTIVE: 'badge-success', FROZEN: 'badge-info', EXPIRED: 'badge-danger', INACTIVE: 'badge-warning' }[s];
  }
}