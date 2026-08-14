package ar.frezco.movimiento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoDTO(
        Long id,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotNull(message = "El tipo es obligatorio") TipoMovimiento tipo,
        @NotNull(message = "La cuenta es obligatoria") Long cuentaId,
        String cuentaNombre,
        @NotNull(message = "El importe es obligatorio")
        @Positive(message = "El importe tiene que ser mayor a cero")
        BigDecimal importe,
        @Size(max = 300) String observacion) {

    public static MovimientoDTO de(Movimiento movimiento) {
        return new MovimientoDTO(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getTipo(),
                movimiento.getCuenta().getId(),
                movimiento.getCuenta().getNombre(),
                movimiento.getImporte(),
                movimiento.getObservacion());
    }
}
