import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MARCA } from '../../config/marca';
import { SesionService } from '../../core/sesion.service';

interface ItemMenu {
  etiqueta: string;
  icono: string;
  ruta: string;
  /** Las pantallas que todavia no existen se muestran deshabilitadas. */
  disponible: boolean;
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
    { etiqueta: 'Resumen', icono: 'pi-home', ruta: '/resumen', disponible: true },
    { etiqueta: 'Pedidos', icono: 'pi-shopping-cart', ruta: '/pedidos', disponible: false },
    { etiqueta: 'Cobros y pagos', icono: 'pi-wallet', ruta: '/movimientos', disponible: false },
    { etiqueta: 'Stock', icono: 'pi-box', ruta: '/stock', disponible: false },
    { etiqueta: 'Cuentas corrientes', icono: 'pi-book', ruta: '/cuentas-corrientes', disponible: false },
    { etiqueta: 'Remito proveedor', icono: 'pi-file', ruta: '/remito-proveedor', disponible: false },
    { etiqueta: 'Articulos', icono: 'pi-tag', ruta: '/articulos', disponible: false },
    { etiqueta: 'Cuentas', icono: 'pi-users', ruta: '/cuentas', disponible: false }
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
