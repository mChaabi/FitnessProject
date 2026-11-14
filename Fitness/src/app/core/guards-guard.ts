import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from './auth/auth-service';
import { Role } from './auth/auth';
import { MeService } from '../data/me/service/me';

/** Logged in, any role. */
export const authGuard: CanActivateFn = (_r, state) => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() ? true
    : inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Anonymous -> login (remembering where they wanted to go). Wrong role -> their own home. */
export const roleGuard = (...allowed: Role[]): CanActivateFn => (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn())
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  return allowed.includes(auth.role()!) ? true : router.createUrlTree([auth.homeFor(auth.role())]);
};

/** Login and sign-up are only for guests. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() ? inject(Router).createUrlTree([auth.homeFor(auth.role())]) : true;
};

/** "/espace" and "/admin": everyone goes to THEIR home. */
export const homeGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return inject(Router).createUrlTree([auth.homeFor(auth.role())]);
};

/** Booking needs an ACTIVE subscription: otherwise back to the member home with a message. */
export const activeMemberGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(MeService).overview().pipe(
    map(o => o.status === 'ACTIVE' ? true
      : router.createUrlTree(['/member/home'], { queryParams: { need: 'subscription' } })),
    catchError(() => of(router.createUrlTree(['/member/home'])))
  );
};