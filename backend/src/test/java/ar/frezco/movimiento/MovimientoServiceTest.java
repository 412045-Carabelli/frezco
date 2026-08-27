package ar.frezco.movimiento;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.cuentacorriente.CuentaCorrienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * No se puede cobrar ni pagar mas de lo que la cuenta debe actualmente.
 * Ver docs/03-reglas-negocio.md.
 */
class MovimientoServiceTest {

    private final MovimientoRepository repositorio = mock(MovimientoRepository.class);
    private final CuentaService cuentas = mock(CuentaService.class);
    private final CuentaCorrienteService cuentasCorrientes = mock(CuentaCorrienteService.class);
    private final MovimientoService servicio = new MovimientoService(repositorio, cuentas, cuentasCorrientes);

    private Cuenta cliente;
    private Cuenta proveedor;

    @BeforeEach
    void setUp() {
        cliente = cuentaDe(1L, TipoCuenta.CLIENTE);
        proveedor = cuentaDe(2L, TipoCuenta.PROVEEDOR);
        when(repositorio.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("rechaza un cobro que supera la deuda del cliente")
    void rechazaCobroQueSuperaLaDeuda() {
        when(cuentas.obtener(cliente.getId())).thenReturn(cliente);
        when(cuentasCorrientes.saldoActual(cliente)).thenReturn(new BigDecimal("1000.00"));

        MovimientoDTO dto = movimiento(cliente.getId(), TipoMovimiento.COBRO, new BigDecimal("1500.00"));

        assertThatThrownBy(() -> servicio.crear(dto))
                .isInstanceOf(ExcepcionesNegocio.Conflicto.class)
                .hasMessageContaining("supera la deuda");
    }

    @Test
    @DisplayName("acepta un cobro igual a la deuda del cliente")
    void aceptaCobroIgualALaDeuda() {
        when(cuentas.obtener(cliente.getId())).thenReturn(cliente);
        when(cuentasCorrientes.saldoActual(cliente)).thenReturn(new BigDecimal("1000.00"));

        MovimientoDTO dto = movimiento(cliente.getId(), TipoMovimiento.COBRO, new BigDecimal("1000.00"));

        assertThat(servicio.crear(dto)).isNotNull();
    }

    @Test
    @DisplayName("rechaza un pago que supera la deuda al proveedor")
    void rechazaPagoQueSuperaLaDeuda() {
        when(cuentas.obtener(proveedor.getId())).thenReturn(proveedor);
        when(cuentasCorrientes.saldoActual(proveedor)).thenReturn(new BigDecimal("500.00"));

        MovimientoDTO dto = movimiento(proveedor.getId(), TipoMovimiento.PAGO, new BigDecimal("600.00"));

        assertThatThrownBy(() -> servicio.crear(dto))
                .isInstanceOf(ExcepcionesNegocio.Conflicto.class)
                .hasMessageContaining("supera la deuda");
    }

    private Cuenta cuentaDe(Long id, TipoCuenta tipo) {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(id);
        cuenta.setNombre("Cuenta " + id);
        cuenta.setTipo(tipo);
        return cuenta;
    }

    private MovimientoDTO movimiento(Long cuentaId, TipoMovimiento tipo, BigDecimal importe) {
        return new MovimientoDTO(null, LocalDate.now(), tipo, cuentaId, null, importe, null);
    }
}
