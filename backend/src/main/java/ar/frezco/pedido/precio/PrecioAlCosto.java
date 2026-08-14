package ar.frezco.pedido.precio;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.CondicionVenta;
import ar.frezco.producto.Producto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Refuerzo de stock y consumo propio: no hay precio de venta, la linea se valoriza al costo y
 * el descuento se ignora. Estos pedidos no cuentan como venta en ningun calculo.
 */
@Component
public class PrecioAlCosto implements PoliticaPrecio {

    @Override
    public Set<TipoCuenta> tiposSoportados() {
        return Set.of(TipoCuenta.REFUERZO, TipoCuenta.CONSUMO);
    }

    @Override
    public BigDecimal precioUnitario(Producto producto, CondicionVenta condicion,
                                     BigDecimal descuentoPct) {
        return producto.getCosto().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal descuentoAplicable(BigDecimal descuentoSolicitado) {
        return BigDecimal.ZERO;
    }
}
