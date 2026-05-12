import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, of } from 'rxjs';
import { AuthService, Rol } from '../services/auth.service';

function entrenadorPendiente(auth: AuthService): boolean {
  const rev = auth.estadoRevision();
  // estadoRevision es la fuente de verdad; si no viene, fallback a estado
  return rev !== null ? rev !== 'APROBADO' : auth.estado() !== 'ACTIVO';
}

/** Redirige al dashboard correspondiente si el usuario ya tiene sesión activa en memoria. */
export const noAuthGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.sessionVerified()) return of(true);

  const rol = auth.rol();
  if (rol === 'ATLETA') return of(router.createUrlTree(['/dashboard/atleta']));
  if (rol === 'ENTRENADOR') {
    if (entrenadorPendiente(auth)) return of(router.createUrlTree(['/pendiente']));
    return of(router.createUrlTree(['/dashboard/entrenador']));
  }
  if (rol === 'ADMIN') return of(router.createUrlTree(['/admin']));
  return of(true);
};

export function rolGuard(rolRequerido: Rol): CanActivateFn {
  return (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    const verificar = (rol: Rol | null) => {
      if (!rol) return router.createUrlTree(['/login']);
      if (rol !== rolRequerido) return router.createUrlTree(['/']);
      if (rol === 'ENTRENADOR' && entrenadorPendiente(auth) && !state.url.startsWith('/pendiente')) {
        return router.createUrlTree(['/pendiente']);
      }
      return true;
    };

    if (auth.rol() !== null) {
      if (auth.rol() === 'ENTRENADOR' && entrenadorPendiente(auth) && !auth.sessionVerified()) {
        return auth.me().pipe(map(() => verificar(auth.rol())));
      }
      return of(verificar(auth.rol()));
    }

    return auth.me().pipe(
      map(() => verificar(auth.rol()))
    );
  };
}
