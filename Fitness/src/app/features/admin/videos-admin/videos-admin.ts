import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Video, VIDEO_CATEGORIES, VideoCategory, categoryLabel, thumbOf } from '../../../data/videos/videos';
import { VideosService } from '../../../data/videos/service/videos';
import { probeVideo } from '../../../shared/video-probe';

const MAX_MB = 200;

@Component({ selector: 'app-videos-admin', standalone: true, imports: [ReactiveFormsModule],
  templateUrl: './videos-admin.html', styleUrl: './videos-admin.css' })
export class VideosAdmin {
  private svc = inject(VideosService);
  private fb = inject(FormBuilder);

  categories = VIDEO_CATEGORIES;
  label = categoryLabel;
  thumb = thumbOf;

  videos = signal<Video[]>([]);
  mode = signal<'file' | 'link'>('file');
  editing = signal<Video | null>(null);
  saving = signal(false);
  progress = signal(0);
  dragging = signal(false);
  file = signal<File | null>(null);
  poster = signal<Blob | null>(null);
  posterUrl = signal<string | null>(null);
  message = signal<{ ok: boolean; text: string } | null>(null);

  form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(255)],
    category: ['BODY_ATTACK' as VideoCategory, Validators.required],
    url: [''],
    durationMin: [null as number | null, [Validators.min(1), Validators.max(600)]],
    active: [true]
  });

  constructor() { this.load(); }
  load() { this.svc.adminList().subscribe(v => this.videos.set(v)); }

  sourceLabel(v: Video) { return v.type === 'YOUTUBE' ? 'YouTube' : v.type === 'UPLOAD' ? 'Fichier envoyé' : 'Fichier du site'; }
  urlOf(v: Video) { return v.type === 'YOUTUBE' ? `https://www.youtube.com/watch?v=${v.source}` : v.source; }
  size(f: File) { return f.size > 1048576 ? (f.size / 1048576).toFixed(1) + ' Mo' : Math.round(f.size / 1024) + ' Ko'; }

  // ---------- choosing a file ----------
  onPick(e: Event) { const i = e.target as HTMLInputElement; this.pick(i.files?.[0]); i.value = ''; }
  onDrop(e: DragEvent) { e.preventDefault(); this.dragging.set(false); this.pick(e.dataTransfer?.files?.[0]); }

  async pick(f?: File) {
    if (!f) return;
    if (!/\.(mp4|webm)$/i.test(f.name) || !/^video\/(mp4|webm)$/.test(f.type || 'video/mp4')) {
      this.say(false, 'Format accepté : MP4 ou WebM.'); return;
    }
    if (f.size > MAX_MB * 1048576) { this.say(false, `Vidéo trop lourde (${MAX_MB} Mo maximum).`); return; }
    this.message.set(null); this.file.set(f);
    if (!this.form.controls.title.value)
      this.form.patchValue({ title: f.name.replace(/\.[^.]+$/, '').replace(/[_-]+/g, ' ').trim() });

    const p = await probeVideo(f);                         // poster + duration, computed in the browser
    this.poster.set(p.thumb);
    const old = this.posterUrl(); if (old) URL.revokeObjectURL(old);
    this.posterUrl.set(p.thumb ? URL.createObjectURL(p.thumb) : null);
    if (p.minutes) this.form.patchValue({ durationMin: p.minutes });
  }

  clearFile(e: Event) {
    e.stopPropagation();
    const old = this.posterUrl(); if (old) URL.revokeObjectURL(old);
    this.file.set(null); this.poster.set(null); this.posterUrl.set(null);
  }

  // ---------- saving ----------
  save() {
    if (this.form.invalid || this.saving()) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    this.message.set(null);

    if (this.mode() === 'file' && !this.editing()) {
      const f = this.file();
      if (!f) { this.say(false, 'Choisissez une vidéo à envoyer.'); return; }
      const fd = new FormData();
      fd.append('file', f, f.name);
      if (this.poster()) fd.append('thumb', this.poster()!, 'poster.jpg');
      fd.append('title', v.title); fd.append('category', v.category); fd.append('active', String(v.active));
      if (v.description) fd.append('description', v.description);
      if (v.durationMin) fd.append('durationMin', String(v.durationMin));

      this.saving.set(true); this.progress.set(0);
      this.svc.upload(fd, p => this.progress.set(p)).subscribe({
        next: () => this.done('Vidéo envoyée.'),
        error: e => this.fail(e)
      });
      return;
    }

    if (!this.editing() && !v.url.trim()) { this.say(false, 'Collez un lien YouTube.'); return; }
    this.saving.set(true);
    this.svc.save(this.editing()?.id ?? null, {
      title: v.title, description: v.description || null, category: v.category,
      url: this.editing()?.type === 'UPLOAD' ? null : v.url, durationMin: v.durationMin, active: v.active
    }).subscribe({ next: () => this.done('Vidéo enregistrée.'), error: e => this.fail(e) });
  }

  edit(v: Video) {
    this.editing.set(v); this.mode.set(v.type === 'UPLOAD' ? 'file' : 'link'); this.message.set(null);
    this.form.patchValue({ title: v.title, description: v.description ?? '', category: v.category,
      url: v.type === 'UPLOAD' ? '' : this.urlOf(v), durationMin: v.durationMin ?? null, active: v.active });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  reset() {
    const old = this.posterUrl(); if (old) URL.revokeObjectURL(old);
    this.editing.set(null); this.file.set(null); this.poster.set(null); this.posterUrl.set(null); this.progress.set(0);
    this.form.reset({ title: '', description: '', category: 'BODY_ATTACK', url: '', durationMin: null, active: true });
  }

  toggle(v: Video) {
    this.svc.save(v.id, { title: v.title, description: v.description, category: v.category,
      url: v.type === 'UPLOAD' ? null : this.urlOf(v), durationMin: v.durationMin, active: !v.active }).subscribe(() => this.load());
  }

  remove(v: Video) {
    if (confirm(`Supprimer « ${v.title} » ?${v.type === 'UPLOAD' ? ' Le fichier sera effacé.' : ''}`))
      this.svc.remove(v.id).subscribe(() => this.load());
  }

  private done(text: string) { this.saving.set(false); this.reset(); this.load(); this.say(true, text); }
  private fail(e: any) {
    this.saving.set(false);
    this.say(false, e.status === 401 ? 'Session expirée : reconnectez-vous puis réessayez.'
      : e.status === 413 ? `Vidéo trop lourde (${MAX_MB} Mo maximum).`
      : e.error?.message ?? (e.error && typeof e.error === 'object' ? Object.values(e.error).join(' · ') : 'Erreur'));
  }
  private say(ok: boolean, text: string) { this.message.set({ ok, text }); }
}