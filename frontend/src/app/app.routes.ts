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
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('./features/pedidos/pedidos.component').then(c => c.PedidosComponent)
      },
      {
        path: 'pedidos/nuevo',
        loadComponent: () =>
          import('./features/pedidos/nuevo-pedido.component').then(c => c.NuevoPedidoComponent)
      },
      {
        path: 'remitos/cliente/:pedidoId',
        loadComponent: () =>
          import('./features/remitos/remito-cliente.component').then(c => c.RemitoClienteComponent)
      },
      {
        path: 'remito-proveedor',
        loadComponent: () =>
          import('./features/remitos/remito-proveedor.component')
            .then(c => c.RemitoProveedorComponent)
      },
      {
        path: 'stock',
        loadComponent: () => import('./features/stock/stock.component').then(c => c.StockComponent)
      },
      {
        path: 'cuentas-corrientes',
        loadComponent: () =>
          import('./features/cuentas-corrientes/cuentas-corrientes.component')
            .then(c => c.CuentasCorrientesComponent)
      },
      {
        path: 'movimientos',
        loadComponent: () =>
          import('./features/movimientos/movimientos.component').then(c => c.MovimientosComponent)
      },
      {
        path: 'articulos',
        loadComponent: () =>
          import('./features/articulos/articulos.component').then(c => c.ArticulosComponent)
      },
      {
        path: 'cuentas',
        loadComponent: () =>
          import('./features/cuentas/cuentas.component').then(c => c.CuentasComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
