import { Component, Input } from '@angular/core';

type ColorKpi = 'marca' | 'emerald' | 'amber' | 'rose' | 'gray';

/** Paleta de indicadores: mismo patron (borde + texto + fondo tintado) que el Sistema de Gestion de Obras. */
const COLORES: Record<ColorKpi, { borde: string; texto: string; fondo: string }> = {
  marca: { borde: 'var(--marca)', texto: 'var(--marca-texto)', fondo: 'var(--marca-50)' },
  emerald: { borde: '#059669', texto: '#047857', fondo: '#ecfdf5' },
  amber: { borde: '#d97706', texto: '#b45309', fondo: '#fffbeb' },
  rose: { borde: '#e11d48', texto: '#be123c', fondo: '#fff1f2' },
  gray: { borde: '#6b7280', texto: '#374151', fondo: '#f9fafb' }
};

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  templateUrl: './kpi-card.component.html'
})
export class KpiCardComponent {
  @Input() titulo = '';
  @Input() valor = '';
  @Input() descripcion = '';
  @Input() icono = '';
  @Input() color: ColorKpi = 'marca';
  @Input() tintado = false;

  get colores() {
    return COLORES[this.color];
  }
}
