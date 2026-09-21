import { Component, OnInit, model } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';

/** Subir en cada release: es la clave con la que se recuerda que la usuaria ya vio las novedades. */
export const VERSION = 'v0.2.0';
const CLAVE_VISTA = `frezco-novedades-vistas-${VERSION}`;
const URL_TICKETS = 'https://soporte.buildrr.cloud/tickets';

interface Cambio {
  ticket: number;
  texto: string;
}

interface GrupoDeCambios {
  titulo: string;
  cambios: Cambio[];
}

@Component({
  selector: 'app-novedades',
  standalone: true,
  imports: [ButtonModule, DialogModule],
  templateUrl: './novedades.component.html'
})
export class NovedadesComponent implements OnInit {

  readonly visible = model(false);
  readonly version = VERSION;
  readonly urlTickets = URL_TICKETS;

  /** El grupo mas nuevo va primero. */
  readonly grupos: GrupoDeCambios[] = [
    {
      titulo: 'Tickets Pablo 21/09/2026',
      cambios: [
        { ticket: 5, texto: 'Cobros y pagos: las deudas pendientes se muestran en una grilla alineada y los importes no se cortan en dos renglones' },
        { ticket: 10007, texto: 'Cobros y pagos: las deudas pendientes separan Clientes de Proveedores, cada grupo ordenado alfabéticamente' },
        { ticket: 10008, texto: 'Análisis comercial: cada fila de los rankings por producto, cliente y zona tiene una barra proporcional al importe' },
        { ticket: 10011, texto: 'Stock: el ranking de ventas permite elegir cuántos artículos analizar (5, 10, 20 o 50)' },
        { ticket: 10012, texto: 'Cuentas corrientes: la pantalla general muestra el total de clientes y el total de proveedores' },
        { ticket: 10013, texto: 'Todas las pantallas de listado tienen botón Imprimir; las tablas se imprimen completas, sin menú ni botones' },
        { ticket: 10014, texto: 'Artículos: el signo $ ya no queda separado del importe cuando la pantalla está al 90%' }
      ]
    }
  ];

  /** La primera vez que se abre una version nueva, las novedades aparecen solas. */
  ngOnInit(): void {
    try {
      if (!localStorage.getItem(CLAVE_VISTA)) {
        this.visible.set(true);
      }
    } catch {
      // Sin almacenamiento local (modo privado): no se muestran solas.
    }
  }

  cerrar(): void {
    try {
      localStorage.setItem(CLAVE_VISTA, '1');
    } catch {
      // Se vuelve a mostrar en la proxima visita; no es grave.
    }
    this.visible.set(false);
  }
}
