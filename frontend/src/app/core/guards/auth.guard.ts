import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, of } from 'rxjs';
import { AuthService, Rol } from '../services/auth.service';


/** Redirige al dashboard correspondiente si el usuario ya tiene sesión activa en memoria. */
export const noAuthGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const rol = auth.rol();
  if (rol === 'ATLETA') return of(router.createUrlTree(['/dashboard/atleta']));
  if (rol === 'ENTRENADOR') {
    if (auth.estado() !== 'ACTIVO') return of(router.createUrlTree(['/pendiente']));
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
      if (rol === 'ENTRENADOR' && auth.estado() !== 'ACTIVO' && !state.url.startsWith('/pendiente')) {
        return router.createUrlTree(['/pendiente']);
      }
      return true;
    };

    // Sesión ya cargada → respuesta inmediata, SALVO entrenador con estado no-ACTIVO
    // (localStorage puede tener dato stale de cuando se registró como PENDIENTE_REVISION)
    if (auth.rol() !== null) {
      if (auth.rol() === 'ENTRENADOR' && auth.estado() !== 'ACTIVO') {
        return auth.me().pipe(map(() => verificar(auth.rol())));
      }
      return of(verificar(auth.rol()));
    }

    // Sesión no cargada → intentar restaurar con cookie
    return auth.me().pipe(
      map(() => verificar(auth.rol()))
    );
  };
}
