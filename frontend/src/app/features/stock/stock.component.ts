import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { SelectButtonModule } from 'primeng/selectbutton';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/api.service';
import { ImpresionService } from '../../core/impresion.service';
import { MovimientoStock, RankingProducto, StockItem } from '../../core/modelos';
import { LayoutHeaderComponent } from '../../shared/layout-header/layout-header.component';
import { BarraFiltrosComponent } from '../../shared/barra-filtros/barra-filtros.component';

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [
    FormsModule, DecimalPipe, ButtonModule, DialogModule, TableModule, ToggleSwitchModule,
    SelectButtonModule, LayoutHeaderComponent, BarraFiltrosComponent
  ],
  templateUrl: './stock.component.html'
})
export class StockComponent {

  private readonly api = inject(ApiService);
  readonly impresion = inject(ImpresionService);
  private readonly mensajes = inject(MessageService);

  readonly items = signal<StockItem[]>([]);
  readonly cargando = signal(false);
  readonly soloConStock = signal(false);

  readonly detalle = signal<StockItem | null>(null);
  readonly movimientos = signal<MovimientoStock[]>([]);
  readonly ranking = signal<RankingProducto[]>([]);
  readonly diasRanking = signal(30);
  readonly cantidadRanking = signal(5);

  constructor() {
    this.cargar();
    this.cargarRanking();
  }

  cambiarDiasRanking(dias: number): void {
    this.diasRanking.set(dias);
    this.cargarRanking();
  }

  cambiarCantidadRanking(cantidad: number): void {
    this.cantidadRanking.set(cantidad);
    this.cargarRanking();
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

  private cargarRanking(): void {
    this.api.rankingStock(this.diasRanking(), this.cantidadRanking()).subscribe({
      next: ranking => this.ranking.set(ranking),
      error: () => this.mensajes.add({ severity: 'error', summary: 'No se pudo cargar el ranking' })
    });
  }
}
