import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';

const API_REFRESH = '/api/v1/auth/refresh';
let refreshing  = false;
let loggingOut  = false;
const refreshDone$ = new BehaviorSubject<boolean>(false);

export function setLoggingOut(value: boolean) { loggingOut = value; }

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const http   = inject(HttpClient);
  const conCredenciales = req.clone({ withCredentials: true });

  console.log(`[INTERCEPTOR] ${req.method} ${req.url}`);

  return next(conCredenciales).pipe(
    catchError((error: HttpErrorResponse) => {
      console.log(`[INTERCEPTOR] Error ${error.status} en ${req.url}`);
      
      if (error.status !== 401 || loggingOut) {
        return throwError(() => error);
      }

      // El refresh mismo falló → limpiar estado e ir al login (pero sin navegar aquí)
      if (req.url.includes(API_REFRESH) || req.url.includes('/auth/login') || req.url.includes('/auth/me')) {
        console.log(`[INTERCEPTOR] No intentar refresh para ${req.url}`);
        refreshing = false;
        refreshDone$.next(false);
        // No navegar desde el interceptor - deja que el componente/guard lo haga
        return throwError(() => error);
      }

      // Otra petición ya está refrescando → encolar y reintentar cuando termine
      if (refreshing) {
        console.log(`[INTERCEPTOR] Ya refrescando, encolando petición`);
        return refreshDone$.pipe(
          filter(done => done),
          take(1),
          switchMap(() => next(conCredenciales))
        );
      }

      // Primera petición que recibe 401 → intentar refresh
      console.log(`[INTERCEPTOR] Intentando refresh`);
      refreshing = true;
      refreshDone$.next(false);

      return http.post(API_REFRESH, {}, { withCredentials: true }).pipe(
        switchMap(() => {
          console.log(`[INTERCEPTOR] Refresh exitoso, reintentando petición`);
          refreshing = false;
          refreshDone$.next(true);
          return next(conCredenciales);
        }),
        catchError((e) => {
          console.log(`[INTERCEPTOR] Refresh falló`);
          refreshing = false;
          refreshDone$.next(false);
          // No navegar desde el interceptor - deja que el componente/guard lo haga
          return throwError(() => e);
        })
      );
    })
  );
};
