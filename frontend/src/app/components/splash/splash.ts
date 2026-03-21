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
    // Start exit animation after 2.2s, remove from DOM after 3s
    setTimeout(() => this.leaving.set(true), 2200);
    setTimeout(() => this.visible.set(false), 3000);
  }
}
