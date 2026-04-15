import { Component, inject, computed, output, input } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { PerfilEntrenador } from '../../services/entrenador.service';

@Component({
  selector: 'app-perfil-entrenador',
  standalone: true,
  templateUrl: './perfil-entrenador.html',
})
export class PerfilEntrenadorComponent {
  readonly auth      = inject(AuthService);
  readonly verPerfil = output<void>();
  readonly perfil    = input<PerfilEntrenador | null>(null);

  readonly nombre = computed(() => this.perfil()?.nombre ?? this.auth.nombre() ?? '');

  readonly iniciales = computed(() => {
    const nombre = this.nombre();
    if (!nombre) return '?';
    return nombre
      .split(' ')
      .slice(0, 2)
      .map(p => p[0])
      .join('')
      .toUpperCase();
  });
}
