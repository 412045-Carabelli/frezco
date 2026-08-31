import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aTexto } from '../../core/fechas';
import { Cuenta, Movimiento, Saldo, TipoMovimiento } from '../../core/modelos';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, AutoCompleteModule, ButtonModule, DatePickerModule, InputNumberModule,
    InputTextModule, SelectButtonModule, TableModule, TagModule, ConfirmDialogModule
  ],
  providers: [ConfirmationService],
  templateUrl: './movimientos.component.html'
})
export class MovimientosComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);
  private readonly confirmacion = inject(ConfirmationService);

  readonly movimientos = signal<Movimiento[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal(false);

  readonly desde = signal<Date | null>(null);
  readonly hasta = signal<Date | null>(null);

  readonly fecha = signal<Date>(new Date());
  readonly tipo = signal<TipoMovimiento>('COBRO');
  readonly cuenta = signal<Cuenta | null>(null);
  readonly importe = signal<number>(0);
  readonly observacion = signal('');
  readonly saldoCuenta = signal<number | null>(null);

  readonly cuentasSugeridas = signal<Cuenta[]>([]);
  readonly deudas = signal<Saldo[]>([]);
  readonly deudasProveedores = signal<Saldo[]>([]);
  readonly tipos: { label: string; value: TipoMovimiento }[] = [
    { label: 'Cobro', value: 'COBRO' },
    { label: 'Pago', value: 'PAGO' }
  ];

  /** Cobro va con clientes, pago con el proveedor: el backend rechaza el resto. */
  readonly cuentaValida = computed(() => {
    const cuenta = this.cuenta();
    if (!cuenta) {
      return true;
    }
    return this.tipo() === 'COBRO' ? cuenta.tipo === 'CLIENTE' : cuenta.tipo === 'PROVEEDOR';
  });

  /** No se puede cobrar ni pagar mas de lo que la cuenta debe actualmente. */
  readonly importeValido = computed(() => {
    const saldo = this.saldoCuenta();
    return saldo === null || this.importe() <= saldo;
  });

  constructor() {
    effect(() => {
      this.desde();
      this.hasta();
      this.cargar();
    });
    this.cargarDeudas();
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.movimientos({ desde: aTexto(this.desde()), hasta: aTexto(this.hasta()) }).subscribe({
      next: movimientos => {
        this.movimientos.set(movimientos);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudieron cargar los movimientos' });
      }
    });
  }

  buscarCuentas(evento: { query: string }): void {
    const tipo = this.tipo() === 'COBRO' ? 'CLIENTE' : 'PROVEEDOR';
    this.api.cuentas(tipo, evento.query, true)
      .subscribe(cuentas => this.cuentasSugeridas.set(cuentas));
  }

  /** Mostrar el saldo al elegir la cuenta: saber cuanto deben antes de cargar el cobro. */
  elegirCuenta(cuenta: Cuenta): void {
    this.cuenta.set(cuenta);
    this.saldoCuenta.set(null);
    this.api.cuentaCorriente(cuenta.id!).subscribe({
      next: estado => this.saldoCuenta.set(estado.saldoFinal),
      error: () => this.saldoCuenta.set(null)
    });
  }

  /** Deudas pendientes: clientes que deben y proveedores a los que se les debe, para pagar/cobrar en un click. */
  cargarDeudas(): void {
    this.api.saldos('CLIENTE', true).subscribe(saldos =>
      this.deudas.set(saldos.filter(s => s.saldo > 0)));
    this.api.saldos('PROVEEDOR', true).subscribe(saldos =>
      this.deudasProveedores.set(saldos.filter(s => s.saldo > 0)));
  }

  /** Precarga el formulario con la cuenta y el saldo total, lista para confirmar. */
  cobrarDeuda(saldo: Saldo): void {
    this.tipo.set('COBRO');
    this.elegirCuenta({ id: saldo.cuentaId, nombre: saldo.nombre, tipo: 'CLIENTE',
      zona: null, descuentoPct: 0, activo: true });
    this.importe.set(saldo.saldo);
  }

  pagarDeuda(saldo: Saldo): void {
    this.tipo.set('PAGO');
    this.elegirCuenta({ id: saldo.cuentaId, nombre: saldo.nombre, tipo: 'PROVEEDOR',
      zona: null, descuentoPct: 0, activo: true });
    this.importe.set(saldo.saldo);
  }

  cambiarTipo(tipo: TipoMovimiento): void {
    this.tipo.set(tipo);
    this.cuenta.set(null);
    this.saldoCuenta.set(null);
  }

  guardar(): void {
    const cuenta = this.cuenta();
    if (!cuenta) {
      this.mensajes.add({ severity: 'warn', summary: 'Elegi una cuenta' });
      return;
    }
    if (!this.importe() || this.importe() <= 0) {
      this.mensajes.add({ severity: 'warn', summary: 'El importe tiene que ser mayor a cero' });
      return;
    }
    if (!this.importeValido()) {
      this.mensajes.add({ severity: 'warn', summary: 'El importe no puede superar la deuda actual' });
      return;
    }

    this.guardando.set(true);
    this.api.crearMovimiento({
      id: null,
      fecha: aTexto(this.fecha())!,
      tipo: this.tipo(),
      cuentaId: cuenta.id!,
      importe: this.importe(),
      observacion: this.observacion() || null
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.mensajes.add({ severity: 'success', summary: 'Movimiento registrado' });
        this.limpiar();
        this.cargar();
        this.cargarDeudas();
      },
      error: respuesta => {
        this.guardando.set(false);
        this.mensajes.add({
          severity: 'error',
          summary: respuesta.error?.mensaje ?? 'No se pudo registrar el movimiento'
        });
      }
    });
  }

  confirmarBorrado(movimiento: Movimiento): void {
    this.confirmacion.confirm({
      header: 'Borrar movimiento',
      message: `¿Borrar el ${movimiento.tipo.toLowerCase()} de ${movimiento.cuentaNombre}?`,
      acceptLabel: 'Borrar',
      rejectLabel: 'Cancelar',
      accept: () => this.borrar(movimiento)
    });
  }

  private borrar(movimiento: Movimiento): void {
    this.api.borrarMovimiento(movimiento.id!).subscribe({
      next: () => {
        this.mensajes.add({ severity: 'success', summary: 'Movimiento borrado' });
        this.cargar();
        this.cargarDeudas();
      },
      error: () => this.mensajes.add({ severity: 'error', summary: 'No se pudo borrar' })
    });
  }

  private limpiar(): void {
    this.importe.set(0);
    this.observacion.set('');
    this.cuenta.set(null);
    this.saldoCuenta.set(null);
  }
}
