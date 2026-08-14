package ar.frezco.pedido;

import ar.frezco.cuenta.TipoCuenta;
import org.springframework.stereotype.Component;

/**
 * Tres secuencias independientes segun el tipo de cuenta: 'Vta 001', 'Ref 001', 'Cons 001'.
 * Se resuelve dentro de la transaccion de guardado. Los numeros de pedidos anulados no se
 * reutilizan.
 */
@Component
public class NumeradorDePedidos {

    private final PedidoRepository repositorio;

    public NumeradorDePedidos(PedidoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public String siguiente(TipoCuenta tipo) {
        String prefijo = tipo.prefijoPedido();
        String ultimo = repositorio.ultimoNumeroDePrefijo(prefijo);
        int proximo = ultimo == null ? 1 : numeroDe(ultimo) + 1;
        return "%s %03d".formatted(prefijo, proximo);
    }

    private int numeroDe(String numero) {
        try {
            return Integer.parseInt(numero.substring(numero.indexOf(' ') + 1).trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Numero de pedido con formato inesperado: " + numero, e);
        }
    }
}
