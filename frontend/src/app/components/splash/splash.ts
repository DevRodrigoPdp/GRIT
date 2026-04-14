import { Component, OnInit, signal } from '@angular/core';

@Component({
  selector: 'app-splash',
  standalone: true,
  imports: [],
  templateUrl: './splash.html',
})
export class SplashComponent implements OnInit {
  readonly visible = signal(true);
  readonly leaving = signal(false);

  ngOnInit(): void {
    // Start exit animation after 0.9s, remove from DOM after 1.6s
    setTimeout(() => this.leaving.set(true), 900);
    setTimeout(() => this.visible.set(false), 1600);
  }
}
