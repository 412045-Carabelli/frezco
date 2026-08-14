package ar.frezco.config;

import java.time.LocalDate;

/**
 * Rango de fechas de una consulta. Los limites nunca son nulos: evita el
 * {@code (:desde IS NULL OR ...)} repetido en cada query y los problemas de inferencia de
 * tipos de Hibernate con parametros nulos.
 */
public record Periodo(LocalDate desde, LocalDate hasta) {

    private static final LocalDate INICIO = LocalDate.of(1900, 1, 1);
    private static final LocalDate FIN = LocalDate.of(2999, 12, 31);

    public static Periodo de(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde == null ? INICIO : desde;
        LocalDate fin = hasta == null ? FIN : hasta;
        if (inicio.isAfter(fin)) {
            throw new ExcepcionesNegocio.DatoInvalido("desde", "La fecha desde es posterior a la hasta");
        }
        return new Periodo(inicio, fin);
    }

    public static Periodo mesDe(LocalDate fecha) {
        return new Periodo(fecha.withDayOfMonth(1), fecha.withDayOfMonth(fecha.lengthOfMonth()));
    }

    /** true si el limite inferior es el sentinel, es decir, si no se filtro por fecha. */
    public boolean sinInicio() {
        return desde.equals(INICIO);
    }
}
