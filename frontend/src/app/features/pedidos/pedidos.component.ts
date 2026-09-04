import { Component, effect, inject, signal, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aTexto } from '../../core/fechas';
import { Cuenta, Pedido } from '../../core/modelos';
import { NuevoPedidoComponent } from './nuevo-pedido.component';
import { TutorialService } from '../../core/tutorial.service';
import { LayoutHeaderComponent } from '../../shared/layout-header/layout-header.component';
import { BarraFiltrosComponent } from '../../shared/barra-filtros/barra-filtros.component';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, RouterLink, AutoCompleteModule, ButtonModule, DatePickerModule,
    DialogModule, TableModule, TagModule, ToggleSwitchModule, ConfirmDialogModule,
    NuevoPedidoComponent, LayoutHeaderComponent, BarraFiltrosComponent
  ],
  providers: [ConfirmationService],
  templateUrl: './pedidos.component.html'
})
export class PedidosComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);
  private readonly confirmacion = inject(ConfirmationService);
  private readonly tutorial = inject(TutorialService);

  @ViewChild('formNuevoPedido') formNuevoPedido?: NuevoPedidoComponent;

  readonly pedidos = signal<Pedido[]>([]);
  readonly cargando = signal(false);
  readonly desde = signal<Date | null>(null);
  readonly hasta = signal<Date | null>(null);
  readonly cuenta = signal<Cuenta | null>(null);
  readonly incluirAnulados = signal(false);
  readonly cuentasSugeridas = signal<Cuenta[]>([]);
  readonly detalle = signal<Pedido | null>(null);
  readonly nuevoPedidoAbierto = signal(false);

  constructor() {
    effect(() => {
      this.desde();
      this.hasta();
      this.cuenta();
      this.incluirAnulados();
      this.cargar();
    });
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.pedidos({
      desde: aTexto(this.desde()),
      hasta: aTexto(this.hasta()),
      cuentaId: this.cuenta()?.id ?? undefined,
      incluirAnulados: this.incluirAnulados()
    }).subscribe({
      next: pedidos => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudieron cargar los pedidos' });
      }
    });
  }

  buscarCuentas(evento: { query: string }): void {
    this.api.cuentas(undefined, evento.query, false)
      .subscribe(cuentas => this.cuentasSugeridas.set(cuentas));
  }

  limpiarCuenta(): void {
    this.cuenta.set(null);
  }

  verDetalle(pedido: Pedido): void {
    this.detalle.set(pedido);
  }

  abrirNuevoPedido(): void {
    this.nuevoPedidoAbierto.set(true);
    this.tutorial.iniciarSiCorresponde('/pedidos/nuevo');
  }

  cerrarNuevoPedido(): void {
    this.nuevoPedidoAbierto.set(false);
  }

  /** Se queda en la pantalla lista para cargar el siguiente pedido; el remito se ve desde Remitos. */
  alGuardarPedido(): void {
    this.cargar();
    this.formNuevoPedido?.reset();
  }

  confirmarAnulacion(pedido: Pedido): void {
    const aviso = pedido.cuenta.tipo === 'REFUERZO'
      ? ' Es un refuerzo: si esa mercaderia ya se vendio, el stock queda en negativo.'
      : '';

    this.confirmacion.confirm({
      header: 'Anular pedido',
      message: `¿Anular el pedido ${pedido.numero}?${aviso}`,
      acceptLabel: 'Anular',
      rejectLabel: 'Cancelar',
      accept: () => this.anular(pedido)
    });
  }

  private anular(pedido: Pedido): void {
    this.api.anularPedido(pedido.id).subscribe({
      next: () => {
        this.mensajes.add({ severity: 'success', summary: `Pedido ${pedido.numero} anulado` });
        this.cargar();
      },
      error: respuesta => this.mensajes.add({
        severity: 'error',
        summary: respuesta.error?.mensaje ?? 'No se pudo anular'
      })
    });
  }
}
