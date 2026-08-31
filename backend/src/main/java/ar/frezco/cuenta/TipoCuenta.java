package ar.frezco.cuenta;

/**
 * REFUERZO y CONSUMO no son clientes reales: son etiquetas que permiten cargar
 * movimientos de mercaderia con la misma pantalla de pedidos. Debe existir exactamente
 * una cuenta de cada una. PROVEEDOR puede tener varias: el negocio le compra a mas de uno.
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

    /** REFUERZO y CONSUMO son cuentas fijas del sistema: debe existir exactamente una y no
     *  se dan de baja. PROVEEDOR ya no lo es: el negocio puede tener varios. */
    public boolean esUnica() {
        return this == REFUERZO || this == CONSUMO;
    }
}
