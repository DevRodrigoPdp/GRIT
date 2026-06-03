import { Directive, ElementRef, OnInit, OnDestroy, Renderer2 } from '@angular/core';

@Directive({
  selector: '[scrollIndicator]',
  standalone: true,
})
export class ScrollIndicatorDirective implements OnInit, OnDestroy {
  private indicator!: HTMLElement;
  private unlisten!: () => void;
  private ro!: ResizeObserver;

  constructor(private el: ElementRef<HTMLElement>, private renderer: Renderer2) {}

  ngOnInit() {
    const host = this.el.nativeElement;
    const parent = host.parentElement;
    if (!parent) return;

    if (getComputedStyle(parent).position === 'static') {
      this.renderer.setStyle(parent, 'position', 'relative');
    }

    this.indicator = this.renderer.createElement('div');
    this.indicator.className = 'scroll-indicator-hint';
    this.renderer.setProperty(this.indicator, 'innerHTML',
      `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14">
        <path d="M6 9l6 6 6-6" stroke-linecap="square"/>
      </svg>`
    );
    this.renderer.appendChild(parent, this.indicator);

    this.update();
    this.unlisten = this.renderer.listen(host, 'scroll', () => this.update());
    this.ro = new ResizeObserver(() => this.update());
    this.ro.observe(host);
  }

  private update() {
    const h = this.el.nativeElement;
    const hasScroll = h.scrollHeight > h.clientHeight + 4;
    const atBottom = h.scrollHeight - h.scrollTop <= h.clientHeight + 8;
    const show = hasScroll && !atBottom;
    this.renderer.setStyle(this.indicator, 'opacity', show ? '1' : '0');
  }

  ngOnDestroy() {
    if (this.unlisten) this.unlisten();
    if (this.ro) this.ro.disconnect();
    this.indicator?.remove();
  }
}
