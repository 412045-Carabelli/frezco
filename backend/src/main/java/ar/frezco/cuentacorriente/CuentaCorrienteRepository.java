package ar.frezco.cuentacorriente;

import ar.frezco.pedido.PedidoLinea;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Los importes de cuenta corriente salen siempre de las lineas: el pedido no guarda totales.
 * Ver docs/03-reglas-negocio.md punto 3.
 */
public interface CuentaCorrienteRepository extends Repository<PedidoLinea, Long> {

    /** Ventas de una cuenta: unidades por precio. */
    @Query("""
            SELECT ped.fecha AS fecha, ped.numero AS numero,
                   SUM(l.unidades * l.precioUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped
            WHERE ped.cuenta.id = :cuentaId AND ped.anulado = false
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY ped.id, ped.fecha, ped.numero
            ORDER BY ped.fecha, ped.id
            """)
    List<ImportePorPedido> ventasDe(@Param("cuentaId") Long cuentaId,
                                    @Param("desde") LocalDate desde,
                                    @Param("hasta") LocalDate hasta);

    /** Mercaderia pedida al proveedor, venga del pedido que venga. */
    @Query("""
            SELECT ped.fecha AS fecha,
                   CONCAT(ped.numero, ' - ', c.nombre) AS numero,
                   SUM(l.unidadesProveedor * l.costoUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped JOIN ped.cuenta c
            WHERE ped.anulado = false AND l.unidadesProveedor > 0
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY ped.id, ped.fecha, ped.numero, c.nombre
            ORDER BY ped.fecha, ped.id
            """)
    List<ImportePorPedido> pedidosAlProveedor(@Param("desde") LocalDate desde,
                                              @Param("hasta") LocalDate hasta);

    /** Refuerzos: lo que se pidio de mas al proveedor para tener stock. */
    @Query("""
            SELECT ped.fecha AS fecha, ped.numero AS numero,
                   SUM(l.unidadesProveedor * l.costoUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped
            WHERE ped.cuenta.id = :cuentaId AND ped.anulado = false
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY ped.id, ped.fecha, ped.numero
            ORDER BY ped.fecha, ped.id
            """)
    List<ImportePorPedido> refuerzosDe(@Param("cuentaId") Long cuentaId,
                                       @Param("desde") LocalDate desde,
                                       @Param("hasta") LocalDate hasta);

    /** Consumo propio: costo de la mercaderia consumida internamente. */
    @Query("""
            SELECT ped.fecha AS fecha, ped.numero AS numero,
                   SUM(l.unidades * l.costoUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped
            WHERE ped.cuenta.id = :cuentaId AND ped.anulado = false
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY ped.id, ped.fecha, ped.numero
            ORDER BY ped.fecha, ped.id
            """)
    List<ImportePorPedido> consumosDe(@Param("cuentaId") Long cuentaId,
                                      @Param("desde") LocalDate desde,
                                      @Param("hasta") LocalDate hasta);

    interface ImportePorPedido {
        LocalDate getFecha();

        String getNumero();

        BigDecimal getImporte();
    }
}
