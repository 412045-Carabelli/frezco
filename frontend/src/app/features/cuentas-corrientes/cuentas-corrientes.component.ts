import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
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
    FormsModule, DecimalPipe, ButtonModule, DatePickerModule, MessageModule, SelectModule, TableModule
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
  readonly saldoProveedor = signal<Saldo | null>(null);

  constructor() {
    this.api.cuentas(undefined, undefined, true).subscribe(cuentas => this.cuentas.set(cuentas));
    this.cargarSaldos();
  }

  elegirCuenta(cuentaId: number): void {
    this.cuentaId.set(cuentaId);
    this.consultar();
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

  private cargarSaldos(): void {
    this.api.saldos('CLIENTE', true).subscribe(saldos => this.saldosClientes.set(saldos));
    this.api.saldos('PROVEEDOR', false)
      .subscribe(saldos => this.saldoProveedor.set(saldos[0] ?? null));
  }
}
