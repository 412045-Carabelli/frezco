package ar.frezco.cuentacorriente;

import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.TipoCuenta;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Vista informativa: acumulado de lo que se pidio de mas al proveedor para tener stock.
 * Ese importe ya esta dentro del saldo del proveedor, no es una deuda aparte.
 */
@Component
public class CuentaCorrienteDeRefuerzo implements EstrategiaCuentaCorriente {

    private final CuentaCorrienteRepository repositorio;

    public CuentaCorrienteDeRefuerzo(CuentaCorrienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public TipoCuenta tipoSoportado() {
        return TipoCuenta.REFUERZO;
    }

    @Override
    public boolean esDeuda() {
        return false;
    }

    @Override
    public List<Asiento> asientos(Cuenta cuenta, Periodo periodo) {
        return repositorio.refuerzosDe(cuenta.getId(), periodo.desde(), periodo.hasta()).stream()
                .map(refuerzo -> Asiento.debe(refuerzo.getFecha(), Asiento.Origen.PEDIDO,
                        refuerzo.getNumero(), refuerzo.getImporte()))
                .toList();
    }
}
