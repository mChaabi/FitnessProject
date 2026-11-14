import { Directive, ElementRef, OnDestroy, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/** Moves the element to <body>, so no parent animation or z-index can trap it. */
@Directive({ selector: '[portal]', standalone: true })
export class Portal implements OnInit, OnDestroy {
  private el = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private browser = isPlatformBrowser(inject(PLATFORM_ID));

  ngOnInit() { if (this.browser) document.body.appendChild(this.el); }
  ngOnDestroy() { this.el.remove(); }
}