package ar.frezco.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** El cliente no manda precios: los resuelve el backend segun la condicion y el descuento. */
public record CrearPedidoRequest(
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotNull(message = "La cuenta es obligatoria") Long cuentaId,
        CondicionVenta condicion,
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.00", message = "El descuento no puede superar el 100%")
        BigDecimal descuentoPct,
        @Size(max = 300) String observacion,
        @NotEmpty(message = "El pedido no tiene lineas") @Valid List<LineaRequest> lineas) {

    public record LineaRequest(
            @NotNull(message = "El articulo es obligatorio") Long productoId,
            @NotNull(message = "Las unidades son obligatorias")
            @Positive(message = "Las unidades tienen que ser mayores a cero")
            BigDecimal unidades) {
    }
}
