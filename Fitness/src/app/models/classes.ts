export type BookingStatus = 'CONFIRMED' | 'WAITLIST' | 'CANCELLED';

export interface ClassSession {
  id: number; templateId?: number | null; name: string; description?: string; coachName: string;
  date: string; startTime: string; endTime: string; capacity: number; room?: string;
  status: 'SCHEDULED' | 'CANCELLED'; confirmed: number; waiting: number;
  myStatus?: BookingStatus | null; myBookingId?: number | null; myPosition?: number | null; bookable: boolean;
}
export interface ClassTemplate {
  id: number; name: string; description?: string; coachId: number; coachName: string; dayOfWeek: number;
  startTime: string; durationMinutes: number; capacity: number; room?: string; active: boolean;
}
export interface Attendee { bookingId: number; memberId: number; memberName: string; status: BookingStatus; queuedAt: string; }
export interface BookingResult { bookingId: number; status: BookingStatus; position?: number | null; }