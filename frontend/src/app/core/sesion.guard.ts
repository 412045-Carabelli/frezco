import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionService } from './sesion.service';

export const sesionGuard: CanActivateFn = () => {
  const sesion = inject(SesionService);
  const router = inject(Router);

  if (!sesion.autenticado()) {
    return router.createUrlTree(['/login']);
  }
  if (!sesion.tieneAccesoFrezco()) {
    sesion.descartarSesion();
    return router.createUrlTree(['/login']);
  }
  return true;
};
