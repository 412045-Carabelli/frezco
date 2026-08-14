package ar.frezco.pedido.precio;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.CondicionVenta;
import ar.frezco.producto.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrecioTest {

    private final Precios precios = new Precios(List.of(new PrecioDeVenta(), new PrecioAlCosto()));

    private final Producto producto = producto();

    @Test
    @DisplayName("cada condicion toma su columna de precio")
    void tomaElPrecioDeLaCondicion() {
        PoliticaPrecio politica = precios.para(TipoCuenta.CLIENTE);

        assertThat(politica.precioUnitario(producto, CondicionVenta.MINORISTA, BigDecimal.ZERO))
                .isEqualByComparingTo("16300.00");
        assertThat(politica.precioUnitario(producto, CondicionVenta.MAYORISTA, BigDecimal.ZERO))
                .isEqualByComparingTo("14500.00");
        assertThat(politica.precioUnitario(producto, CondicionVenta.CANTIDAD, BigDecimal.ZERO))
                .isEqualByComparingTo("15200.00");
    }

    @Test
    @DisplayName("el descuento se aplica sobre el precio de la condicion y se redondea a 2 decimales")
    void aplicaElDescuento() {
        BigDecimal precio = precios.para(TipoCuenta.CLIENTE)
                .precioUnitario(producto, CondicionVenta.MINORISTA, new BigDecimal("10"));

        assertThat(precio).isEqualByComparingTo("14670.00");
    }

    @Test
    @DisplayName("refuerzo y consumo se valorizan al costo y descartan el descuento")
    void valorizaAlCostoLasCuentasEspeciales() {
        for (TipoCuenta tipo : List.of(TipoCuenta.REFUERZO, TipoCuenta.CONSUMO)) {
            PoliticaPrecio politica = precios.para(tipo);

            assertThat(politica.descuentoAplicable(new BigDecimal("15"))).isEqualByComparingTo("0");
            assertThat(politica.precioUnitario(producto, CondicionVenta.MINORISTA, BigDecimal.ZERO))
                    .isEqualByComparingTo("11700.00");
        }
    }

    private Producto producto() {
        Producto producto = new Producto();
        producto.setId(3L);
        producto.setNombre("Arandano 1kg");
        producto.setCosto(new BigDecimal("11700.00"));
        producto.setPrecioMinorista(new BigDecimal("16300.00"));
        producto.setPrecioMayorista(new BigDecimal("14500.00"));
        producto.setPrecioCantidad(new BigDecimal("15200.00"));
        return producto;
    }
}
