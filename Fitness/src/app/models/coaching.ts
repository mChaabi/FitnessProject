export interface Coach { id: number; nom: string; prenom: string; email: string;
  specialties?: string; bio?: string; active: boolean; clients: number; }
export interface ProgramItem { id?: number; exerciceId: number; exerciceName?: string; categorie?: string;
  dayOfWeek: number; sets: number; reps: number; restSeconds: number; notes?: string; }
export interface Program { id: number; name: string; goal?: string; startDate: string; active: boolean;
  memberId: number; memberName: string; coachName: string; items: ProgramItem[]; }
export interface ProgramSummary { id: number; name: string; startDate: string; active: boolean; memberName: string; coachName: string; }

export const DAYS = [
  { n: 1, label: 'Lundi' }, { n: 2, label: 'Mardi' }, { n: 3, label: 'Mercredi' }, { n: 4, label: 'Jeudi' },
  { n: 5, label: 'Vendredi' }, { n: 6, label: 'Samedi' }, { n: 7, label: 'Dimanche' }];