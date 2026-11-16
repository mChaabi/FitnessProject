export interface FreezeRow {
  id: number; startDate: string; resumeDate: string; endedOn?: string | null;
  daysAdded: number; status: 'ACTIVE' | 'ENDED'; reason?: string | null; createdBy: string;
}
export interface FreezeResult { freeze: FreezeRow; cancelledBookings: number; newEndDate: string | null; }

export const FREEZE_MIN = 7;
export const FREEZE_MAX = 90;           // keep in sync with app.freeze.* in application.properties