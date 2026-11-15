
import { API } from '../../core/api';

export type VideoCategory = 'BODY_ATTACK' | 'MUSCULATION' | 'CROSSFIT' | 'CARDIO_HIIT' | 'YOGA' | 'STRETCHING';

export interface Video {
  id: number; title: string; description?: string | null; category: VideoCategory;
  type: 'YOUTUBE' | 'FILE' | 'UPLOAD'; source: string; thumb?: string | null;
  durationMin?: number | null; active: boolean;
}

export const mediaUrl = (name: string) => `${API}/media/videos/${name}`;

/** Poster: free from YouTube, generated at upload time for files. */
export const thumbOf = (v: Video) =>
  v.type === 'YOUTUBE' ? `https://img.youtube.com/vi/${v.source}/hqdefault.jpg`
  : v.type === 'UPLOAD' && v.thumb ? mediaUrl(v.thumb) : null;

/** Address of the playable file (not used for YouTube). */
export const playUrl = (v: Video) => v.type === 'UPLOAD' ? mediaUrl(v.source) : v.source;

export const VIDEO_CATEGORIES: { key: VideoCategory; label: string }[] = [
  { key: 'BODY_ATTACK', label: 'Body Attack' },
  { key: 'MUSCULATION', label: 'Musculation' },
  { key: 'CROSSFIT', label: 'CrossFit' },
  { key: 'CARDIO_HIIT', label: 'Cardio / HIIT' },
  { key: 'YOGA', label: 'Yoga' },
  { key: 'STRETCHING', label: 'Étirements' },
];

export const categoryLabel = (k: string) => VIDEO_CATEGORIES.find(c => c.key === k)?.label ?? k;

