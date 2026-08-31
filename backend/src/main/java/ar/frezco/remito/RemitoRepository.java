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
     * Lineas a pedirle a un proveedor puntual en el periodo, de cualquier pedido, consolidadas
     * por producto en todo el rango (sin separar por dia): una sola fila por articulo con el
     * total de unidades y el importe acumulado. El proveedor es el elegido en la linea, no el
     * del articulo (que es solo un default en el momento de cargar el pedido).
     */
    @Query("""
            SELECT p.nombre AS producto,
                   SUM(l.unidadesProveedor) AS unidades,
                   SUM(l.unidadesProveedor * l.costoUnitario) AS importe
            FROM PedidoLinea l JOIN l.pedido ped JOIN l.producto p
            WHERE ped.anulado = false AND l.unidadesProveedor > 0
              AND l.proveedor.id = :proveedorId
              AND ped.fecha BETWEEN :desde AND :hasta
            GROUP BY p.nombre
            ORDER BY p.nombre
            """)
    List<LineaConsolidadaProveedor> lineasConsolidadasParaProveedor(@Param("proveedorId") Long proveedorId,
                                                                    @Param("desde") LocalDate desde,
                                                                    @Param("hasta") LocalDate hasta);

    interface LineaConsolidadaProveedor {
        String getProducto();

        BigDecimal getUnidades();

        BigDecimal getImporte();
    }
}
