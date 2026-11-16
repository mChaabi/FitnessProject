export type ContactStatus = 'NEW' | 'READ' | 'DONE';

export interface ContactRow {
  id: number; nom: string; email: string; sujet?: string | null; message: string;
  status: ContactStatus; createdAt: string; handledBy?: string | null; handledAt?: string | null;
}

const SUBJECTS: Record<string, string> = { info: 'Informations cours', coach: 'Suivi coaching', tech: 'Problème technique' };
export const subjectLabel = (s?: string | null) => (s && SUBJECTS[s]) || 'Sans sujet';