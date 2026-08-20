import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { TableModule } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aFecha, aTexto } from '../../core/fechas';
import { AnalisisComercial } from '../../core/modelos';

@Component({
  selector: 'app-analisis-comercial',
  standalone: true,
  imports: [FormsModule, DecimalPipe, ButtonModule, DatePickerModule, TableModule],
  templateUrl: './analisis-comercial.component.html'
})
export class AnalisisComercialComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);

  readonly analisis = signal<AnalisisComercial | null>(null);
  readonly cargando = signal(false);
  readonly desde = signal<Date | null>(null);
  readonly hasta = signal<Date | null>(null);

  constructor() {
    this.cargar();
  }

  /** Sin fechas, el backend devuelve el mes en curso y de ahi salen los filtros. */
  cargar(): void {
    this.cargando.set(true);
    this.api.analisisComercial(aTexto(this.desde()), aTexto(this.hasta())).subscribe({
      next: analisis => {
        this.analisis.set(analisis);
        if (!this.desde()) {
          this.desde.set(aFecha(analisis.desde));
          this.hasta.set(aFecha(analisis.hasta));
        }
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudo cargar el analisis' });
      }
    });
  }
}
