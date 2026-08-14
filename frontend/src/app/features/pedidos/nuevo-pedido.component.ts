import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectButtonModule } from 'primeng/selectbutton';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aTexto } from '../../core/fechas';
import { CondicionVenta, Cuenta, Producto } from '../../core/modelos';

interface LineaEnEdicion {
  producto: Producto | null;
  unidades: number;
  precioUnitario: number;
  stockDisponible: number;
}

@Component({
  selector: 'app-nuevo-pedido',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, AutoCompleteModule, ButtonModule, DatePickerModule,
    InputNumberModule, InputTextModule, SelectButtonModule
  ],
  templateUrl: './nuevo-pedido.component.html'
})
export class NuevoPedidoComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);
  private readonly router = inject(Router);

  readonly fecha = signal<Date>(new Date());
  readonly cuenta = signal<Cuenta | null>(null);
  readonly condicion = signal<CondicionVenta>('MINORISTA');
  readonly descuentoPct = signal(0);
  readonly observacion = signal('');
  readonly lineas = signal<LineaEnEdicion[]>([]);
  readonly guardando = signal(false);

  readonly cuentasSugeridas = signal<Cuenta[]>([]);
  readonly productosSugeridos = signal<Producto[]>([]);

  readonly condiciones: { label: string; value: CondicionVenta }[] = [
    { label: 'Minorista', value: 'MINORISTA' },
    { label: 'Mayorista', value: 'MAYORISTA' },
    { label: 'Por cantidad', value: 'CANTIDAD' }
  ];

  /** Refuerzo y consumo no tienen precio de venta: se ocultan condicion y descuento. */
  readonly esVenta = computed(() => this.cuenta()?.tipo === 'CLIENTE' || this.cuenta() === null);

  readonly total = computed(() =>
    this.lineas().reduce((suma, linea) => suma + linea.unidades * linea.precioUnitario, 0));

  buscarCuentas(evento: { query: string }): void {
    this.api.cuentas(undefined, evento.query, true)
      .subscribe(cuentas => this.cuentasSugeridas.set(cuentas.filter(c => c.tipo !== 'PROVEEDOR')));
  }

  buscarProductos(evento: { query: string }): void {
    this.api.productos(evento.query, true).subscribe(productos => this.productosSugeridos.set(productos));
  }

  elegirCuenta(cuenta: Cuenta): void {
    this.cuenta.set(cuenta);
    this.descuentoPct.set(cuenta.tipo === 'CLIENTE' ? cuenta.descuentoPct : 0);
    this.recalcularPrecios();
  }

  cambiarCondicion(condicion: CondicionVenta): void {
    this.condicion.set(condicion);
    this.recalcularPrecios();
  }

  cambiarDescuento(descuento: number): void {
    this.descuentoPct.set(descuento ?? 0);
    this.recalcularPrecios();
  }

  agregarLinea(): void {
    this.lineas.update(lineas => [
      ...lineas,
      { producto: null, unidades: 1, precioUnitario: 0, stockDisponible: 0 }
    ]);
  }

  quitarLinea(indice: number): void {
    this.lineas.update(lineas => lineas.filter((_, i) => i !== indice));
  }

  elegirProducto(indice: number, producto: Producto): void {
    this.actualizarLinea(indice, { producto });
    this.consultarPrecio(indice, producto);
  }

  cambiarUnidades(indice: number, unidades: number): void {
    this.actualizarLinea(indice, { unidades: unidades ?? 0 });
  }

  /** Aviso informativo: el reparto real lo hace el backend al guardar. */
  sePideAlProveedor(linea: LineaEnEdicion): boolean {
    return !!linea.producto && linea.unidades > linea.stockDisponible;
  }

  guardar(): void {
    const cuenta = this.cuenta();
    const lineasCargadas = this.lineas().filter(linea => linea.producto && linea.unidades > 0);

    if (!cuenta) {
      this.mensajes.add({ severity: 'warn', summary: 'Elegi una cuenta' });
      return;
    }
    if (lineasCargadas.length === 0) {
      this.mensajes.add({ severity: 'warn', summary: 'Agrega al menos un articulo' });
      return;
    }

    this.guardando.set(true);
    this.api.crearPedido({
      fecha: aTexto(this.fecha())!,
      cuentaId: cuenta.id!,
      condicion: this.condicion(),
      descuentoPct: this.esVenta() ? this.descuentoPct() : 0,
      observacion: this.observacion() || null,
      lineas: lineasCargadas.map(linea => ({
        productoId: linea.producto!.id!,
        unidades: linea.unidades
      }))
    }).subscribe({
      next: pedido => {
        this.guardando.set(false);
        this.mensajes.add({ severity: 'success', summary: `Pedido ${pedido.numero} guardado` });
        // El flujo natural es cargar la venta y mandar el remito.
        if (pedido.cuenta.tipo === 'CLIENTE') {
          this.router.navigate(['/remitos/cliente', pedido.id]);
        } else {
          this.router.navigate(['/pedidos']);
        }
      },
      error: respuesta => {
        this.guardando.set(false);
        this.mensajes.add({
          severity: 'error',
          summary: respuesta.error?.mensaje ?? 'No se pudo guardar el pedido'
        });
      }
    });
  }

  private recalcularPrecios(): void {
    this.lineas().forEach((linea, indice) => {
      if (linea.producto) {
        this.consultarPrecio(indice, linea.producto);
      }
    });
  }

  private consultarPrecio(indice: number, producto: Producto): void {
    this.api.precio(producto.id!, this.cuenta()?.id ?? null, this.condicion(), this.descuentoPct())
      .subscribe({
        next: preview => this.actualizarLinea(indice, {
          precioUnitario: preview.precioUnitario,
          stockDisponible: preview.stockDisponible
        }),
        error: () => this.mensajes.add({ severity: 'error', summary: 'No se pudo obtener el precio' })
      });
  }

  private actualizarLinea(indice: number, cambios: Partial<LineaEnEdicion>): void {
    this.lineas.update(lineas =>
      lineas.map((linea, i) => (i === indice ? { ...linea, ...cambios } : linea)));
  }
}
