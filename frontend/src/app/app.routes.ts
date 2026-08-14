import { Routes } from '@angular/router';
import { sesionGuard } from './core/sesion.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(c => c.LoginComponent)
  },
  {
    path: '',
    canActivate: [sesionGuard],
    loadComponent: () => import('./shared/layout/layout.component').then(c => c.LayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'resumen' },
      {
        path: 'resumen',
        loadComponent: () =>
          import('./features/resumen/resumen.component').then(c => c.ResumenComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
