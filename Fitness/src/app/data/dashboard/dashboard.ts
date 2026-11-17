export interface Kpis {
  activeMembers: number; newThisMonth: number;
  revenueThisMonth: number; revenueSamePeriodLastMonth: number; revenueGrowth: number | null;
  entriesToday: number; deniedToday: number; expiringCount: number; atRiskCount: number;
  expiringDays: number; inactivityDays: number;
}

export interface ExpiringRow { memberId: number; memberName: string; phone?: string; plan: string; endDate: string; daysLeft: number; }
export interface AtRiskRow { memberId: number; memberName: string; phone?: string; lastVisit?: string | null; daysSince?: number | null; }
export interface SessionLoad { id: number; name: string; date: string; start: string; confirmed: number; capacity: number; waiting: number; }
export interface PeriodStats {
  days: number; from: string;
  newMembers: number; newMembersPrev: number; revenue: number; revenuePrev: number;
  visits: number; visitsPrev: number; uniqueVisitors: number; uniqueVisitorsPrev: number; coachesActive: number;
}
export interface PlanStat { plan: string; active: number; revenue: number; }
export interface CoachStat { id: number; name: string; specialties?: string | null; clients: number; sessions: number; occupancy: number; }
export interface ClassStat { name: string; sessions: number; confirmed: number; capacity: number; occupancy: number; }

export interface DashboardData {
  kpis: Kpis;
  revenue: { month: string; amount: number }[];
  visits: { day: string; visits: number }[];
  plans: { plan: string; count: number }[];
  expiring: ExpiringRow[]; atRisk: AtRiskRow[];
  classes: { sessions: number; avgOccupancy: number; waitingTotal: number; upcoming: SessionLoad[] };
  generatedAt: string;
  period: PeriodStats;
  memberStatus: { status: string; count: number }[];
  memberGrowth: { month: string; count: number }[];
  heatmap: number[][];
  planStats: PlanStat[];
  coachStats: CoachStat[];
  topClasses: ClassStat[];
}