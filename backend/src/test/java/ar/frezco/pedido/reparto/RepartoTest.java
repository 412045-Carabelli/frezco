package ar.frezco.pedido.reparto;

import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.pedido.PedidoLinea;
import ar.frezco.producto.Producto;
import ar.frezco.stock.StockDisponible;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Los siete casos de referencia de docs/03-reglas-negocio.md, tomados de la operacion real.
 * Si alguno de estos falla, el stock del sistema deja de coincidir con la realidad.
 */
class RepartoTest {

    private static final Long PRODUCTO_ID = 7L;

    private final Repartos repartos = new Repartos(List.of(new RepartoDeSalida(), new RepartoDeEntrada()));

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            // caso,                        tipo,     stockPrevio, unidades, salida, proveedor, entrada
            "Venta sin stock,               CLIENTE,  0,           1,        0,      1,         0",
            "Venta con stock suficiente,    CLIENTE,  3,           1,        1,      0,         0",
            "Venta con stock parcial,       CLIENTE,  1,           4,        1,      3,         0",
            "Venta con stock exacto,        CLIENTE,  2,           2,        2,      0,         0",
            "Refuerzo,                      REFUERZO, 0,           3,        0,      3,         3",
            "Consumo propio con stock,      CONSUMO,  4,           1,        1,      0,         0",
            "Consumo propio sin stock,      CONSUMO,  0,           1,        0,      1,         0"
    })
    void reparteSegunLaTablaDeReferencia(String caso, TipoCuenta tipo, int stockPrevio, int unidades,
                                         int salidaEsperada, int proveedorEsperado, int entradaEsperada) {
        StockDisponible stock = stockCon(stockPrevio);
        PedidoLinea linea = linea(unidades);

        repartos.para(tipo).repartir(linea, stock);

        assertThat(linea.getSalidaStock()).isEqualByComparingTo(valor(salidaEsperada));
        assertThat(linea.getUnidadesProveedor()).isEqualByComparingTo(valor(proveedorEsperado));
        assertThat(linea.getEntradaStock()).isEqualByComparingTo(valor(entradaEsperada));
    }

    @Test
    @DisplayName("dos lineas del mismo producto consumen el stock de forma incremental")
    void consumeElStockDentroDelMismoPedido() {
        StockDisponible stock = stockCon(3);
        PedidoLinea primera = linea(2);
        PedidoLinea segunda = linea(2);

        EstrategiaReparto reparto = repartos.para(TipoCuenta.CLIENTE);
        reparto.repartir(primera, stock);
        reparto.repartir(segunda, stock);

        assertThat(primera.getSalidaStock()).isEqualByComparingTo(valor(2));
        assertThat(primera.getUnidadesProveedor()).isEqualByComparingTo(valor(0));
        // Solo queda 1 en stock: la segunda linea toma 1 y pide 1.
        assertThat(segunda.getSalidaStock()).isEqualByComparingTo(valor(1));
        assertThat(segunda.getUnidadesProveedor()).isEqualByComparingTo(valor(1));
    }

    @Test
    @DisplayName("un refuerzo deja stock disponible para las lineas siguientes del mismo pedido")
    void elRefuerzoAlimentaElStockDelMismoPedido() {
        StockDisponible stock = stockCon(0);
        PedidoLinea entrada = linea(5);

        repartos.para(TipoCuenta.REFUERZO).repartir(entrada, stock);

        assertThat(stock.de(PRODUCTO_ID)).isEqualByComparingTo(valor(5));
    }

    @Test
    @DisplayName("un stock negativo heredado no habilita a vender de mas")
    void noVendeContraStockNegativo() {
        StockDisponible stock = new StockDisponible(Map.of(PRODUCTO_ID, new BigDecimal("-4")));
        PedidoLinea linea = linea(2);

        repartos.para(TipoCuenta.CLIENTE).repartir(linea, stock);

        assertThat(linea.getSalidaStock()).isEqualByComparingTo(valor(0));
        assertThat(linea.getUnidadesProveedor()).isEqualByComparingTo(valor(2));
    }

    @Test
    @DisplayName("la cuenta del proveedor no admite pedidos")
    void rechazaPedidosDeProveedor() {
        assertThatThrownBy(() -> repartos.para(TipoCuenta.PROVEEDOR))
                .hasMessageContaining("no admiten pedidos");
    }

    private StockDisponible stockCon(int unidades) {
        return new StockDisponible(Map.of(PRODUCTO_ID, valor(unidades)));
    }

    private PedidoLinea linea(int unidades) {
        Producto producto = new Producto();
        producto.setId(PRODUCTO_ID);
        producto.setNombre("Arandano 1kg");

        PedidoLinea linea = new PedidoLinea();
        linea.setProducto(producto);
        linea.setUnidades(valor(unidades));
        return linea;
    }

    private BigDecimal valor(int unidades) {
        return new BigDecimal(unidades);
    }
}
