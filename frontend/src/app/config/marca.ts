/**
 * Identidad comercial. Vive en configuracion, no en el dominio: el sistema tiene que poder
 * reutilizarse para otro emprendimiento cambiando solo este archivo.
 *
 * La escala de color alimenta el tema de PrimeNG (ver app.config.ts) y las variables CSS
 * de styles.css. No hardcodear colores en los componentes.
 */
export const MARCA = {
  nombre: 'FrezCo',
  descripcion: 'Stock y pedidos',
  color: '#0891b2',
  escala: {
    50: '#ecfeff',
    100: '#cffafe',
    200: '#a5f3fc',
    300: '#67e8f9',
    400: '#22d3ee',
    500: '#06b6d4',
    600: '#0891b2',
    700: '#0e7490',
    800: '#155e75',
    900: '#164e63',
    950: '#083344'
  }
} as const;
