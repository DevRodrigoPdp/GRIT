import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './footer.html',
})
export class FooterComponent implements AfterViewInit, OnDestroy {
  @ViewChild('bgVideo') bgVideoRef!: ElementRef<HTMLVideoElement>;

  readonly year = new Date().getFullYear();
  private observer!: IntersectionObserver;

  ngAfterViewInit() {
    const video = this.bgVideoRef.nativeElement;

    video.muted = true; // 👈 forzar por código
    video.volume = 0;   // 👈 doble seguridad

    this.observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) video.play().catch(() => { });
      else video.pause();
    }, { threshold: 0.1 });
    this.observer.observe(video);
  }

  ngOnDestroy() {
    this.observer?.disconnect();
  }
}
