import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aTexto, primerDiaDelMes } from '../../core/fechas';
import { MARCA } from '../../config/marca';
import { RemitoProveedor } from '../../core/modelos';

@Component({
  selector: 'app-remito-proveedor',
  standalone: true,
  imports: [FormsModule, DecimalPipe, ButtonModule, DatePickerModule],
  templateUrl: './remito-proveedor.component.html',
  styleUrl: './remito.css'
})
export class RemitoProveedorComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);

  readonly marca = MARCA;
  readonly desde = signal<Date>(primerDiaDelMes());
  readonly hasta = signal<Date>(new Date());
  readonly remito = signal<RemitoProveedor | null>(null);
  readonly cargando = signal(false);

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.remitoProveedor(aTexto(this.desde()), aTexto(this.hasta())).subscribe({
      next: remito => {
        this.remito.set(remito);
        this.cargando.set(false);
      },
      error: respuesta => {
        this.cargando.set(false);
        this.mensajes.add({
          severity: 'error',
          summary: respuesta.error?.mensaje ?? 'No se pudo cargar el remito'
        });
      }
    });
  }

  imprimir(): void {
    window.print();
  }
}
