export type VideoCategory = 'BODY_ATTACK' | 'MUSCULATION' | 'CROSSFIT' | 'CARDIO_HIIT' | 'YOGA' | 'STRETCHING';

export interface Video {
  id: number; title: string; description?: string | null; category: VideoCategory;
  type: 'YOUTUBE' | 'FILE'; source: string; durationMin?: number | null; active: boolean;
}

export const VIDEO_CATEGORIES: { key: VideoCategory; label: string }[] = [
  { key: 'BODY_ATTACK', label: 'Body Attack' },
  { key: 'MUSCULATION', label: 'Musculation' },
  { key: 'CROSSFIT', label: 'CrossFit' },
  { key: 'CARDIO_HIIT', label: 'Cardio / HIIT' },
  { key: 'YOGA', label: 'Yoga' },
  { key: 'STRETCHING', label: 'Étirements' },
];

export const categoryLabel = (k: string) => VIDEO_CATEGORIES.find(c => c.key === k)?.label ?? k;

/** YouTube gives a free thumbnail; local files have none (the card shows a gradient). */
export const thumbOf = (v: Video) => v.type === 'YOUTUBE' ? `https://img.youtube.com/vi/${v.source}/hqdefault.jpg` : null;