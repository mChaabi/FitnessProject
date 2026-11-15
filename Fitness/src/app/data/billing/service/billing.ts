import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Plan, SubscriptionRow, BillingRecord, Page, PaymentRow, Revenue } from '../billing';

@Injectable({
  providedIn: 'root',
})
export class BillingService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8080';

  // Subscriptions (Maps to SubscriptionController)
  getSubscriptions(memberId: number) {
    return this.http.get<SubscriptionRow[]>(`${this.api}/members/${memberId}/subscriptions`);
  }

  createBilling(memberId: number, data: any) {
    return this.http.post<SubscriptionRow>(`${this.api}/members/${memberId}/subscriptions`, data);
  }

  // Plans (Maps to PlanController)
  getPlans() {
    return this.http.get<Plan[]>(`${this.api}/plans`);
  }

  allPlans() {
    return this.http.get<Plan[]>(`${this.api}/plans/all`);
  }

  createPlan(data: any) {
    return this.http.post<Plan>(`${this.api}/plans`, data);
  }

  updatePlan(id: number, data: any) {
    return this.http.put<Plan>(`${this.api}/plans/${id}`, data);
  }

    /** Downloads the receipt PDF (the token is added by the interceptor). */
  downloadReceipt(paymentId: number, receiptNumber: string) {
    this.http.get(`${this.api}/payments/${paymentId}/receipt`, { responseType: 'blob' }).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `${receiptNumber}.pdf`;
        document.body.appendChild(a); a.click(); a.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: () => alert("Impossible de télécharger le reçu. Réessayez."),
    });
  }

  payments(page: number = 0, size: number = 10) {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<PaymentRow>>(`${this.api}/payments`, { params }); // changed from /billing/payments
  }

  revenue() {
    return this.http.get<Revenue>(`${this.api}/payments/summary`); // changed from /billing/revenue
  }
}
