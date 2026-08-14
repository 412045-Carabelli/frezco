package ar.frezco.cuentacorriente;

import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.TipoCuenta;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Vista informativa del costo de la mercaderia consumida internamente. No genera deuda con
 * nadie: se valoriza por unidades totales, no solo por lo pedido al proveedor.
 */
@Component
public class CuentaCorrienteDeConsumo implements EstrategiaCuentaCorriente {

    private final CuentaCorrienteRepository repositorio;

    public CuentaCorrienteDeConsumo(CuentaCorrienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public TipoCuenta tipoSoportado() {
        return TipoCuenta.CONSUMO;
    }

    @Override
    public boolean esDeuda() {
        return false;
    }

    @Override
    public List<Asiento> asientos(Cuenta cuenta, Periodo periodo) {
        return repositorio.consumosDe(cuenta.getId(), periodo.desde(), periodo.hasta()).stream()
                .map(consumo -> Asiento.debe(consumo.getFecha(), Asiento.Origen.PEDIDO,
                        consumo.getNumero(), consumo.getImporte()))
                .toList();
    }
}
