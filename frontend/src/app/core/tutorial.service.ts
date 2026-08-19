import { Injectable, signal } from '@angular/core';
import { driver } from 'driver.js';
import { pasosDe } from '../shared/tutorial/pasos';

const CLAVE_AUTOMATICO = 'frezco.tutorial.automatico';
const CLAVE_VISTAS = 'frezco.tutorial.vistas';

/**
 * Tutorial guiado por pantalla, sobre driver.js. Los textos viven en pasos.ts; aca solo esta
 * cuando se abre y la preferencia de la usuaria.
 *
 * La preferencia queda en el navegador: es una ayuda de pantalla, no un dato del negocio, y
 * no justifica una tabla.
 */
@Injectable({ providedIn: 'root' })
export class TutorialService {

  /** Si esta activo, cada pantalla muestra su ayuda la primera vez que se entra. */
  readonly automatico = signal(this.leerAutomatico());

  /** true si la pantalla tiene ayuda escrita: el boton se oculta cuando no hay. */
  hayAyudaPara(ruta: string): boolean {
    return pasosDe(ruta).length > 0;
  }

  iniciar(ruta: string): void {
    const pasos = pasosDe(ruta);
    if (pasos.length === 0) {
      return;
    }

    driver({
      showProgress: true,
      allowClose: true,
      overlayColor: '#111827',
      overlayOpacity: 0.55,
      nextBtnText: 'Siguiente',
      prevBtnText: 'Anterior',
      doneBtnText: 'Listo',
      progressText: 'Paso {{current}} de {{total}}',
      popoverClass: 'tutorial-frezco',
      steps: pasos.map(paso => ({
        element: this.elementoVisible(paso.selector),
        popover: { title: paso.titulo, description: paso.texto }
      }))
    }).drive();

    this.marcarVista(ruta);
  }

  /**
   * Devuelve el selector solo si el elemento existe y se ve en este tamano de pantalla. El
   * menu lateral, por ejemplo, esta oculto en el celular: ahi el paso se muestra centrado en
   * vez de senalar un elemento invisible.
   */
  private elementoVisible(selector?: string): string | undefined {
    if (!selector) {
      return undefined;
    }
    const elemento = document.querySelector(selector);
    const rect = elemento?.getBoundingClientRect();
    return rect && rect.width > 0 && rect.height > 0 ? selector : undefined;
  }

  /** Se llama al entrar a cada pantalla. Solo abre si la ayuda automatica esta activada. */
  iniciarSiCorresponde(ruta: string): void {
    if (this.automatico() && !this.yaVista(ruta)) {
      this.iniciar(ruta);
    }
  }

  alternarAutomatico(activo: boolean): void {
    this.automatico.set(activo);
    localStorage.setItem(CLAVE_AUTOMATICO, String(activo));
    // Al reactivarla, la ayuda vuelve a aparecer en todas las pantallas.
    if (activo) {
      localStorage.removeItem(CLAVE_VISTAS);
    }
  }

  private leerAutomatico(): boolean {
    // Por defecto encendida: la primera vez conviene que aparezca sola.
    return localStorage.getItem(CLAVE_AUTOMATICO) !== 'false';
  }

  private yaVista(ruta: string): boolean {
    return this.vistas().includes(ruta);
  }

  private marcarVista(ruta: string): void {
    const vistas = this.vistas();
    if (!vistas.includes(ruta)) {
      localStorage.setItem(CLAVE_VISTAS, JSON.stringify([...vistas, ruta]));
    }
  }

  private vistas(): string[] {
    try {
      return JSON.parse(localStorage.getItem(CLAVE_VISTAS) ?? '[]') as string[];
    } catch {
      return [];
    }
  }
}
