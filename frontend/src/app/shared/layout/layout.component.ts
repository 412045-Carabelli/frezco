import { Component, inject, signal } from '@angular/core';
import { Location, NgTemplateOutlet } from '@angular/common';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { DrawerModule } from 'primeng/drawer';
import { TooltipModule } from 'primeng/tooltip';
import { FormsModule } from '@angular/forms';
import { MARCA } from '../../config/marca';
import { ECOSISTEMA } from '../../config/ecosistema';
import { SesionService } from '../../core/sesion.service';
import { TutorialService } from '../../core/tutorial.service';

interface ItemMenu {
  etiqueta: string;
  icono: string;
  ruta: string;
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive, NgTemplateOutlet, FormsModule, ButtonModule,
    ToggleSwitchModule, DrawerModule, TooltipModule
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {

  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);
  private readonly ubicacion = inject(Location);

  readonly tutorial = inject(TutorialService);
  readonly marca = MARCA;
  readonly ecosistema = ECOSISTEMA;
  menuAbierto = false;
  readonly rutaActual = signal('');

  readonly items: ItemMenu[] = [
    { etiqueta: 'Resumen', icono: 'pi-home', ruta: '/resumen' },
    { etiqueta: 'Pedidos', icono: 'pi-shopping-cart', ruta: '/pedidos' },
    { etiqueta: 'Cobros y pagos', icono: 'pi-wallet', ruta: '/movimientos' },
    { etiqueta: 'Stock', icono: 'pi-box', ruta: '/stock' },
    { etiqueta: 'Cuentas corrientes', icono: 'pi-book', ruta: '/cuentas-corrientes' },
    { etiqueta: 'Remito proveedor', icono: 'pi-file', ruta: '/remito-proveedor' },
    { etiqueta: 'Análisis comercial', icono: 'pi-chart-bar', ruta: '/analisis-comercial' },
    { etiqueta: 'Articulos', icono: 'pi-tag', ruta: '/articulos' },
    { etiqueta: 'Cuentas', icono: 'pi-users', ruta: '/cuentas' }
  ];

  constructor() {
    this.router.events
      .pipe(filter(evento => evento instanceof NavigationEnd))
      .subscribe(evento => this.alCambiarDePantalla((evento as NavigationEnd).urlAfterRedirects));
  }

  alternarMenu(): void {
    this.menuAbierto = !this.menuAbierto;
  }

  cerrarMenu(): void {
    this.menuAbierto = false;
  }

  ayudaDeEstaPantalla(): void {
    this.cerrarMenu();
    this.tutorial.iniciar(this.rutaActual());
  }

  hayAyuda(): boolean {
    return this.tutorial.hayAyudaPara(this.rutaActual());
  }

  /** En el inicio no hay "atras" util: es la primera pantalla del flujo. */
  hayAtras(): boolean {
    return this.rutaActual() !== '/resumen' && this.rutaActual() !== '';
  }

  volverAtras(): void {
    this.ubicacion.back();
  }

  salir(): void {
    this.sesion.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }

  /** El tutorial se abre solo en cada pantalla nueva mientras la ayuda automatica este activa. */
  private alCambiarDePantalla(url: string): void {
    const ruta = url.split('?')[0];
    this.rutaActual.set(ruta);
    this.cerrarMenu();
    this.tutorial.iniciarSiCorresponde(ruta);
  }
}
