import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavLink } from '../../models/grit.models';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
})
export class HeaderComponent {
  readonly isMenuOpen = signal(false);

  readonly navLinks: NavLink[] = [
    { label: 'ARQUITECTURA', href: '#arquitectura' },
    { label: 'CAPACIDADES', href: '#capacidades' },
    { label: 'VERIFICACIÓN', href: '#verificacion' },
  ];

  toggleMenu(): void {
    this.isMenuOpen.update(v => !v);
  }
}
