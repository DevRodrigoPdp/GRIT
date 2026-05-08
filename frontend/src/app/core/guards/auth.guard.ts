import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { of } from 'rxjs';
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
 * Espera a que la sesión sea inicializada por APP_INITIALIZER.
 */
export function rolGuard(rolRequerido: Rol): CanActivateFn {
  return async (_route, state) => {
    const auth   = inject(AuthService);
    const router = inject(Router);

    // Esperar a que la sesión esté inicializada (APP_INITIALIZER la marca)
    let intentos = 0;
    while (!auth.sessionInitialized() && intentos < 100) {
      await new Promise(resolve => setTimeout(resolve, 50));
      intentos++;
    }

    const rol = auth.rol();
    console.log('Guard: rol=', rol, 'requerido=', rolRequerido);

    // No hay autenticación
    if (!rol) {
      console.log('Guard: sin rol, redirigiendo a login');
      return router.createUrlTree(['/login']);
    }

    // Rol incorrecto
    if (rol !== rolRequerido) {
      console.log('Guard: rol incorrecto, redirigiendo a home');
      return router.createUrlTree(['/']);
    }

    // Verificar estado de la cuenta
    const estado = auth.estado();
    if (estado === 'PENDIENTE_REVISION') {
      if (rol === 'ENTRENADOR' && !state.url.startsWith('/pendiente')) {
        console.log('Guard: entrenador pendiente, redirigiendo a /pendiente');
        return router.createUrlTree(['/pendiente']);
      }
      if (rol !== 'ENTRENADOR') {
        console.log('Guard: cuenta pendiente (no entrenador), redirigiendo a login');
        return router.createUrlTree(['/login']);
      }
    }

    if (estado === 'RECHAZADO') {
      console.log('Guard: cuenta rechazada, redirigiendo a login');
      return router.createUrlTree(['/login']);
    }

    console.log('Guard: acceso permitido');
    return true;
  };
}
