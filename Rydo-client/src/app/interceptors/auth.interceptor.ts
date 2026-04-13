import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('token') : null;
  const userId = typeof localStorage !== 'undefined' ? localStorage.getItem('userId') : null;

  const headers: { [key: string]: string } = {};

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (userId && userId !== 'undefined' && userId !== 'null') {
    headers['X-User-Id'] = userId;
  }

  if (Object.keys(headers).length > 0) {
    req = req.clone({ setHeaders: headers });
  }

  return next(req);
};
