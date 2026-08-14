import { Component, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { MARCA } from '../../config/marca';
import { RemitoCliente } from '../../core/modelos';

@Component({
  selector: 'app-remito-cliente',
  standalone: true,
  imports: [DecimalPipe, ButtonModule],
  templateUrl: './remito-cliente.component.html',
  styleUrl: './remito.css'
})
export class RemitoClienteComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);

  /** Llega por withComponentInputBinding desde la ruta /remitos/cliente/:pedidoId */
  readonly pedidoId = input.required<string>();

  readonly marca = MARCA;
  readonly remito = signal<RemitoCliente | null>(null);

  constructor() {
    queueMicrotask(() => this.cargar());
  }

  imprimir(): void {
    window.print();
  }

  private cargar(): void {
    this.api.remitoCliente(Number(this.pedidoId())).subscribe({
      next: remito => this.remito.set(remito),
      error: respuesta => this.mensajes.add({
        severity: 'error',
        summary: respuesta.error?.mensaje ?? 'No se pudo cargar el remito'
      })
    });
  }
}
