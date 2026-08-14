import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { MovimientoStock, StockItem } from '../../core/modelos';

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [FormsModule, DecimalPipe, ButtonModule, DialogModule, TableModule, ToggleSwitchModule],
  templateUrl: './stock.component.html'
})
export class StockComponent {

  private readonly api = inject(ApiService);
  private readonly mensajes = inject(MessageService);

  readonly items = signal<StockItem[]>([]);
  readonly cargando = signal(false);
  readonly soloConStock = signal(false);

  readonly detalle = signal<StockItem | null>(null);
  readonly movimientos = signal<MovimientoStock[]>([]);

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.stock(this.soloConStock()).subscribe({
      next: items => {
        this.items.set(items);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.mensajes.add({ severity: 'error', summary: 'No se pudo cargar el stock' });
      }
    });
  }

  verMovimientos(item: StockItem): void {
    this.detalle.set(item);
    this.movimientos.set([]);
    this.api.movimientosDeStock(item.productoId).subscribe({
      next: movimientos => this.movimientos.set(movimientos),
      error: () => this.mensajes.add({ severity: 'error', summary: 'No se pudieron cargar los movimientos' })
    });
  }

  cerrarDetalle(): void {
    this.detalle.set(null);
  }
}
