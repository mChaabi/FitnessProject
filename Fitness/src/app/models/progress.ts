export interface Measurement {
  id: number; measuredOn: string;
  weightKg?: number | null; bodyFatPercent?: number | null;
  waistCm?: number | null; chestCm?: number | null; armCm?: number | null; notes?: string | null;
}
export type MetricKey = 'weightKg' | 'bodyFatPercent' | 'waistCm' | 'chestCm' | 'armCm';

export const METRICS: { key: MetricKey; label: string; unit: string }[] = [
  { key: 'weightKg',       label: 'Poids',    unit: 'kg' },
  { key: 'bodyFatPercent', label: 'Masse grasse', unit: '%' },
  { key: 'waistCm',        label: 'Tour de taille', unit: 'cm' },
  { key: 'chestCm',        label: 'Poitrine', unit: 'cm' },
  { key: 'armCm',          label: 'Bras',     unit: 'cm' },
];