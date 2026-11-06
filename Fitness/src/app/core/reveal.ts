import { Directive, ElementRef, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Directive({ selector: '[reveal]', standalone: true })
export class Reveal implements OnInit {
  private el = inject(ElementRef<HTMLElement>);
  private browser = isPlatformBrowser(inject(PLATFORM_ID));
  ngOnInit() {
    const e = this.el.nativeElement;
    e.classList.add('reveal');
    if (!this.browser || !('IntersectionObserver' in window)) { e.classList.add('in'); return; }
    const io = new IntersectionObserver(([x]) => { if (x.isIntersecting) { e.classList.add('in'); io.disconnect(); } },
      { threshold: .15 });
    io.observe(e);
  }
}