import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { SesionService } from './sesion.service';

/**
 * La identidad viaja en el JWT del Auth Service compartido, no en cookie: cada request a la
 * API va con el access token en el header Authorization. El Gateway lo valida y le inyecta
 * los headers de identidad a este backend.
 */
export const credencialesInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const token = inject(SesionService).accessToken();
  if (!token) {
    return siguiente(peticion);
  }
  return siguiente(peticion.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  }));
};
