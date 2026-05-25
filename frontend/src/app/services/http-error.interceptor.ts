import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { NotifyService } from '../ui/notify.service';
import { apiErrorMessage } from '../utils/api-error';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const notify = inject(NotifyService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/auth/login')) {
        return auth.refreshSession().pipe(
          switchMap(() => next(req)),
          catchError(() => {
            auth.logout();
            notify.show('error', 'Session expired. Please login again.');
            return throwError(() => err);
          })
        );
      }
      if (err.status >= 500) {
        notify.show('error', apiErrorMessage(err, 'Server error. Please try again later.'));
      }
      return throwError(() => err);
    })
  );
};
