package ar.frezco.pedido;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.movimiento.MovimientoRepository;
import ar.frezco.pedido.precio.PrecioDeVenta;
import ar.frezco.pedido.precio.Precios;
import ar.frezco.pedido.reparto.RepartoDeSalida;
import ar.frezco.pedido.reparto.Repartos;
import ar.frezco.producto.Producto;
import ar.frezco.producto.ProductoRepository;
import ar.frezco.stock.StockDisponible;
import ar.frezco.stock.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Un pedido se puede editar o anular mientras no tenga un cobro/pago asociado.
 * Ver docs/03-reglas-negocio.md, seccion 7.
 */
class PedidoServiceTest {

    private final PedidoRepository repositorio = mock(PedidoRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final CuentaService cuentas = mock(CuentaService.class);
    private final StockService stock = mock(StockService.class);
    private final Precios precios = new Precios(List.of(new PrecioDeVenta()));
    private final Repartos repartos = new Repartos(List.of(new RepartoDeSalida()));
    private final NumeradorDePedidos numerador = mock(NumeradorDePedidos.class);
    private final MovimientoRepository movimientos = mock(MovimientoRepository.class);

    private final PedidoService servicio =
            new PedidoService(repositorio, productos, cuentas, stock, precios, repartos, numerador, movimientos);

    private Cuenta cliente;
    private Cuenta proveedor;
    private Producto producto;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        cliente = new Cuenta();
        cliente.setId(1L);
        cliente.setNombre("Cliente");
        cliente.setTipo(TipoCuenta.CLIENTE);
        cliente.setActivo(true);

        proveedor = new Cuenta();
        proveedor.setId(2L);
        proveedor.setNombre("Proveedor");
        proveedor.setTipo(TipoCuenta.PROVEEDOR);
        proveedor.setActivo(true);

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Arandano 1kg");
        producto.setCosto(new BigDecimal("100.00"));
        producto.setPrecioMinorista(new BigDecimal("200.00"));

        pedido = new Pedido();
        pedido.setId(5L);
        pedido.setNumero("Vta 001");
        pedido.setCuenta(cliente);
        pedido.setCondicion(CondicionVenta.MINORISTA);
        pedido.setDescuentoPct(BigDecimal.ZERO);

        when(productos.findById(producto.getId())).thenReturn(Optional.of(producto));
        when(cuentas.obtener(proveedor.getId())).thenReturn(proveedor);
        when(stock.disponible()).thenReturn(new StockDisponible(Map.of()));
        when(repositorio.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(repositorio.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("edita un pedido sin pago: recalcula lineas y precios")
    void editaPedidoSinPago() {
        when(movimientos.existsByPedidoId(pedido.getId())).thenReturn(false);

        EditarPedidoRequest peticion = new EditarPedidoRequest(
                LocalDate.now(), CondicionVenta.MINORISTA, BigDecimal.ZERO, "sin el arandano que no trajeron",
                List.of(new CrearPedidoRequest.LineaRequest(producto.getId(), new BigDecimal("2"), proveedor.getId())));

        PedidoDTO resultado = servicio.editar(pedido.getId(), peticion);

        assertThat(resultado.lineas()).hasSize(1);
        assertThat(resultado.total()).isEqualByComparingTo("400.00");
        assertThat(resultado.editable()).isTrue();
    }

    @Test
    @DisplayName("no permite editar un pedido que ya tiene un cobro o pago asociado")
    void noEditaPedidoConPago() {
        when(movimientos.existsByPedidoId(pedido.getId())).thenReturn(true);

        EditarPedidoRequest peticion = new EditarPedidoRequest(
                LocalDate.now(), CondicionVenta.MINORISTA, BigDecimal.ZERO, null,
                List.of(new CrearPedidoRequest.LineaRequest(producto.getId(), BigDecimal.ONE, null)));

        assertThatThrownBy(() -> servicio.editar(pedido.getId(), peticion))
                .isInstanceOf(ExcepcionesNegocio.Conflicto.class)
                .hasMessageContaining("cobro o pago asociado");
    }

    @Test
    @DisplayName("no permite anular un pedido que ya tiene un cobro o pago asociado")
    void noAnulaPedidoConPago() {
        when(movimientos.existsByPedidoId(pedido.getId())).thenReturn(true);

        assertThatThrownBy(() -> servicio.anular(pedido.getId()))
                .isInstanceOf(ExcepcionesNegocio.Conflicto.class)
                .hasMessageContaining("cobro o pago asociado");
    }

    @Test
    @DisplayName("anula un pedido sin pago asociado")
    void anulaPedidoSinPago() {
        when(movimientos.existsByPedidoId(pedido.getId())).thenReturn(false);

        servicio.anular(pedido.getId());

        assertThat(pedido.isAnulado()).isTrue();
    }
}
