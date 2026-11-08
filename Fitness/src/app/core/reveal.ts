import { Directive, ElementRef, inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Directive({ selector: '[reveal]', standalone: true })
export class Reveal implements OnInit, OnDestroy {
  private el = inject(ElementRef<HTMLElement>);
  private browser = isPlatformBrowser(inject(PLATFORM_ID));
  private io?: IntersectionObserver;

  ngOnInit() {
    const e = this.el.nativeElement;
    e.classList.add('reveal');
    if (!this.browser || !('IntersectionObserver' in window)) { e.classList.add('in'); return; }
    this.io = new IntersectionObserver(([x]) => {
      if (x.isIntersecting) { e.classList.add('in'); this.io?.disconnect(); }
    }, { threshold: .12, rootMargin: '0px 0px -40px 0px' });
    this.io.observe(e);
  }
  ngOnDestroy() { this.io?.disconnect(); }
}