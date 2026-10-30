import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core'; // <-- ¡Corregido desde '@angular/core'!
import { BillingService } from '../../../services/billing';
import { Page } from '../../../models/member';
import { PaymentRow, Revenue } from '../../../models/billing';

@Component({ selector: 'app-payments', standalone: true, imports: [DatePipe, CurrencyPipe], templateUrl: './payments.html' })
export class PaymentsComponent {
  private svc = inject(BillingService);
  data = signal<Page<PaymentRow> | null>(null);
  revenue = signal<Revenue | null>(null);

  constructor() { this.go(0); this.svc.revenue().subscribe(r => this.revenue.set(r)); }
  go(p: number) { this.svc.payments(p).subscribe(d => this.data.set(d)); }
  receipt(p: PaymentRow) { this.svc.downloadReceipt(p.id, p.receiptNumber); }
  growth() {
    const r = this.revenue(); if (!r || !r.lastMonth) return null;
    return Math.round(((r.thisMonth - r.lastMonth) / r.lastMonth) * 100);
  }
}