package ar.frezco.pedido;

import ar.frezco.producto.Producto;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Condicion elegida a mano en cada pedido. No hay umbrales automaticos: CANTIDAD es una
 * condicion mas, igual que MAYORISTA. Ver docs/07-decisiones-pendientes.md D3.
 *
 * <p>Cada constante sabe de que columna del producto sale su precio: es la variante mas
 * chica del patron Strategy y evita el switch repetido en los servicios.
 */
public enum CondicionVenta {

    MINORISTA(Producto::getPrecioMinorista),
    MAYORISTA(Producto::getPrecioMayorista),
    CANTIDAD(Producto::getPrecioCantidad);

    private final Function<Producto, BigDecimal> precio;

    CondicionVenta(Function<Producto, BigDecimal> precio) {
        this.precio = precio;
    }

    public BigDecimal precioBase(Producto producto) {
        BigDecimal valor = precio.apply(producto);
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
