package ar.frezco.resumen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenDTO(
        LocalDate desde,
        LocalDate hasta,
        BigDecimal ventas,
        BigDecimal costo,
        BigDecimal margen,
        BigDecimal margenPct,
        long cantidadPedidos,
        BigDecimal ticketPromedio,
        BigDecimal saldoACobrar,
        BigDecimal saldoAPagar,
        List<String> productosSinStock) {
}
