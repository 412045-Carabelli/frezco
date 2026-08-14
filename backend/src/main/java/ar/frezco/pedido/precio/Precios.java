package ar.frezco.pedido.precio;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.cuenta.TipoCuenta;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Elige la politica de precio segun el tipo de cuenta del pedido. */
@Component
public class Precios {

    private final Map<TipoCuenta, PoliticaPrecio> porTipo = new EnumMap<>(TipoCuenta.class);

    public Precios(List<PoliticaPrecio> politicas) {
        for (PoliticaPrecio politica : politicas) {
            for (TipoCuenta tipo : politica.tiposSoportados()) {
                porTipo.put(tipo, politica);
            }
        }
    }

    public PoliticaPrecio para(TipoCuenta tipo) {
        PoliticaPrecio politica = porTipo.get(tipo);
        if (politica == null) {
            throw new ExcepcionesNegocio.Conflicto(
                    "Las cuentas de tipo " + tipo + " no admiten pedidos");
        }
        return politica;
    }
}
