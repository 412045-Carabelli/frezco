package ar.frezco.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockDTO(
        Long productoId,
        String nombre,
        String categoria,
        BigDecimal entradas,
        BigDecimal salidas,
        BigDecimal stockActual,
        LocalDate ultimaEntrada,
        LocalDate ultimaSalida) {
}
