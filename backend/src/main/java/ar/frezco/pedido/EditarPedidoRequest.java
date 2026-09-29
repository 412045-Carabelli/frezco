package ar.frezco.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Igual a {@link CrearPedidoRequest} pero sin cuentaId: al editar, la cuenta del pedido
 * no cambia. Solo se puede editar mientras el pedido no tenga cobro/pago asociado.
 */
public record EditarPedidoRequest(
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        CondicionVenta condicion,
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.00", message = "El descuento no puede superar el 100%")
        BigDecimal descuentoPct,
        @Size(max = 300) String observacion,
        @NotEmpty(message = "El pedido no tiene lineas") @Valid List<CrearPedidoRequest.LineaRequest> lineas) {
}
