export type PaymentMethod = 'CASH' | 'CARD' | 'TRANSFER';
export interface Plan { id: number; name: string; price: number; durationDays: number; description?: string; active: boolean; }
export type PlanRequest = Omit<Plan, 'id'>;
export interface SubscriptionRow { subscriptionId: number; planName: string; startDate: string; endDate: string;
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED'; paymentId: number; receiptNumber: string; amount: number; }
export interface PaymentRow { id: number; receiptNumber: string; memberName: string; planName: string;
  amount: number; method: PaymentMethod; paidAt: string; }
export interface Revenue { thisMonth: number; lastMonth: number; }