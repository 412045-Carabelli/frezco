package ar.frezco.cuentacorriente;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un movimiento de cuenta corriente antes de calcular el saldo corriendo. Las estrategias
 * devuelven asientos sueltos; el servicio los ordena y acumula.
 */
public record Asiento(LocalDate fecha, Origen origen, String detalle, BigDecimal debe, BigDecimal haber) {

    public enum Origen {
        PEDIDO,
        MOVIMIENTO
    }

    public static Asiento debe(LocalDate fecha, Origen origen, String detalle, BigDecimal importe) {
        return new Asiento(fecha, origen, detalle, importe, BigDecimal.ZERO);
    }

    public static Asiento haber(LocalDate fecha, Origen origen, String detalle, BigDecimal importe) {
        return new Asiento(fecha, origen, detalle, BigDecimal.ZERO, importe);
    }
}
