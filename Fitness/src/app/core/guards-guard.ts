import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../core/auth/auth-service';
import { Role } from './auth/auth';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() ? true : inject(Router).createUrlTree(['/login']);
};

export const roleGuard = (...allowed: Role[]): CanActivateFn => (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn())
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  return allowed.includes(auth.role()!) ? true : router.createUrlTree([auth.homeFor(auth.role())]);
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() ? inject(Router).createUrlTree([auth.homeFor(auth.role())]) : true;
};

