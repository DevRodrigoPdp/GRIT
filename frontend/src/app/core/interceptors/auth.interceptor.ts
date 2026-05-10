import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';

const API_REFRESH = '/api/v1/auth/refresh';
let refreshing  = false;
let loggingOut  = false;
const refreshDone$ = new BehaviorSubject<boolean | null>(null);

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

      if (req.url.includes(API_REFRESH) || req.url.includes('/auth/login') || req.url.includes('/auth/me')) {
        refreshing = false;
        refreshDone$.next(false);
        return throwError(() => error);
      }

      if (refreshing) {
        return refreshDone$.pipe(
          filter(done => done !== null),
          take(1),
          switchMap((done) => done ? next(conCredenciales) : throwError(() => error))
        );
      }

      refreshing = true;
      refreshDone$.next(null);

      return http.post(API_REFRESH, {}, { withCredentials: true }).pipe(
        switchMap(() => {
          refreshing = false;
          refreshDone$.next(true);
          return next(conCredenciales);
        }),
        catchError((e) => {
          refreshing = false;
          refreshDone$.next(false);
          return throwError(() => e);
        })
      );
    })
  );
};
