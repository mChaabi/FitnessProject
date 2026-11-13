import { Video } from '../videos/videos';

export interface Metric { key: string; label: string; value: string; }
export type Severity = 'POSITIVE' | 'INFO' | 'WARNING' | 'CRITICAL';
export interface InsightView { title: string; detail: string; severity: Severity; metrics: Metric[]; }
export interface AnalysisReply {
  summary: string; insights: InsightView[]; actions: string[]; ai: boolean; generatedAt: string;
  videos: Video[];
}