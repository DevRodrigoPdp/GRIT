import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavLink } from '../models/grit.models';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
})
export class HeaderComponent {
  readonly isMenuOpen = signal(false);

  readonly navLinks: NavLink[] = [
    { label: 'CÓMO FUNCIONA', href: '#como-funciona' },
    { label: 'PARA QUIÉN', href: '#para-quien' },
  ];

  toggleMenu(): void {
    this.isMenuOpen.update(v => !v);
  }

  scrollTo(event: Event, href: string): void {
    event.preventDefault();
    const id = href.replace('#', '');
    const el = document.getElementById(id);
    if (el) el.scrollIntoView({ behavior: 'instant' });
  }
}
