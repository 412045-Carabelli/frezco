package ar.frezco.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoDTO(
        Long id,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,
        @Size(max = 60) String categoria,
        @NotNull(message = "El proveedor es obligatorio") Long proveedorId,
        String proveedorNombre,
        BigDecimal kg,
        BigDecimal lt,
        @PositiveOrZero(message = "El costo no puede ser negativo") BigDecimal costo,
        @PositiveOrZero(message = "El precio minorista no puede ser negativo") BigDecimal precioMinorista,
        @PositiveOrZero(message = "El precio mayorista no puede ser negativo") BigDecimal precioMayorista,
        @PositiveOrZero(message = "El precio por cantidad no puede ser negativo") BigDecimal precioCantidad,
        @PositiveOrZero(message = "El descuento no puede ser negativo")
        @jakarta.validation.constraints.DecimalMax(value = "100.00", message = "El descuento no puede superar el 100%")
        BigDecimal descuentoPct,
        boolean activo) {

    public static ProductoDTO de(Producto producto) {
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getProveedor().getId(),
                producto.getProveedor().getNombre(),
                producto.getKg(),
                producto.getLt(),
                producto.getCosto(),
                producto.getPrecioMinorista(),
                producto.getPrecioMayorista(),
                producto.getPrecioCantidad(),
                producto.getDescuentoPct(),
                producto.isActivo());
    }
}
