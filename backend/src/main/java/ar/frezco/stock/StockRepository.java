package ar.frezco.stock;

import ar.frezco.producto.Producto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * El stock no se guarda: sale de sumar entradas y restar salidas sobre las lineas de pedidos
 * no anulados. Ver docs/02-modelo-datos.md, "por que no hay tabla de stock".
 */
public interface StockRepository extends Repository<Producto, Long> {

    @Query("""
            SELECT l.producto.id AS productoId,
                   SUM(l.entradaStock - l.salidaStock) AS stock
            FROM PedidoLinea l
            WHERE l.pedido.anulado = false
            GROUP BY l.producto.id
            """)
    List<StockDeProducto> stockPorProducto();

    @Query("""
            SELECT p.id AS productoId,
                   p.nombre AS nombre,
                   p.categoria AS categoria,
                   COALESCE(SUM(CASE WHEN ped.id IS NOT NULL THEN l.entradaStock ELSE 0 END), 0) AS entradas,
                   COALESCE(SUM(CASE WHEN ped.id IS NOT NULL THEN l.salidaStock ELSE 0 END), 0) AS salidas,
                   MAX(CASE WHEN l.entradaStock > 0 THEN ped.fecha ELSE NULL END) AS ultimaEntrada,
                   MAX(CASE WHEN l.salidaStock > 0 THEN ped.fecha ELSE NULL END) AS ultimaSalida
            FROM Producto p
            LEFT JOIN PedidoLinea l ON l.producto = p
            LEFT JOIN Pedido ped ON ped = l.pedido AND ped.anulado = false
            WHERE p.activo = true
            GROUP BY p.id, p.nombre, p.categoria
            ORDER BY p.nombre
            """)
    List<VistaStock> vistaDeStock();

    @Query("""
            SELECT ped.fecha AS fecha,
                   ped.numero AS numero,
                   c.nombre AS cuenta,
                   l.entradaStock AS entrada,
                   l.salidaStock AS salida
            FROM PedidoLinea l
            JOIN l.pedido ped
            JOIN ped.cuenta c
            WHERE l.producto.id = :productoId
              AND ped.anulado = false
              AND ped.fecha BETWEEN :desde AND :hasta
            ORDER BY ped.fecha, ped.id
            """)
    List<MovimientoDeStock> movimientosDe(@Param("productoId") Long productoId,
                                          @Param("desde") LocalDate desde,
                                          @Param("hasta") LocalDate hasta);

    interface StockDeProducto {
        Long getProductoId();

        BigDecimal getStock();
    }

    interface VistaStock {
        Long getProductoId();

        String getNombre();

        String getCategoria();

        BigDecimal getEntradas();

        BigDecimal getSalidas();

        LocalDate getUltimaEntrada();

        LocalDate getUltimaSalida();
    }

    interface MovimientoDeStock {
        LocalDate getFecha();

        String getNumero();

        String getCuenta();

        BigDecimal getEntrada();

        BigDecimal getSalida();
    }
}
