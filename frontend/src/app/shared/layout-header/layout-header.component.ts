import { Component, Input, inject } from '@angular/core';
import { Location, NgClass } from '@angular/common';
import { ButtonModule } from 'primeng/button';

type TipoMembrete = 'listado' | 'detalle' | 'alta' | 'edicion';

/** Un degradado por tipo de pantalla, mismo patron que el Sistema de Gestion de Obras. */
const ESTILOS: Record<TipoMembrete, { fondo: string; subtitulo: string }> = {
  listado: { fondo: 'linear-gradient(135deg, var(--marca) 0%, var(--marca-oscuro) 100%)', subtitulo: 'text-cyan-100' },
  detalle: { fondo: 'linear-gradient(135deg, #f59e0b 0%, #b45309 100%)', subtitulo: 'text-amber-100' },
  alta: { fondo: 'linear-gradient(135deg, #16a34a 0%, #15803d 100%)', subtitulo: 'text-green-100' },
  edicion: { fondo: 'linear-gradient(135deg, #dc2626 0%, #b91c1c 100%)', subtitulo: 'text-red-100' }
};

@Component({
  selector: 'app-layout-header',
  standalone: true,
  imports: [NgClass, ButtonModule],
  templateUrl: './layout-header.component.html'
})
export class LayoutHeaderComponent {
  private readonly ubicacion = inject(Location);

  @Input() titulo = '';
  @Input() subtitulo = '';
  @Input() tipo: TipoMembrete = 'listado';
  /** La pantalla de inicio no tiene "atras" util. */
  @Input() mostrarVolver = true;

  get estilo() {
    return ESTILOS[this.tipo];
  }

  volver(): void {
    this.ubicacion.back();
  }
}
