package ar.frezco.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoStockDTO(
        LocalDate fecha,
        String numero,
        String cuenta,
        BigDecimal entrada,
        BigDecimal salida,
        BigDecimal saldo) {
}
