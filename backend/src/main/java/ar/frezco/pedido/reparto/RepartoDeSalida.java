package ar.frezco.pedido.reparto;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.PedidoLinea;
import ar.frezco.stock.StockDisponible;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Ventas y consumo propio: sale de stock lo que haya, el resto se le pide al proveedor.
 * El consumo propio se comporta igual que una venta en cuanto al reparto (docs/07 D2).
 */
@Component
public class RepartoDeSalida implements EstrategiaReparto {

    @Override
    public Set<TipoCuenta> tiposSoportados() {
        return Set.of(TipoCuenta.CLIENTE, TipoCuenta.CONSUMO);
    }

    @Override
    public void repartir(PedidoLinea linea, StockDisponible stock) {
        Long productoId = linea.getProducto().getId();
        BigDecimal disponible = stock.de(productoId);
        BigDecimal salida = linea.getUnidades().min(disponible);

        linea.setEntradaStock(BigDecimal.ZERO);
        linea.setSalidaStock(salida);
        linea.setUnidadesProveedor(linea.getUnidades().subtract(salida));

        stock.consumir(productoId, salida);
    }
}
