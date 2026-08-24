package ar.frezco.stock;

import java.math.BigDecimal;

/**
 * Top de productos mas vendidos, con una sugerencia simple de cuanto reponer:
 * lo vendido en los ultimos 30 dias menos el stock actual, nunca negativo.
 */
public record RankingProductoDTO(
        Long productoId,
        String nombre,
        BigDecimal unidadesVendidas,
        BigDecimal sugeridoReponer) {
}
