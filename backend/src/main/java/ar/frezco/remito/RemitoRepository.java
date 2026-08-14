package ar.frezco.remito;

import ar.frezco.pedido.PedidoLinea;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RemitoRepository extends Repository<PedidoLinea, Long> {

    /**
     * Lineas a pedirle al proveedor en el periodo, de cualquier pedido. Se consolidan por
     * fecha, producto y costo: dos pedidos del mismo dia con el mismo articulo salen en una
     * sola linea. Si el costo cambio entre pedidos, salen separadas, que es lo correcto.
     */
    @Query("""
            SELECT ped.fecha AS fecha,
                   p.nombre AS producto,
                   l.costoUnitario AS costoUnitario,
                   SUM(l.unidadesProveedor) AS unidades
            FROM PedidoLinea l JOIN l.pedido ped JOIN l.producto p
            WHERE ped.anulado = false AND l.unidadesProveedor > 0
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY ped.fecha, p.nombre, l.costoUnitario
            ORDER BY ped.fecha, p.nombre
            """)
    List<LineaProveedor> lineasParaProveedor(@Param("desde") LocalDate desde,
                                             @Param("hasta") LocalDate hasta);

    interface LineaProveedor {
        LocalDate getFecha();

        String getProducto();

        BigDecimal getCostoUnitario();

        BigDecimal getUnidades();
    }
}
