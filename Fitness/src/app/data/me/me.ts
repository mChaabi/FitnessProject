export interface MemberOverview {
  firstName: string; status: 'ACTIVE' | 'EXPIRED' | 'NONE';
  planName?: string | null; startDate?: string | null; endDate?: string | null;
  daysLeft?: number | null; totalDays: number;
  coachName?: string | null; coachSpecialties?: string | null; requestedPlan?: string | null;
}