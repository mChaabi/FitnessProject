import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Video, VIDEO_CATEGORIES, VideoCategory, categoryLabel } from '../../../data/videos/videos';
import { VideosService } from '../../../data/videos/service/videos';

@Component({ selector: 'app-videos-admin', standalone: true, imports: [ReactiveFormsModule],
  templateUrl: './videos-admin.html' })
export class VideosAdmin {
  private svc = inject(VideosService);
  private fb = inject(FormBuilder);

  categories = VIDEO_CATEGORIES;
  label = categoryLabel;
  videos = signal<Video[]>([]);
  editingId = signal<number | null>(null);
  saving = signal(false);
  message = signal<{ ok: boolean; text: string } | null>(null);

  form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(255)],
    category: ['BODY_ATTACK' as VideoCategory, Validators.required],
    url: ['', [Validators.required, Validators.maxLength(255)]],
    durationMin: [null as number | null, [Validators.min(1), Validators.max(600)]],
    active: [true]
  });

  constructor() { this.load(); }
  load() { this.svc.adminList().subscribe(v => this.videos.set(v)); }

  /** The API returns only the ID for YouTube: rebuild a link so the field is editable. */
  urlOf(v: Video) { return v.type === 'YOUTUBE' ? `https://www.youtube.com/watch?v=${v.source}` : v.source; }

  edit(v: Video) {
    this.editingId.set(v.id); this.message.set(null);
    this.form.patchValue({ title: v.title, description: v.description ?? '', category: v.category,
      url: this.urlOf(v), durationMin: v.durationMin ?? null, active: v.active });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  reset() {
    this.editingId.set(null);
    this.form.reset({ title: '', description: '', category: 'BODY_ATTACK', url: '', durationMin: null, active: true });
  }

  save() {
    if (this.form.invalid || this.saving()) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.message.set(null);
    const v = this.form.getRawValue();
    this.svc.save(this.editingId(), { ...v, description: v.description || null }).subscribe({
      next: () => { this.saving.set(false); this.reset(); this.load(); this.message.set({ ok: true, text: 'Vidéo enregistrée' }); },
      error: e => { this.saving.set(false);
        this.message.set({ ok: false, text: e.error?.message ?? (Object.values(e.error ?? {}).join(' · ') || 'Erreur') }); }
    });
  }

  toggle(v: Video) {
    this.svc.save(v.id, { title: v.title, description: v.description, category: v.category,
      url: this.urlOf(v), durationMin: v.durationMin, active: !v.active }).subscribe(() => this.load());
  }

  remove(v: Video) {
    if (confirm(`Supprimer « ${v.title} » ?`)) this.svc.remove(v.id).subscribe(() => this.load());
  }
}