import { Component } from '@angular/core';

/**
 * Tarjeta de filtros: mismo criterio que el Sistema de Gestion de Obras (etiquetas chicas en
 * mayuscula, campos a la izquierda, acciones a la derecha). El contenido de cada pantalla se
 * proyecta con los selectores [filtros] y [acciones].
 */
@Component({
  selector: 'app-barra-filtros',
  standalone: true,
  templateUrl: './barra-filtros.component.html',
  styleUrl: './barra-filtros.component.css'
})
export class BarraFiltrosComponent {
}
