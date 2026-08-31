/**
 * Otros sistemas del mismo ecosistema (mismo VPS, mismo Dokploy), a los que se puede saltar
 * desde el menu. Cada uno vive en su propio subdominio: no hay integracion mas alla del link.
 *
 * Agregar un sistema nuevo es agregar una entrada aca, nada de codigo.
 */
export interface SistemaDelEcosistema {
  nombre: string;
  url: string;
  icono: string;
}

export const ECOSISTEMA: SistemaDelEcosistema[] = [];
