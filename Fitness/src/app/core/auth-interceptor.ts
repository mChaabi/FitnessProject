import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service';
import { API } from './api';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(API)) return next(req);          // nunca se envía el token a terceros
  const auth = inject(AuthService);
  const isAuthCall = req.url.startsWith(`${API}/auth/`);

  const prepare = (r: HttpRequest<unknown>) => {
    const t = auth.token();
    return r.clone({
      withCredentials: true,
      setHeaders: { 'X-Requested-With': 'fitforge', ...(t && !isAuthCall ? { Authorization: `Bearer ${t}` } : {}) }
    });
  };

  return next(prepare(req)).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401 || isAuthCall) return throwError(() => err);
      return auth.refresh().pipe(
        switchMap(() => next(prepare(req))),                // reintento con el token nuevo
        catchError(() => { auth.expire(); return throwError(() => err); }));
    }));
};