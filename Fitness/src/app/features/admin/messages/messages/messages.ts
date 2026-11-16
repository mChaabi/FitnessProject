import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ContactsService } from '../../../../data/contacts/service/contacts';
import { ContactRow, ContactStatus } from '../../../../data/contacts/contacts';
import { Page } from '../../../../data/members/member';



@Component({ selector: 'app-messages', standalone: true, imports: [DatePipe],
  templateUrl: './messages.html', styleUrl: './messages.css' })
export class MessagesPage {
  private svc = inject(ContactsService);

  tabs = [{ k: '', l: 'Tous' }, { k: 'NEW', l: 'Nouveaux' }, { k: 'READ', l: 'Lus' }, { k: 'DONE', l: 'Traités' }];
  label = (subject: string | null | undefined) => subject ?? '';

  status = signal('');
  page = signal(0);
  data = signal<Page<ContactRow> | null>(null);
  counts = signal<Record<string, number>>({});
  selected = signal<ContactRow | null>(null);
  loading = signal(true);
  error = signal(false);

  constructor() { this.load(); }

  load() {
    this.loading.set(true); this.error.set(false);
    this.svc.list(this.status(), this.page()).subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
    this.svc.stats().subscribe(s => { this.counts.set(s); this.svc.unread.set(s['NEW'] ?? 0); });
  }

  filter(k: string) { this.status.set(k); this.page.set(0); this.selected.set(null); this.load(); }
  go(p: number) { this.page.set(p); this.selected.set(null); this.load(); }

  open(r: ContactRow) {
    this.selected.set(r);
    if (r.status === 'NEW') this.set(r, 'READ');          // opening a new message marks it as read
  }

  set(r: ContactRow, value: ContactStatus) {
    this.svc.setStatus(r.id, value).subscribe(u => {
      this.data.update(d => d && { ...d, content: d.content.map(x => x.id === u.id ? u : x) });
      if (this.selected()?.id === u.id) this.selected.set(u);
      this.svc.stats().subscribe(s => { this.counts.set(s); this.svc.unread.set(s['NEW'] ?? 0); });
    });
  }

  remove(r: ContactRow) {
    if (!confirm(`Supprimer le message de ${r.nom} ?`)) return;
    this.svc.remove(r.id).subscribe(() => { this.selected.set(null); this.load(); });
  }

  mailto(r: ContactRow) {
    const subject = encodeURIComponent('Re: ' + this.label(r.sujet));
    const body = encodeURIComponent(`Bonjour ${r.nom},\n\n`);
    return `mailto:${r.email}?subject=${subject}&body=${body}`;
  }

  badge(s: ContactStatus) { return ({ NEW: 'badge-danger', READ: 'badge-info', DONE: 'badge-success' })[s]; }
  statusLabel(s: ContactStatus) { return ({ NEW: 'Nouveau', READ: 'Lu', DONE: 'Traité' })[s]; }
}