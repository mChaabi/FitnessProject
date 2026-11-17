export interface AuditRow {
  id: number; createdAt: string; actor: string; actorRole?: string | null;
  action: string; entityType: string; entityId?: number | null; summary: string;
}

type Tone = 'ok' | 'info' | 'warn';
export const ACTIONS: Record<string, { label: string; tone: Tone }> = {
  SUBSCRIPTION_SOLD:   { label: 'Abonnement vendu', tone: 'ok' },
  MEMBER_CREATED:      { label: 'Membre créé', tone: 'ok' },
  MEMBER_UPDATED:      { label: 'Membre modifié', tone: 'info' },
  MEMBER_DEACTIVATED:  { label: 'Membre désactivé', tone: 'warn' },
  PLAN_CREATED:        { label: 'Offre créée', tone: 'ok' },
  PLAN_UPDATED:        { label: 'Offre modifiée', tone: 'info' },
  PLAN_PRICE_CHANGED:  { label: 'Prix modifié', tone: 'warn' },
  PLAN_ACTIVATED:      { label: 'Offre activée', tone: 'ok' },
  PLAN_DEACTIVATED:    { label: 'Offre désactivée', tone: 'warn' },
  COACH_CREATED:       { label: 'Coach créé', tone: 'ok' },
  COACH_ACTIVATED:     { label: 'Coach réactivé', tone: 'ok' },
  COACH_DEACTIVATED:   { label: 'Coach désactivé', tone: 'warn' },
  COACH_ASSIGNED:      { label: 'Coach assigné', tone: 'info' },
  VIDEO_ADDED:         { label: 'Vidéo ajoutée', tone: 'ok' },
  VIDEO_DELETED:       { label: 'Vidéo supprimée', tone: 'warn' },
  MEMBERSHIP_FROZEN:   { label: 'Abonnement gelé', tone: 'info' },
  MEMBERSHIP_RESUMED:  { label: 'Abonnement repris', tone: 'ok' },
  FACE_ENROLLED:       { label: 'Visage enregistré', tone: 'info' },
  FACE_DELETED:        { label: 'Visage supprimé', tone: 'warn' },
  SESSION_CANCELLED:   { label: 'Séance annulée', tone: 'warn' },
};
export const actionLabel = (a: string) => ACTIONS[a]?.label ?? a;
export const actionTone = (a: string): Tone => ACTIONS[a]?.tone ?? 'info';

export const AUDIT_TYPES = [
  { k: '', l: 'Tout' }, { k: 'MEMBER', l: 'Membres' }, { k: 'PLAN', l: 'Offres' },
  { k: 'COACH', l: 'Coachs' }, { k: 'VIDEO', l: 'Vidéos' }, { k: 'SESSION', l: 'Cours' },
];