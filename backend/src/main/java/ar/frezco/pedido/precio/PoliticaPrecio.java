package ar.frezco.pedido.precio;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.CondicionVenta;
import ar.frezco.producto.Producto;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Strategy: con que precio se carga una linea. Un pedido de cliente se valoriza al precio de
 * venta con descuento; un refuerzo o un consumo propio se valorizan al costo, porque no son
 * ventas. Ver docs/03-reglas-negocio.md punto 1.
 */
public interface PoliticaPrecio {

    Set<TipoCuenta> tiposSoportados();

    BigDecimal precioUnitario(Producto producto, CondicionVenta condicion, BigDecimal descuentoPct);

    /** El descuento del pedido no aplica a las cuentas que se valorizan al costo. */
    BigDecimal descuentoAplicable(BigDecimal descuentoSolicitado);
}
