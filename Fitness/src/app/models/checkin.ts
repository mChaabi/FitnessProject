export type Outcome = 'ALLOWED' | 'DENIED' | 'DUPLICATE';
export interface CheckInResult {
  outcome: Outcome; reason?: string; message: string;
  memberId?: number; memberName?: string; planName?: string;
  endDate?: string; daysLeft?: number; expiringSoon: boolean; at: string;
}
export interface CheckInRow { id: number; memberId: number; memberName: string; checkedAt: string; outcome: Outcome; reason?: string; }
export interface TodayStats { entered: number; denied: number; }