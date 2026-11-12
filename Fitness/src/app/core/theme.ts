import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const KEY = 'fitforge_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private browser = isPlatformBrowser(inject(PLATFORM_ID));   // keep above the signal that reads it
  private shells = 0;                                         // how many dashboard shells are on screen
  readonly dark = signal(this.initial());

  toggle() { this.set(!this.dark()); }

  set(dark: boolean) {
    this.dark.set(dark);
    try { if (this.browser) localStorage.setItem(KEY, dark ? 'dark' : 'light'); } catch { /* private mode */ }
    if (this.shells > 0) this.apply();           // applied immediately, so charts read the right colors
  }

  /** The dashboard shell calls this when it appears... */
  enter() { this.shells++; this.apply(); }

  /** ...and this when it disappears: the public site goes back to light. */
  leave() {
    this.shells = Math.max(0, this.shells - 1);
    if (this.shells === 0 && this.browser) document.documentElement.removeAttribute('data-theme');
  }

  private apply() {
    if (this.browser) document.documentElement.setAttribute('data-theme', this.dark() ? 'dark' : 'light');
  }

  private initial(): boolean {
    if (!this.browser) return false;
    try {
      const saved = localStorage.getItem(KEY);
      if (saved) return saved === 'dark';
    } catch { /* ignore */ }
    return window.matchMedia('(prefers-color-scheme: dark)').matches;   // first visit: follow the system
  }
}