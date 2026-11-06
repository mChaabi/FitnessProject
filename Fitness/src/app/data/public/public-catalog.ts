export interface PublicSession {
  id: number;
  name: string;
  description?: string;
  coachName: string;
  date: string;
  startTime: string;
  endTime: string;
  room?: string;
  capacity: number;
  freeSpots: number;
}

export interface PublicCoach {
  id: number;
  name: string;
  specialties?: string;
  bio?: string;
}
