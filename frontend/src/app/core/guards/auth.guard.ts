import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, of } from 'rxjs';
import { AuthService, Rol } from '../services/auth.service';

/** Redirige al dashboard correspondiente si el usuario ya tiene sesión activa. */
export const noAuthGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  const redirigir = (rol: Rol | null) => {
    if (!rol) return true;
    if (rol === 'ATLETA')     return router.createUrlTree(['/dashboard/atleta']);
    if (rol === 'ENTRENADOR') return router.createUrlTree(['/dashboard/entrenador']);
    if (rol === 'ADMIN')      return router.createUrlTree(['/admin']);
    return true;
  };

  if (auth.rol() !== null) return of(redirigir(auth.rol()));

  return auth.me().pipe(map(() => redirigir(auth.rol())));
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
  return () => {
    const auth   = inject(AuthService);
    const router = inject(Router);

    const verificar = (rol: Rol | null) => {
      if (!rol) return router.createUrlTree(['/login']);
      if (rol !== rolRequerido) return router.createUrlTree(['/']);
      return true;
    };

    // Sesión ya cargada → respuesta inmediata
    if (auth.rol() !== null) {
      return of(verificar(auth.rol()));
    }

    // Sesión no cargada → intentar restaurar con cookie
    return auth.me().pipe(
      map(() => verificar(auth.rol()))
    );
  };
}
