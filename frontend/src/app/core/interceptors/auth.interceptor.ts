import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpContextToken } from '@angular/common/http';
import { throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';

export interface ApiError {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errorCode?: string; // Tu propiedad personalizada en Spring
}

const API_REFRESH = '/api/v1/auth/refresh';
const IS_RETRY_CSRF = new HttpContextToken<boolean>(() => false);
let refreshing = false;
let loggingOut = false;
const refreshDone$ = new BehaviorSubject<boolean | null>(null);

export function setLoggingOut(value: boolean) { loggingOut = value; }

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const http = inject(HttpClient);

  if (req.context.get(IS_RETRY_CSRF)) {
    return next(req.clone({ withCredentials: true }));
  }

  const secureReq = req.clone({ withCredentials: true });

  return next(secureReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Casteo seguro al estándar Problem Details
      const apiError = error.error as ApiError;
      const errorCode = apiError?.errorCode;

      // --- MANEJO DE CSRF (403) ---
      if (error.status === 403 && errorCode === 'SECURITY_CSRF_INVALID') {
        if (req.method === 'GET') return throwError(() => error);

        return http.get('/api/v1/auth/me', { withCredentials: true }).pipe(
          switchMap(() => {
            const retryReq = req.clone({
              withCredentials: true,
              context: req.context.set(IS_RETRY_CSRF, true)
            });
            return next(retryReq);
          }),
          catchError(err => {
            router.navigate(['/login']);
            return throwError(() => err);
          })
        );
      }

      // --- MANEJO DE EXPIRACIÓN (401) ---
      if (error.status === 401 && !loggingOut) {
        if (req.url.includes(API_REFRESH) || req.url.includes('/login')) {
          return throwError(() => error);
        }

        if (refreshing) {
          return refreshDone$.pipe(
            filter(done => done !== null),
            take(1),
            switchMap(isSuccess => isSuccess ? next(secureReq) : throwError(() => error))
          );
        }

        refreshing = true;
        refreshDone$.next(null);

        return http.post(API_REFRESH, {}, { withCredentials: true }).pipe(
          switchMap(() => {
            refreshing = false;
            refreshDone$.next(true);
            return next(secureReq);
          }),
          catchError(refreshErr => {
            refreshing = false;
            refreshDone$.next(false);
            router.navigate(['/login']);
            return throwError(() => refreshErr);
          })
        );
      }

      return throwError(() => error);
    })
  );
};