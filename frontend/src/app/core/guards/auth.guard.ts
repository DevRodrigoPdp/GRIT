import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, of } from 'rxjs';
import { AuthService, Rol } from '../services/auth.service';


/** Redirige al dashboard correspondiente si el usuario ya tiene sesión activa en memoria. */
export const noAuthGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  const rol = auth.rol();
  if (rol === 'ATLETA')     return of(router.createUrlTree(['/dashboard/atleta']));
  if (rol === 'ENTRENADOR') return of(router.createUrlTree(['/dashboard/entrenador']));
  if (rol === 'ADMIN')      return of(router.createUrlTree(['/admin']));
  return of(true);
};

/**
 * Guard de autenticación y autorización por rol.
 *
 * Si la sesión ya está cargada en signals → comprueba el rol directamente.
 * Si los signals están vacíos (recarga de página) → llama a GET /me para
 * restaurar la sesión desde la cookie HttpOnly antes de decidir.
 *
 * Uso en rutas:
 *   canActivate: [rolGuard('ATLETA')]
 *   canActivate: [rolGuard('ENTRENADOR')]
 *   canActivate: [rolGuard('ADMIN')]
 */
export function rolGuard(rolRequerido: Rol): CanActivateFn {
  return (_route, state) => {
    const auth   = inject(AuthService);
    const router = inject(Router);

    const verificar = (rol: Rol | null) => {
      console.log('Verificando rol:', rol, 'requerido:', rolRequerido);
      if (!rol) return router.createUrlTree(['/login']);
      if (rol !== rolRequerido) return router.createUrlTree(['/']);
      const estado = auth.estado();
      if (estado === 'PENDIENTE_REVISION') {
        if (rol === 'ENTRENADOR' && !state.url.startsWith('/pendiente')) {
          return router.createUrlTree(['/pendiente']);
        }
        if (rol !== 'ENTRENADOR') {
          return router.createUrlTree(['/login']);
        }
      }
      if (estado === 'RECHAZADO') {
        return router.createUrlTree(['/login']);
      }
      return true;
    };

    // Sesión ya cargada → respuesta inmediata
    if (auth.rol() !== null) {
      console.log('Sesión ya cargada en signals');
      return of(verificar(auth.rol()));
    }

    console.log('Sesión no cargada, intentando restaurar con cookie');
    // Sesión no cargada → intentar restaurar con cookie
    return auth.me().pipe(
      map(() => {
        console.log('Sesión restaurada, verificando rol');
        return verificar(auth.rol());
      })
    );
  };
}
