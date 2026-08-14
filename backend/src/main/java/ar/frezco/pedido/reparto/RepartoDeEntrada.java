package ar.frezco.pedido.reparto;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.PedidoLinea;
import ar.frezco.stock.StockDisponible;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Refuerzo de stock: se le pide entero al proveedor y entra entero a stock. Queda disponible
 * para las lineas siguientes del mismo pedido.
 */
@Component
public class RepartoDeEntrada implements EstrategiaReparto {

    @Override
    public Set<TipoCuenta> tiposSoportados() {
        return Set.of(TipoCuenta.REFUERZO);
    }

    @Override
    public void repartir(PedidoLinea linea, StockDisponible stock) {
        BigDecimal unidades = linea.getUnidades();

        linea.setEntradaStock(unidades);
        linea.setSalidaStock(BigDecimal.ZERO);
        linea.setUnidadesProveedor(unidades);

        stock.agregar(linea.getProducto().getId(), unidades);
    }
}
