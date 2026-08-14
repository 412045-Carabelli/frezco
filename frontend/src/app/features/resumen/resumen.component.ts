import { Component, inject } from '@angular/core';
import { SesionService } from '../../core/sesion.service';

/**
 * Placeholder. Los indicadores del periodo se implementan en la Etapa 6.
 * Ver docs/05-pantallas.md punto 10.
 */
@Component({
  selector: 'app-resumen',
  standalone: true,
  template: `
    <h1 class="text-xl font-semibold text-gray-800">Resumen</h1>
    <p class="text-sm text-gray-500 mt-1">Sesion iniciada como {{ sesion.usuario() }}.</p>
    <p class="text-sm text-gray-500 mt-4">
      Los indicadores del periodo se cargan en la Etapa 6 del plan.
    </p>
  `
})
export class ResumenComponent {
  readonly sesion = inject(SesionService);
}
