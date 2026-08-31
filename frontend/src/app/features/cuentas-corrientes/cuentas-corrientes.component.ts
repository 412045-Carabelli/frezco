import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aTexto } from '../../core/fechas';
import { Cuenta, CuentaCorriente, Saldo } from '../../core/modelos';

@Component({
  selector: 'app-cuentas-corrientes',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, ButtonModule, DatePickerModule, DialogModule, InputNumberModule,
    InputTextModule, MessageModule, SelectModule, TableModule
  ],
  templateUrl: './cuentas-corrientes.component.html'
})
export class CuentasCorrientesComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);

  readonly cuentas = signal<Cuenta[]>([]);
  readonly cuentaId = signal<number | null>(null);
  readonly desde = signal<Date | null>(null);
  readonly hasta = signal<Date | null>(null);
  readonly estado = signal<CuentaCorriente | null>(null);
  readonly cargando = signal(false);

  readonly saldosClientes = signal<Saldo[]>([]);
  readonly saldosProveedores = signal<Saldo[]>([]);

  readonly lineasConMes = computed(() =>
    (this.estado()?.lineas ?? []).map(linea => ({ ...linea, mesLabel: this.mesLabelDe(linea.fecha) })));

  /** Cuenta actualmente vista, si admite cancelar deuda (CLIENTE o PROVEEDOR con saldo != 0). */
  readonly puedePagarCobrar = computed(() => {
    const estado = this.estado();
    return !!estado && estado.saldoFinal !== 0
      && (estado.cuenta.tipo === 'CLIENTE' || estado.cuenta.tipo === 'PROVEEDOR');
  });

  readonly dialogoAbierto = signal(false);
  readonly fechaPago = signal<Date>(new Date());
  readonly importePago = signal<number>(0);
  readonly observacionPago = signal('');
  readonly guardandoPago = signal(false);

  constructor() {
    this.api.cuentas(undefined, undefined, true).subscribe(cuentas => this.cuentas.set(cuentas));
    this.cargarSaldos();
  }

  elegirCuenta(cuentaId: number): void {
    this.cuentaId.set(cuentaId);
    this.consultar();
  }

  imprimir(): void {
    window.print();
  }

  consultar(): void {
    const cuentaId = this.cuentaId();
    if (!cuentaId) {
      return;
    }

    this.cargando.set(true);
    this.api.cuentaCorriente(cuentaId, aTexto(this.desde()), aTexto(this.hasta())).subscribe({
      next: estado => {
        this.estado.set(estado);
        this.cargando.set(false);
      },
      error: respuesta => {
        this.cargando.set(false);
        this.mensajes.add({
          severity: 'error',
          summary: respuesta.error?.mensaje ?? 'No se pudo cargar la cuenta corriente'
        });
      }
    });
  }

  abrirPagoCobro(): void {
    const estado = this.estado();
    if (!estado) {
      return;
    }
    this.fechaPago.set(new Date());
    this.importePago.set(Math.abs(estado.saldoFinal));
    this.observacionPago.set('');
    this.dialogoAbierto.set(true);
  }

  cerrarPagoCobro(): void {
    this.dialogoAbierto.set(false);
  }

  confirmarPagoCobro(): void {
    const estado = this.estado();
    if (!estado) {
      return;
    }

    this.guardandoPago.set(true);
    this.api.crearMovimiento({
      id: null,
      fecha: aTexto(this.fechaPago())!,
      tipo: estado.cuenta.tipo === 'CLIENTE' ? 'COBRO' : 'PAGO',
      cuentaId: estado.cuenta.id,
      importe: this.importePago(),
      observacion: this.observacionPago() || null
    }).subscribe({
      next: () => {
        this.guardandoPago.set(false);
        this.dialogoAbierto.set(false);
        this.mensajes.add({ severity: 'success', summary: 'Movimiento registrado' });
        this.consultar();
        this.cargarSaldos();
      },
      error: respuesta => {
        this.guardandoPago.set(false);
        this.mensajes.add({
          severity: 'error',
          summary: respuesta.error?.mensaje ?? 'No se pudo registrar el movimiento'
        });
      }
    });
  }

  subtotalMes(mesLabel: string, campo: 'debe' | 'haber'): number {
    return this.lineasConMes()
      .filter(linea => linea.mesLabel === mesLabel)
      .reduce((suma, linea) => suma + linea[campo], 0);
  }

  private mesLabelDe(fecha: string): string {
    const etiqueta = new Date(fecha + 'T00:00:00')
      .toLocaleDateString('es-AR', { month: 'long', year: 'numeric' });
    return etiqueta.charAt(0).toUpperCase() + etiqueta.slice(1);
  }

  private cargarSaldos(): void {
    this.api.saldos('CLIENTE', true).subscribe(saldos => this.saldosClientes.set(saldos));
    this.api.saldos('PROVEEDOR', false).subscribe(saldos => this.saldosProveedores.set(saldos));
  }
}
