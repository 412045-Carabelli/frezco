package ar.frezco.pedido.precio;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.CondicionVenta;
import ar.frezco.producto.Producto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/** Pedidos de cliente: precio de la condicion elegida, con el descuento del pedido aplicado. */
@Component
public class PrecioDeVenta implements PoliticaPrecio {

    private static final BigDecimal CIEN = new BigDecimal("100");

    @Override
    public Set<TipoCuenta> tiposSoportados() {
        return Set.of(TipoCuenta.CLIENTE);
    }

    /**
     * El descuento del articulo y el del pedido no son acumulativos: si el articulo tiene
     * descuento propio, ese pisa al general del pedido para esta linea.
     */
    @Override
    public BigDecimal precioUnitario(Producto producto, CondicionVenta condicion,
                                     BigDecimal descuentoPct) {
        BigDecimal base = condicion.precioBase(producto);
        BigDecimal descuentoEfectivo = producto.getDescuentoPct().signum() > 0
                ? producto.getDescuentoPct()
                : descuentoPct;
        BigDecimal factor = BigDecimal.ONE.subtract(descuentoEfectivo.divide(CIEN, 6, RoundingMode.HALF_UP));
        return base.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal descuentoAplicable(BigDecimal descuentoSolicitado) {
        return descuentoSolicitado;
    }
}
