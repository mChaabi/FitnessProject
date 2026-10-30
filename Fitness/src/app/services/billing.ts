import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Plan, PlanRequest, SubscriptionRow, PaymentRow, Revenue, PaymentMethod } from '../models/billing';
import { Page } from '../models/member';

@Injectable({ providedIn: 'root' })
export class BillingService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080';

  activePlans()  { return this.http.get<Plan[]>(`${this.api}/plans`); }
  allPlans()     { return this.http.get<Plan[]>(`${this.api}/plans/all`); }
  createPlan(p: PlanRequest) { return this.http.post<Plan>(`${this.api}/plans`, p); }
  updatePlan(id: number, p: PlanRequest) { return this.http.put<Plan>(`${this.api}/plans/${id}`, p); }

  sell(memberId: number, planId: number, method: PaymentMethod) {
    return this.http.post<SubscriptionRow>(`${this.api}/members/${memberId}/subscriptions`, { planId, method });
  }
  history(memberId: number) { return this.http.get<SubscriptionRow[]>(`${this.api}/members/${memberId}/subscriptions`); }

  payments(page: number) {
    return this.http.get<Page<PaymentRow>>(`${this.api}/payments`, { params: new HttpParams().set('page', page) });
  }
  revenue() { return this.http.get<Revenue>(`${this.api}/payments/summary`); }

  /** El PDF se pide con HttpClient para que el interceptor añada el token. */
  downloadReceipt(paymentId: number, receiptNumber: string) {
    this.http.get(`${this.api}/payments/${paymentId}/receipt`, { responseType: 'blob' }).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `${receiptNumber}.pdf`; a.click();
      URL.revokeObjectURL(url);
    });
  }
}