package ar.frezco.pedido.reparto;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.cuenta.TipoCuenta;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Elige la estrategia de reparto segun el tipo de cuenta. Spring inyecta todas las
 * implementaciones; agregar una nueva no toca esta clase.
 */
@Component
public class Repartos {

    private final Map<TipoCuenta, EstrategiaReparto> porTipo = new EnumMap<>(TipoCuenta.class);

    public Repartos(List<EstrategiaReparto> estrategias) {
        for (EstrategiaReparto estrategia : estrategias) {
            for (TipoCuenta tipo : estrategia.tiposSoportados()) {
                porTipo.put(tipo, estrategia);
            }
        }
    }

    public EstrategiaReparto para(TipoCuenta tipo) {
        EstrategiaReparto estrategia = porTipo.get(tipo);
        if (estrategia == null) {
            throw new ExcepcionesNegocio.Conflicto(
                    "Las cuentas de tipo " + tipo + " no admiten pedidos");
        }
        return estrategia;
    }
}
