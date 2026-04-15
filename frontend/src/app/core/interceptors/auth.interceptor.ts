import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError, BehaviorSubject, Observable } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

const API_REFRESH = '/api/v1/auth/refresh';

// Estado compartido del refresco — evita llamadas paralelas al mismo endpoint
let refreshing          = false;
const refreshDone$      = new BehaviorSubject<boolean>(false);

/**
 * Interceptor de autenticación.
 *
 * 1. Añade withCredentials a todas las peticiones → el navegador envía
 *    automáticamente access_token y refresh_token como cookies HttpOnly.
 *
 * 2. Si el servidor responde 401 (access_token expirado):
 *    a. Llama a POST /api/v1/auth/refresh (el navegador envía refresh_token).
 *    b. Si el refresh tiene éxito → reintenta la petición original.
 *    c. Si el refresh falla (refresh_token expirado) → redirige a /login.
 *
 * 3. Si el 401 llega desde el propio endpoint de refresh → redirige a /login
 *    sin reintentar (evita bucle infinito).
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<any> => {
  const router = inject(Router);
  const http   = inject(HttpClient);

  // Añadir withCredentials a todas las peticiones
  const reqConCredenciales = req.clone({ withCredentials: true });

  return next(reqConCredenciales).pipe(
    catchError((error: HttpErrorResponse) => {
      // Solo actuar ante 401 y fuera del endpoint de refresh
      if (error.status !== 401 || req.url.includes(API_REFRESH)) {
        if (error.status === 401) {
          // 401 en el refresh → sesión completamente expirada
          refreshing = false;
          refreshDone$.next(false);
          router.navigate(['/login']);
        }
        return throwError(() => error);
      }

      // Si ya hay un refresh en curso, esperar a que termine y reintentar
      if (refreshing) {
        return refreshDone$.pipe(
          filter(done => done),
          take(1),
          switchMap(() => next(reqConCredenciales))
        );
      }

      // Iniciar refresco
      refreshing = true;
      refreshDone$.next(false);

      return http
        .post(API_REFRESH, {}, { withCredentials: true })
        .pipe(
          switchMap(() => {
            refreshing = false;
            refreshDone$.next(true);
            // Reintentar la petición original con el nuevo access_token
            return next(reqConCredenciales);
          }),
          catchError((refreshError) => {
            // Refresh fallido → sesión expirada, ir al login
            refreshing = false;
            refreshDone$.next(false);
            router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
    })
  );
};
