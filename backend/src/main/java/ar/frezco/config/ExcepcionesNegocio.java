package ar.frezco.config;

/** Excepciones de negocio del sistema. Las traduce a HTTP {@link ManejadorExcepciones}. */
public final class ExcepcionesNegocio {

    private ExcepcionesNegocio() {
    }

    /** 404. */
    public static class NoEncontrado extends RuntimeException {
        public NoEncontrado(String mensaje) {
            super(mensaje);
        }
    }

    /** 409: la operacion es valida en forma pero rompe una regla de negocio. */
    public static class Conflicto extends RuntimeException {
        public Conflicto(String mensaje) {
            super(mensaje);
        }
    }

    /** 400: dato invalido detectado fuera de las anotaciones de validacion. */
    public static class DatoInvalido extends RuntimeException {
        private final String campo;

        public DatoInvalido(String campo, String mensaje) {
            super(mensaje);
            this.campo = campo;
        }

        public String getCampo() {
            return campo;
        }
    }
}
