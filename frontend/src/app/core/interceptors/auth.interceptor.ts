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

  return next(conCredenciales).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || loggingOut) {
        return throwError(() => error);
      }

      // El refresh mismo falló → limpiar estado e ir al login
      if (req.url.includes(API_REFRESH) || req.url.includes('/auth/login')) {
        refreshing = false;
        refreshDone$.next(false);
        router.navigate(['/login']);
        return throwError(() => error);
      }

      // Otra petición ya está refrescando → encolar y reintentar cuando termine
      if (refreshing) {
        return refreshDone$.pipe(
          filter(done => done),
          take(1),
          switchMap(() => next(conCredenciales))
        );
      }

      // Primera petición que recibe 401 → intentar refresh
      refreshing = true;
      refreshDone$.next(false);

      return http.post(API_REFRESH, {}, { withCredentials: true }).pipe(
        switchMap(() => {
          refreshing = false;
          refreshDone$.next(true);
          return next(conCredenciales);
        }),
        catchError((e) => {
          refreshing = false;
          refreshDone$.next(false);
          router.navigate(['/login']);
          return throwError(() => e);
        })
      );
    })
  );
};
