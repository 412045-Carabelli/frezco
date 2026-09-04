import { Component, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { TableModule } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aTexto } from '../../core/fechas';
import { Cuenta, Pedido } from '../../core/modelos';
import { BarraFiltrosComponent } from '../../shared/barra-filtros/barra-filtros.component';

/** Listado de remitos de cliente: un pedido de venta = un remito. */
@Component({
  selector: 'app-remitos-clientes',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, RouterLink, AutoCompleteModule, ButtonModule, DatePickerModule,
    TableModule, BarraFiltrosComponent
  ],
  templateUrl: './remitos-clientes.component.html'
})
export class RemitosClientesComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);

  readonly pedidos = signal<Pedido[]>([]);
  readonly cargando = signal(false);
  readonly desde = signal<Date | null>(null);
  readonly hasta = signal<Date | null>(null);
  readonly cuenta = signal<Cuenta | null>(null);
  readonly cuentasSugeridas = signal<Cuenta[]>([]);

  constructor() {
    effect(() => {
      this.desde();
      this.hasta();
      this.cuenta();
      this.cargar();
    });
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.pedidos({
      desde: aTexto(this.desde()),
      hasta: aTexto(this.hasta()),
      cuentaId: this.cuenta()?.id ?? undefined,
      incluirAnulados: false
    }).subscribe({
      next: pedidos => {
        this.pedidos.set(pedidos.filter(p => p.cuenta.tipo === 'CLIENTE'));
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudieron cargar los remitos' });
      }
    });
  }

  buscarCuentas(evento: { query: string }): void {
    this.api.cuentas('CLIENTE', evento.query, false)
      .subscribe(cuentas => this.cuentasSugeridas.set(cuentas));
  }

  limpiarCuenta(): void {
    this.cuenta.set(null);
  }
}
