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

/**
 * Al proveedor se le debe todo lo que se le pidio, venga del pedido que venga: ventas que no
 * salieron de stock, refuerzos y consumos. Lo que define el impacto es
 * {@code unidadesProveedor > 0}, no la cuenta del pedido.
 */
@Component
public class CuentaCorrienteDeProveedor implements EstrategiaCuentaCorriente {

    private final CuentaCorrienteRepository repositorio;
    private final MovimientoRepository movimientos;

    public CuentaCorrienteDeProveedor(CuentaCorrienteRepository repositorio,
                                      MovimientoRepository movimientos) {
        this.repositorio = repositorio;
        this.movimientos = movimientos;
    }

    @Override
    public TipoCuenta tipoSoportado() {
        return TipoCuenta.PROVEEDOR;
    }

    @Override
    public List<Asiento> asientos(Cuenta cuenta, Periodo periodo) {
        List<Asiento> asientos = new ArrayList<>();

        repositorio.pedidosAlProveedor(periodo.desde(), periodo.hasta())
                .forEach(pedido -> asientos.add(Asiento.debe(
                        pedido.getFecha(), Asiento.Origen.PEDIDO, pedido.getNumero(), pedido.getImporte())));

        for (Movimiento pago : movimientos.findByCuentaIdOrderByFechaAscIdAsc(cuenta.getId())) {
            if (pago.getTipo() == TipoMovimiento.PAGO
                    && !pago.getFecha().isBefore(periodo.desde())
                    && !pago.getFecha().isAfter(periodo.hasta())) {
                asientos.add(Asiento.haber(pago.getFecha(), Asiento.Origen.MOVIMIENTO,
                        pago.getObservacion() == null || pago.getObservacion().isBlank()
                                ? "Pago" : "Pago - " + pago.getObservacion(),
                        pago.getImporte()));
            }
        }

        return asientos;
    }
}
