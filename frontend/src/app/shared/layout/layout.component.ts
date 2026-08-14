import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MARCA } from '../../config/marca';
import { SesionService } from '../../core/sesion.service';

interface ItemMenu {
  etiqueta: string;
  icono: string;
  ruta: string;
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ButtonModule],
  templateUrl: './layout.component.html'
})
export class LayoutComponent {

  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);

  readonly marca = MARCA;
  readonly menuAbierto = signal(false);

  readonly items: ItemMenu[] = [
    { etiqueta: 'Resumen', icono: 'pi-home', ruta: '/resumen' },
    { etiqueta: 'Pedidos', icono: 'pi-shopping-cart', ruta: '/pedidos' },
    { etiqueta: 'Cobros y pagos', icono: 'pi-wallet', ruta: '/movimientos' },
    { etiqueta: 'Stock', icono: 'pi-box', ruta: '/stock' },
    { etiqueta: 'Cuentas corrientes', icono: 'pi-book', ruta: '/cuentas-corrientes' },
    { etiqueta: 'Remito proveedor', icono: 'pi-file', ruta: '/remito-proveedor' },
    { etiqueta: 'Articulos', icono: 'pi-tag', ruta: '/articulos' },
    { etiqueta: 'Cuentas', icono: 'pi-users', ruta: '/cuentas' }
  ];

  alternarMenu(): void {
    this.menuAbierto.update(abierto => !abierto);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  salir(): void {
    this.sesion.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
