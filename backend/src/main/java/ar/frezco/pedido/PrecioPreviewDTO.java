package ar.frezco.pedido;

import java.math.BigDecimal;

/**
 * Vista previa para la pantalla de carga. {@code stockDisponible} es informativo: el reparto
 * real lo hace el backend al guardar.
 */
public record PrecioPreviewDTO(BigDecimal precioUnitario, BigDecimal stockDisponible) {
}
