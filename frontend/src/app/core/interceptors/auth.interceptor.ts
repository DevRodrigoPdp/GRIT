// import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
// import { inject } from '@angular/core';
// import { Router } from '@angular/router';
// import { throwError, BehaviorSubject, Observable } from 'rxjs';
// import { catchError, filter, take, switchMap } from 'rxjs/operators';
// import { HttpClient } from '@angular/common/http';
//
// const API_REFRESH = '/api/v1/auth/refresh';
// let refreshing = false;
// const refreshDone$ = new BehaviorSubject<boolean>(false);
//
// export const authInterceptor: HttpInterceptorFn = (req, next) => {
//   const router = inject(Router);
//   const http   = inject(HttpClient);
//   const reqConCredenciales = req.clone({ withCredentials: true });
//   return next(reqConCredenciales).pipe(
//     catchError((error: HttpErrorResponse) => {
//       if (error.status !== 401 || req.url.includes(API_REFRESH)) {
//         if (error.status === 401) { refreshing = false; refreshDone$.next(false); router.navigate(['/login']); }
//         return throwError(() => error);
//       }
//       if (refreshing) { return refreshDone$.pipe(filter(d => d), take(1), switchMap(() => next(reqConCredenciales))); }
//       refreshing = true; refreshDone$.next(false);
//       return http.post(API_REFRESH, {}, { withCredentials: true }).pipe(
//         switchMap(() => { refreshing = false; refreshDone$.next(true); return next(reqConCredenciales); }),
//         catchError((e) => { refreshing = false; refreshDone$.next(false); router.navigate(['/login']); return throwError(() => e); })
//       );
//     })
//   );
// };

import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { Observable } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<any> => {
  return next(req.clone({ withCredentials: true }));
};
