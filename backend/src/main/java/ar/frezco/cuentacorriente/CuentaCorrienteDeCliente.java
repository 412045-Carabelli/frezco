package ar.frezco.cuentacorriente;

import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.TipoCuenta;
import ar.frezco.movimiento.Movimiento;
import ar.frezco.movimiento.MovimientoRepository;
import ar.frezco.movimiento.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** El cliente debe por sus pedidos de venta y cancela con cobros. */
@Component
public class CuentaCorrienteDeCliente implements EstrategiaCuentaCorriente {

    private final CuentaCorrienteRepository repositorio;
    private final MovimientoRepository movimientos;

    public CuentaCorrienteDeCliente(CuentaCorrienteRepository repositorio,
                                    MovimientoRepository movimientos) {
        this.repositorio = repositorio;
        this.movimientos = movimientos;
    }

    @Override
    public TipoCuenta tipoSoportado() {
        return TipoCuenta.CLIENTE;
    }

    @Override
    public List<Asiento> asientos(Cuenta cuenta, Periodo periodo) {
        List<Asiento> asientos = new ArrayList<>();

        repositorio.ventasDe(cuenta.getId(), periodo.desde(), periodo.hasta())
                .forEach(venta -> asientos.add(Asiento.debe(
                        venta.getFecha(), Asiento.Origen.PEDIDO, venta.getNumero(), venta.getImporte())));

        for (Movimiento cobro : movimientos.findByCuentaIdOrderByFechaAscIdAsc(cuenta.getId())) {
            if (cobro.getTipo() == TipoMovimiento.COBRO && dentroDe(cobro, periodo)) {
                asientos.add(Asiento.haber(cobro.getFecha(), Asiento.Origen.MOVIMIENTO,
                        detalle(cobro), cobro.getImporte()));
            }
        }

        return asientos;
    }

    private boolean dentroDe(Movimiento movimiento, Periodo periodo) {
        return !movimiento.getFecha().isBefore(periodo.desde())
                && !movimiento.getFecha().isAfter(periodo.hasta());
    }

    private String detalle(Movimiento movimiento) {
        return movimiento.getObservacion() == null || movimiento.getObservacion().isBlank()
                ? "Cobro"
                : "Cobro - " + movimiento.getObservacion();
    }
}
