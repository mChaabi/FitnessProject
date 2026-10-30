import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnDestroy, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MemberService } from '../../../services/member';
import { AuthService } from '../../../services/auth-service';
import { Member } from '../../../models/member';
import { Plan, SubscriptionRow, PaymentMethod } from '../../../models/billing';
import { BillingService } from '../../../services/billing';
import { Coach } from '../../../models/coaching';
import { CoachingService } from '../../../services/coaching';

@Component({
  selector: 'app-member-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './member-detail.html',
  styleUrl: './member-detail.css',
})
export class MemberDetailComponent implements OnDestroy {
  private svc = inject(MemberService);
  private route = inject(ActivatedRoute);
  auth = inject(AuthService);
  private billing = inject(BillingService);
  private coaching = inject(CoachingService);

  coaches = signal<Coach[]>([]);
  linkEmail = signal('');
  msg = signal('');
  plans = signal<Plan[]>([]);
  history = signal<SubscriptionRow[]>([]);
  selling = signal(false);
  saleError = signal('');
  planId = signal<number | null>(null);
  method = signal<PaymentMethod>('CASH');

  member = signal<Member | null>(null);
  qrUrl = signal<string | null>(null);
  private id = Number(this.route.snapshot.paramMap.get('id'));

  constructor() {
    this.svc.get(this.id).subscribe((m) => this.member.set(m));
    this.svc.qr(this.id).subscribe((blob) => this.qrUrl.set(URL.createObjectURL(blob)));
    this.billing.activePlans().subscribe((p) => this.plans.set(p));
    if (this.auth.role() === 'ADMIN') this.coaching.coaches().subscribe(c => this.coaches.set(c.filter(x => x.active)));

    this.loadHistory();
  }

  assignCoach(v: string) {
  this.coaching.assign(this.id, v ? +v : null).subscribe(() => this.msg.set('Coach mis à jour'));
}
link() {
  this.coaching.linkAccount(this.id, this.linkEmail()).subscribe({
    next: () => { this.msg.set('Compte lié'); this.svc.get(this.id).subscribe(m => this.member.set(m)); },
    error: e => this.msg.set(e.error?.message ?? 'Erreur')
    });}

  loadHistory() {
    this.billing.history(this.id).subscribe((h) => this.history.set(h));
  }
  

  deactivate() {
    if (!confirm('Désactiver ce membre ?')) return;
    this.svc
      .deactivate(this.id)
      .subscribe(() => this.svc.get(this.id).subscribe((m) => this.member.set(m)));
  }

  sell() {
    const plan = this.planId();
    if (!plan || this.selling()) return; // evita el doble clic = doble cobro
    this.selling.set(true);
    this.saleError.set('');
    this.billing.sell(this.id, plan, this.method()).subscribe({
      next: (r) => {
        this.selling.set(false);
        this.loadHistory();
        this.svc.get(this.id).subscribe((m) => this.member.set(m));
        this.billing.downloadReceipt(r.paymentId, r.receiptNumber);
      },
      error: (e) => {
        this.selling.set(false);
        this.saleError.set(e.error?.message ?? 'Erreur');
      },
    });
  }

  download(h: SubscriptionRow) {
    this.billing.downloadReceipt(h.paymentId, h.receiptNumber);
  }

  ngOnDestroy() {
    const u = this.qrUrl();
    if (u) URL.revokeObjectURL(u);
  } // libera memoria
}
