package ar.frezco.pedido;

/**
 * Condicion elegida a mano en cada pedido. No hay umbrales automaticos: CANTIDAD es una
 * condicion mas, igual que MAYORISTA. Ver docs/07-decisiones-pendientes.md D3.
 */
public enum CondicionVenta {
    MINORISTA,
    MAYORISTA,
    CANTIDAD
}
