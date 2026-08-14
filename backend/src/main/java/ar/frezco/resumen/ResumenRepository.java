package ar.frezco.resumen;

import ar.frezco.pedido.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Solo cuentan los pedidos de clientes: refuerzos y consumos no son ventas.
 * Ver docs/03-reglas-negocio.md punto 6.
 */
public interface ResumenRepository extends Repository<Pedido, Long> {

    @Query("""
            SELECT COALESCE(SUM(l.unidades * l.precioUnitario), 0) AS ventas,
                   COALESCE(SUM(l.unidades * l.costoUnitario), 0) AS costo
            FROM PedidoLinea l JOIN l.pedido ped JOIN ped.cuenta c
            WHERE ped.anulado = false AND c.tipo = ar.frezco.cuenta.TipoCuenta.CLIENTE
              AND ped.fecha BETWEEN :desde AND :hasta
            """)
    TotalesDeVenta totalesDeVenta(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
            SELECT COUNT(ped)
            FROM Pedido ped JOIN ped.cuenta c
            WHERE ped.anulado = false AND c.tipo = ar.frezco.cuenta.TipoCuenta.CLIENTE
              AND ped.fecha BETWEEN :desde AND :hasta
            """)
    long cantidadDePedidos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    interface TotalesDeVenta {
        BigDecimal getVentas();

        BigDecimal getCosto();
    }
}
