package ar.frezco.pedido.reparto;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.PedidoLinea;
import ar.frezco.stock.StockDisponible;

import java.util.Set;

/**
 * Strategy: como se reparte una linea entre stock propio y pedido al proveedor. Varia segun
 * el tipo de cuenta del pedido y es la unica logica del sistema con casos de prueba
 * cerrados (docs/03-reglas-negocio.md, tabla de casos de referencia).
 */
public interface EstrategiaReparto {

    Set<TipoCuenta> tiposSoportados();

    /**
     * Completa {@code entradaStock}, {@code salidaStock} y {@code unidadesProveedor} de la
     * linea, y descuenta o suma lo que corresponda en {@code stock}.
     */
    void repartir(PedidoLinea linea, StockDisponible stock);
}
