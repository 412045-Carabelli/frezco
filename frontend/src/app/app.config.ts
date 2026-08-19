import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { registerLocaleData } from '@angular/common';
import localeEsAr from '@angular/common/locales/es-AR';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';
import { definePreset } from '@primeng/themes';
import { MessageService } from 'primeng/api';

import { routes } from './app.routes';
import { credencialesInterceptor } from './core/credenciales.interceptor';
import { MARCA } from './config/marca';

registerLocaleData(localeEsAr);

/** Aura con el color de la marca como primario: botones, foco y estados activos. */
const temaFrezco = definePreset(Aura, {
  semantic: {
    primary: MARCA.escala
  }
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([credencialesInterceptor])),
    provideAnimationsAsync(),
    providePrimeNG({
      theme: {
        preset: temaFrezco,
        options: {
          prefix: 'p',
          darkModeSelector: 'light-theme',
          cssLayer: false
        }
      }
    }),
    MessageService,
    { provide: LOCALE_ID, useValue: 'es-AR' }
  ]
};
