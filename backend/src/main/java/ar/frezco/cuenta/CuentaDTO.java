package ar.frezco.cuenta;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CuentaDTO(
        Long id,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,
        @NotNull(message = "El tipo es obligatorio") TipoCuenta tipo,
        @Size(max = 60) String zona,
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.00", message = "El descuento no puede superar el 100%")
        BigDecimal descuentoPct,
        boolean activo) {

    public static CuentaDTO de(Cuenta cuenta) {
        return new CuentaDTO(
                cuenta.getId(),
                cuenta.getNombre(),
                cuenta.getTipo(),
                cuenta.getZona(),
                cuenta.getDescuentoPct(),
                cuenta.isActivo());
    }
}
