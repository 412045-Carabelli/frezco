import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, PercentPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { aTexto } from '../../core/fechas';
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

  /** Sin fechas, el backend calcula sobre todos los pedidos, sin filtrar por periodo. */
  cargar(): void {
    this.cargando.set(true);
    this.api.resumen(aTexto(this.desde()), aTexto(this.hasta())).subscribe({
      next: resumen => {
        this.resumen.set(resumen);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudo cargar el resumen' });
      }
    });
  }
}
