import { HttpInterceptorFn } from '@angular/common/http';

/**
 * La sesion viaja en cookie, no en header. Todas las llamadas a la API tienen que ir con
 * credenciales.
 */
export const credencialesInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  return siguiente(peticion.clone({ withCredentials: true }));
};
