import { Component, signal, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavLink } from '../models/grit.models';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
})
export class HeaderComponent {
  readonly vistaActual = input<string>('inicio');
  readonly navClick    = output<string>();

  readonly isMenuOpen = signal(false);

  readonly navLinks: NavLink[] = [
    { label: 'CÓMO FUNCIONA', href: 'como-funciona' },
    { label: 'PARA QUIÉN',    href: 'para-quien'    },
    { label: 'PRECIOS',       href: 'precios'       },
  ];

  navegar(event: Event, href: string): void {
    event.preventDefault();
    this.navClick.emit(href);
    this.isMenuOpen.set(false);
  }

  toggleMenu(): void {
    this.isMenuOpen.update(v => !v);
  }
}
