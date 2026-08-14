import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { Cuenta, TipoCuenta } from '../../core/modelos';

const CUENTA_NUEVA: Cuenta = {
  id: null, nombre: '', tipo: 'CLIENTE', zona: null, descuentoPct: 0, activo: true
};

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, ButtonModule, DialogModule, InputNumberModule, InputTextModule,
    SelectModule, TableModule, TagModule, ConfirmDialogModule
  ],
  providers: [ConfirmationService],
  templateUrl: './cuentas.component.html'
})
export class CuentasComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);
  private readonly confirmacion = inject(ConfirmationService);

  readonly cuentas = signal<Cuenta[]>([]);
  readonly cargando = signal(false);
  readonly busqueda = signal('');
  readonly editando = signal<Cuenta | null>(null);
  readonly guardando = signal(false);

  readonly tipos: { label: string; value: TipoCuenta }[] = [
    { label: 'Cliente', value: 'CLIENTE' },
    { label: 'Proveedor', value: 'PROVEEDOR' },
    { label: 'Refuerzo de stock', value: 'REFUERZO' },
    { label: 'Consumo propio', value: 'CONSUMO' }
  ];

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.cuentas(undefined, this.busqueda(), false).subscribe({
      next: cuentas => {
        this.cuentas.set(cuentas);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudieron cargar las cuentas' });
      }
    });
  }

  nueva(): void {
    this.editando.set({ ...CUENTA_NUEVA });
  }

  editar(cuenta: Cuenta): void {
    this.editando.set({ ...cuenta });
  }

  cerrar(): void {
    this.editando.set(null);
  }

  actualizarCampo<K extends keyof Cuenta>(campo: K, valor: Cuenta[K]): void {
    const cuenta = this.editando();
    if (cuenta) {
      this.editando.set({ ...cuenta, [campo]: valor });
    }
  }

  guardar(): void {
    const cuenta = this.editando();
    if (!cuenta || !cuenta.nombre.trim()) {
      this.mensajes.add({ severity: 'warn', summary: 'El nombre es obligatorio' });
      return;
    }

    this.guardando.set(true);
    this.api.guardarCuenta(cuenta).subscribe({
      next: () => {
        this.guardando.set(false);
        this.editando.set(null);
        this.mensajes.add({ severity: 'success', summary: 'Cuenta guardada' });
        this.cargar();
      },
      error: respuesta => {
        this.guardando.set(false);
        this.mensajes.add({
          severity: 'error',
          summary: respuesta.error?.mensaje ?? 'No se pudo guardar la cuenta'
        });
      }
    });
  }

  confirmarBaja(cuenta: Cuenta): void {
    this.confirmacion.confirm({
      header: 'Dar de baja',
      message: `¿Dar de baja la cuenta "${cuenta.nombre}"?`,
      acceptLabel: 'Dar de baja',
      rejectLabel: 'Cancelar',
      accept: () => this.darDeBaja(cuenta)
    });
  }

  /** Las cuentas del sistema no se editan como tipo ni se dan de baja. */
  esEspecial(cuenta: Cuenta): boolean {
    return cuenta.tipo !== 'CLIENTE';
  }

  etiquetaTipo(tipo: TipoCuenta): string {
    return this.tipos.find(opcion => opcion.value === tipo)?.label ?? tipo;
  }

  private darDeBaja(cuenta: Cuenta): void {
    this.api.desactivarCuenta(cuenta.id!).subscribe({
      next: () => {
        this.mensajes.add({ severity: 'success', summary: 'Cuenta dada de baja' });
        this.cargar();
      },
      error: respuesta => this.mensajes.add({
        severity: 'error',
        summary: respuesta.error?.mensaje ?? 'No se pudo dar de baja'
      })
    });
  }
}
