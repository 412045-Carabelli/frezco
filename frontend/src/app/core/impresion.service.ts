import { Injectable, signal } from '@angular/core';

/**
 * Impresion de las pantallas de listado. Mientras se imprime, las tablas se muestran sin
 * paginar para que salga todo el listado y no solo la pagina en pantalla.
 */
@Injectable({ providedIn: 'root' })
export class ImpresionService {

  readonly imprimiendo = signal(false);

  imprimir(): void {
    this.imprimiendo.set(true);
    // El timeout deja que Angular dibuje todas las filas antes de abrir el dialogo.
    setTimeout(() => {
      window.print();
      this.imprimiendo.set(false);
    });
  }
}
