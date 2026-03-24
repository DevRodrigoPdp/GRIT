import { ChangeDetectorRef, Component, ElementRef, NgZone, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-scroll-video',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './scroll-video.html',
})
export class ScrollVideoComponent implements OnInit, OnDestroy {
  @ViewChild('container', { static: true }) containerRef!: ElementRef<HTMLDivElement>;
  @ViewChild('video') videoRef?: ElementRef<HTMLVideoElement>;
  @ViewChild('videoMobile') videoMobileRef?: ElementRef<HTMLVideoElement>;

  progress = 0;
  phase1Opacity = 1;
  phase2Opacity = 0;
  phase3Opacity = 0;
  barHeight = '0%';

  private targetProgress = 0;
  private rafId = 0;
  private zone = inject(NgZone);
  private cdr = inject(ChangeDetectorRef);

  private onScroll = () => {
    const container = this.containerRef.nativeElement;
    const rect = container.getBoundingClientRect();
    const scrollable = container.offsetHeight - window.innerHeight;
    const scrolled = -rect.top;
    this.targetProgress = Math.min(Math.max(scrolled / scrollable, 0), 1);
  };

  private calcBarHeight(p: number): number {
    const target = 11;
    if (p <= 0) return 0;
    if (p < 0.08) return (p / 0.08) * target;
    if (p < 0.92) return target;
    if (p < 1) return ((1 - p) / 0.08) * target;
    return 0;
  }

  private fadePhase(p: number, start: number, end: number): number {
    const fadeIn = 0.05;
    if (p < start) return 0;
    if (start > 0 && p < start + fadeIn) return (p - start) / fadeIn;
    if (p < end - fadeIn) return 1;
    if (p < end) return (end - p) / fadeIn;
    return 0;
  }

  private seekVideo(el: HTMLVideoElement) {
    if (!el.duration) return;
    const target = this.targetProgress * el.duration;
    const diff = target - el.currentTime;
    if (Math.abs(diff) > 0.01) el.currentTime += diff * 0.12;
  }

  private loop = () => {
    if (this.videoRef) this.seekVideo(this.videoRef.nativeElement);
    if (this.videoMobileRef) this.seekVideo(this.videoMobileRef.nativeElement);

    const p = this.targetProgress;
    const p1 = this.fadePhase(p, 0, 0.35);
    const p2 = this.fadePhase(p, 0.35, 0.68);
    const p3 = this.fadePhase(p, 0.68, 1.05);
    const bar = this.calcBarHeight(p) + '%';

    if (p !== this.progress || p1 !== this.phase1Opacity || p2 !== this.phase2Opacity || p3 !== this.phase3Opacity) {
      this.zone.run(() => {
        this.progress = p;
        this.phase1Opacity = p1;
        this.phase2Opacity = p2;
        this.phase3Opacity = p3;
        this.barHeight = bar;
        this.cdr.detectChanges();
      });
    }

    this.rafId = requestAnimationFrame(this.loop);
  };

  ngOnInit() {
    this.zone.runOutsideAngular(() => {
      window.addEventListener('scroll', this.onScroll, { passive: true });
      this.rafId = requestAnimationFrame(this.loop);
    });
  }

  ngOnDestroy() {
    window.removeEventListener('scroll', this.onScroll);
    cancelAnimationFrame(this.rafId);
  }
}
