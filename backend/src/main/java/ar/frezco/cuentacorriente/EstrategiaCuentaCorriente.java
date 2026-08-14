package ar.frezco.cuentacorriente;

import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.TipoCuenta;

import java.util.List;

/**
 * Strategy: que cuenta en el debe y que en el haber. Las cuatro reglas de
 * docs/03-reglas-negocio.md punto 3 son distintas entre si, y agregar un tipo de cuenta no
 * deberia obligar a tocar el servicio.
 */
public interface EstrategiaCuentaCorriente {

    TipoCuenta tipoSoportado();

    List<Asiento> asientos(Cuenta cuenta, Periodo periodo);

    /** true si el saldo representa una deuda real; false si es una vista informativa. */
    default boolean esDeuda() {
        return true;
    }
}
