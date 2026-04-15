import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, of } from 'rxjs';
import { AuthService, Rol } from '../services/auth.service';

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
