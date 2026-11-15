import { Component, ElementRef, PLATFORM_ID, computed, effect, inject, signal, viewChild } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Video, VIDEO_CATEGORIES, categoryLabel, playUrl, thumbOf } from '../../../data/videos/videos';
import { VideosService } from '../../../data/videos/service/videos';

@Component({
  selector: 'app-member-videos', standalone: true,
  templateUrl: './videos.html', styleUrl: './videos.css'
})
export class VideosPage {
  private svc = inject(VideosService);
  private san = inject(DomSanitizer);
  private browser = isPlatformBrowser(inject(PLATFORM_ID));

  categories = VIDEO_CATEGORIES;
  label = categoryLabel;
  thumb = thumbOf;
  src = playUrl;
  dlg = viewChild.required<ElementRef<HTMLDialogElement>>('dlg');

  all = signal<Video[]>([]);
  loading = signal(true);
  error = signal(false);
  filter = signal('');
  query = signal('');
  playing = signal<Video | null>(null);

  filtering = computed(() => !!this.filter() || !!this.query().trim());
  featured = computed(() => (this.filtering() ? null : this.all()[0] ?? null));

  visible = computed(() => {
    const f = this.filter(), q = this.query().trim().toLowerCase(), first = this.featured();
    return this.all().filter(v => v.id !== first?.id && (!f || v.category === f) &&
      (!q || v.title.toLowerCase().includes(q) || (v.description ?? '').toLowerCase().includes(q)));
  });

  related = computed(() => {
    const p = this.playing();
    return p ? this.all().filter(v => v.id !== p.id && v.category === p.category).slice(0, 4) : [];
  });

  count = (k: string) => this.all().filter(v => v.category === k).length;

  /** Safe: the backend keeps only an 11-character YouTube ID. */
  embed = computed<SafeResourceUrl | null>(() => {
    const v = this.playing();
    return v && v.type === 'YOUTUBE'
      ? this.san.bypassSecurityTrustResourceUrl(`https://www.youtube-nocookie.com/embed/${v.source}?autoplay=1&rel=0&modestbranding=1`)
      : null;
  });

  constructor() {
    this.svc.list().subscribe({
      next: v => { this.all.set(v); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
    effect(() => {
      const open = this.playing() !== null;
      if (!this.browser) return;
      const d = this.dlg().nativeElement;
      if (open && !d.open) d.showModal();
      else if (!open && d.open) d.close();
    });
  }

  open(v: Video) { this.playing.set(v); }
  close() { this.playing.set(null); }
  onBackdrop(e: MouseEvent) { if (e.target === this.dlg().nativeElement) this.close(); }
}