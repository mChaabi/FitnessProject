import { DestroyRef, Directive, ElementRef, PLATFORM_ID, effect, inject, input, untracked } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/** <strong [countUp]="n" prefix="MAD " suffix="%"></strong>: the number counts from its previous value to the new one. */
@Directive({ selector: '[countUp]', standalone: true })
export class CountUp {
  value = input.required<number>({ alias: 'countUp' });
  prefix = input('');
  suffix = input('');
  decimals = input(0);
  duration = input(1100);

  private el = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private browser = isPlatformBrowser(inject(PLATFORM_ID));
  private shown = 0;
  private raf = 0;

  constructor() {
    effect(() => {
      const to = Number(this.value()) || 0;       // only the value is tracked
      untracked(() => this.run(to));
    });
    inject(DestroyRef).onDestroy(() => { if (this.browser) cancelAnimationFrame(this.raf); });
  }

  private run(to: number) {
    if (this.browser) cancelAnimationFrame(this.raf);
    const from = this.shown;
    if (!this.browser || from === to || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      this.paint(to); return;                      // server, same value or reduced motion: no animation
    }
    const ms = this.duration(), t0 = performance.now();
    const step = (now: number) => {
      const p = Math.min(1, (now - t0) / ms);
      this.paint(from + (to - from) * (1 - Math.pow(1 - p, 3)));   // ease-out: fast start, soft landing
      if (p < 1) this.raf = requestAnimationFrame(step);
    };
    this.raf = requestAnimationFrame(step);
  }

  private paint(n: number) {
    this.shown = n;
    const d = this.decimals();
    const txt = new Intl.NumberFormat('fr-FR', { minimumFractionDigits: d, maximumFractionDigits: d }).format(n);
    this.el.textContent = this.prefix() + txt + this.suffix();
  }
}