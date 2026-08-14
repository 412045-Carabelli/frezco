package ar.frezco.stock;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Stock disponible mientras se procesa un pedido. Se carga una sola vez al empezar y se va
 * descontando linea por linea: dos lineas del mismo producto no pueden salir las dos del
 * mismo stock. Ver docs/03-reglas-negocio.md, "el stock se consume dentro del mismo pedido".
 */
public class StockDisponible {

    private final Map<Long, BigDecimal> porProducto;

    public StockDisponible(Map<Long, BigDecimal> stockInicial) {
        this.porProducto = new HashMap<>(stockInicial);
    }

    public static StockDisponible vacio() {
        return new StockDisponible(Map.of());
    }

    /** Nunca negativo: un stock negativo no habilita a vender de mas. */
    public BigDecimal de(Long productoId) {
        BigDecimal stock = porProducto.getOrDefault(productoId, BigDecimal.ZERO);
        return stock.max(BigDecimal.ZERO);
    }

    public void consumir(Long productoId, BigDecimal unidades) {
        porProducto.merge(productoId, unidades.negate(), BigDecimal::add);
    }

    public void agregar(Long productoId, BigDecimal unidades) {
        porProducto.merge(productoId, unidades, BigDecimal::add);
    }
}
