import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ApiService } from '../../core/api.service';
import { Producto } from '../../core/modelos';

const PRODUCTO_NUEVO: Producto = {
  id: null, nombre: '', categoria: null, kg: null, lt: null,
  costo: 0, precioMinorista: 0, precioMayorista: 0, precioCantidad: 0, activo: true
};

@Component({
  selector: 'app-articulos',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, ButtonModule, DialogModule, InputNumberModule,
    InputTextModule, TableModule, TagModule, ConfirmDialogModule
  ],
  providers: [ConfirmationService],
  templateUrl: './articulos.component.html'
})
export class ArticulosComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);
  private readonly confirmacion = inject(ConfirmationService);

  readonly productos = signal<Producto[]>([]);
  readonly cargando = signal(false);
  readonly busqueda = signal('');
  readonly soloActivos = signal(true);

  readonly editando = signal<Producto | null>(null);
  readonly guardando = signal(false);

  /** Margen porcentual de cada precio contra el costo. Ayuda a decidir sin calculadora. */
  readonly margenes = computed(() => {
    const producto = this.editando();
    if (!producto) {
      return { minorista: 0, mayorista: 0, cantidad: 0 };
    }
    return {
      minorista: this.margen(producto.precioMinorista, producto.costo),
      mayorista: this.margen(producto.precioMayorista, producto.costo),
      cantidad: this.margen(producto.precioCantidad, producto.costo)
    };
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.productos(this.busqueda(), this.soloActivos()).subscribe({
      next: productos => {
        this.productos.set(productos);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudieron cargar los articulos' });
      }
    });
  }

  nuevo(): void {
    this.editando.set({ ...PRODUCTO_NUEVO });
  }

  editar(producto: Producto): void {
    this.editando.set({ ...producto });
  }

  cerrar(): void {
    this.editando.set(null);
  }

  guardar(): void {
    const producto = this.editando();
    if (!producto || !producto.nombre.trim()) {
      this.mensajes.add({ severity: 'warn', summary: 'El nombre es obligatorio' });
      return;
    }

    this.guardando.set(true);
    this.api.guardarProducto(producto).subscribe({
      next: () => {
        this.guardando.set(false);
        this.editando.set(null);
        this.mensajes.add({ severity: 'success', summary: 'Articulo guardado' });
        this.cargar();
      },
      error: respuesta => {
        this.guardando.set(false);
        this.mensajes.add({
          severity: 'error',
          summary: respuesta.error?.mensaje ?? 'No se pudo guardar el articulo'
        });
      }
    });
  }

  confirmarBaja(producto: Producto): void {
    this.confirmacion.confirm({
      header: 'Dar de baja',
      message: `¿Dar de baja "${producto.nombre}"? Los pedidos historicos no se modifican.`,
      acceptLabel: 'Dar de baja',
      rejectLabel: 'Cancelar',
      accept: () => this.darDeBaja(producto)
    });
  }

  actualizarCampo<K extends keyof Producto>(campo: K, valor: Producto[K]): void {
    const producto = this.editando();
    if (producto) {
      this.editando.set({ ...producto, [campo]: valor });
    }
  }

  private darDeBaja(producto: Producto): void {
    this.api.desactivarProducto(producto.id!).subscribe({
      next: () => {
        this.mensajes.add({ severity: 'success', summary: 'Articulo dado de baja' });
        this.cargar();
      },
      error: respuesta => this.mensajes.add({
        severity: 'error',
        summary: respuesta.error?.mensaje ?? 'No se pudo dar de baja'
      })
    });
  }

  private margen(precio: number, costo: number): number {
    if (!precio || precio <= 0) {
      return 0;
    }
    return ((precio - costo) / precio) * 100;
  }
}
