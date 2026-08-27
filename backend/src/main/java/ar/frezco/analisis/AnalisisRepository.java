package ar.frezco.analisis;

import ar.frezco.pedido.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Rankings comerciales: solo pedidos de clientes, no anulados. Ver docs/03-reglas-negocio.md
 * punto 6 (misma logica que el resumen) y docs/07-decisiones-pendientes.md D5.
 */
public interface AnalisisRepository extends Repository<Pedido, Long> {

    @Query("""
            SELECT p.id AS productoId, p.nombre AS nombre,
                   SUM(l.unidades) AS unidades,
                   SUM(l.unidades * l.precioUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped JOIN ped.cuenta c JOIN l.producto p
            WHERE ped.anulado = false AND c.tipo = ar.frezco.cuenta.TipoCuenta.CLIENTE
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY p.id, p.nombre
            ORDER BY SUM(l.unidades) DESC
            """)
    List<RankingProducto> porProducto(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
                                      Pageable pageable);

    @Query("""
            SELECT c.id AS cuentaId, c.nombre AS nombre,
                   COUNT(DISTINCT ped.id) AS cantidadPedidos,
                   SUM(l.unidades * l.precioUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped JOIN ped.cuenta c
            WHERE ped.anulado = false AND c.tipo = ar.frezco.cuenta.TipoCuenta.CLIENTE
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY c.id, c.nombre
            ORDER BY SUM(l.unidades * l.precioUnitario) DESC
            """)
    List<RankingCliente> porCliente(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
                                    Pageable pageable);

    @Query("""
            SELECT COALESCE(c.zona, 'Sin zona') AS zona,
                   SUM(l.unidades * l.precioUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped JOIN ped.cuenta c
            WHERE ped.anulado = false AND c.tipo = ar.frezco.cuenta.TipoCuenta.CLIENTE
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY c.zona
            ORDER BY SUM(l.unidades * l.precioUnitario) DESC
            """)
    List<RankingZona> porZona(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    interface RankingProducto {
        Long getProductoId();

        String getNombre();

        BigDecimal getUnidades();

        BigDecimal getImporte();
    }

    interface RankingCliente {
        Long getCuentaId();

        String getNombre();

        long getCantidadPedidos();

        BigDecimal getImporte();
    }

    interface RankingZona {
        String getZona();

        BigDecimal getImporte();
    }
}
