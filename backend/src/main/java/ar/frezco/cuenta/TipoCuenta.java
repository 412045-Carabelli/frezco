package ar.frezco.cuenta;

/**
 * REFUERZO y CONSUMO no son clientes reales: son etiquetas que permiten cargar
 * movimientos de mercaderia con la misma pantalla de pedidos. Debe existir exactamente
 * una cuenta de cada tipo distinto de CLIENTE.
 */
public enum TipoCuenta {
    CLIENTE,
    PROVEEDOR,
    REFUERZO,
    CONSUMO;

    /** Prefijo de numeracion de los pedidos de este tipo de cuenta. */
    public String prefijoPedido() {
        return switch (this) {
            case CLIENTE -> "Vta";
            case REFUERZO -> "Ref";
            case CONSUMO -> "Cons";
            case PROVEEDOR -> throw new IllegalStateException(
                    "La cuenta del proveedor no puede tener pedidos");
        };
    }

    public boolean esEspecial() {
        return this != CLIENTE;
    }
}
