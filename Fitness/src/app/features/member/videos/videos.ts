import { Component, PLATFORM_ID, computed, effect, HostListener, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Video, VIDEO_CATEGORIES, categoryLabel, thumbOf } from '../../../data/videos/videos';
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

  all = signal<Video[]>([]);
  loading = signal(true);
  error = signal(false);
  filter = signal('');           // '' = all categories
  query = signal('');
  playing = signal<Video | null>(null);

  visible = computed(() => {
    const f = this.filter(), q = this.query().trim().toLowerCase();
    return this.all().filter(v => (!f || v.category === f) &&
      (!q || v.title.toLowerCase().includes(q) || (v.description ?? '').toLowerCase().includes(q)));
  });

  /** Counts per category for the chips. */
  count = (k: string) => this.all().filter(v => v.category === k).length;

  /** Safe: the backend stores only an 11-character YouTube ID, never a free URL. */
  embed = computed<SafeResourceUrl | null>(() => {
    const v = this.playing();
    if (!v || v.type !== 'YOUTUBE') return null;
    return this.san.bypassSecurityTrustResourceUrl(
      `https://www.youtube-nocookie.com/embed/${v.source}?autoplay=1&rel=0&modestbranding=1`);
  });

  constructor() {
    this.svc.list().subscribe({
      next: v => { this.all.set(v); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); }
    });
    effect(() => {                                    // lock page scroll while the player is open
      const open = this.playing() !== null;
      if (this.browser) document.body.style.overflow = open ? 'hidden' : '';
    });
  }

  open(v: Video) { this.playing.set(v); }

  @HostListener('document:keydown.escape')
  close() { this.playing.set(null); }
}