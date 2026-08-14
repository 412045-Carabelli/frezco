import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, PercentPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aFecha, aTexto } from '../../core/fechas';
import { Resumen } from '../../core/modelos';

@Component({
  selector: 'app-resumen',
  standalone: true,
  imports: [FormsModule, DecimalPipe, PercentPipe, ButtonModule, DatePickerModule],
  templateUrl: './resumen.component.html'
})
export class ResumenComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);

  readonly resumen = signal<Resumen | null>(null);
  readonly cargando = signal(false);
  readonly desde = signal<Date | null>(null);
  readonly hasta = signal<Date | null>(null);

  constructor() {
    this.cargar();
  }

  /** Sin fechas, el backend devuelve el mes en curso y de ahi salen los filtros. */
  cargar(): void {
    this.cargando.set(true);
    this.api.resumen(aTexto(this.desde()), aTexto(this.hasta())).subscribe({
      next: resumen => {
        this.resumen.set(resumen);
        if (!this.desde()) {
          this.desde.set(aFecha(resumen.desde));
          this.hasta.set(aFecha(resumen.hasta));
        }
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudo cargar el resumen' });
      }
    });
  }
}
