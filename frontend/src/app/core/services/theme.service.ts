import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly key = 'grit-theme';

  readonly isDark = signal<boolean>(
    localStorage.getItem(this.key) !== 'light'
  );

  constructor() {
    effect(() => {
      const dark = this.isDark();
      const html = document.documentElement;
      html.classList.add('theme-transitioning');
      html.classList.toggle('light', !dark);
      localStorage.setItem(this.key, dark ? 'dark' : 'light');
      setTimeout(() => html.classList.remove('theme-transitioning'), 400);
    });
  }

  toggle(): void {
    this.isDark.update(v => !v);
  }

  set(dark: boolean): void {
    this.isDark.set(dark);
  }
}
