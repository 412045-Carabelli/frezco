package ar.frezco.cuentacorriente;

import ar.frezco.cuenta.TipoCuenta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CuentaCorrienteDTO(
        CuentaResumenDTO cuenta,
        BigDecimal saldoAnterior,
        BigDecimal totalDebe,
        BigDecimal totalHaber,
        BigDecimal saldoFinal,
        /** false en refuerzo y consumo: el saldo es informativo, no una deuda. */
        boolean esDeuda,
        List<LineaDTO> lineas) {

    public record CuentaResumenDTO(Long id, String nombre, TipoCuenta tipo) {
    }

    public record LineaDTO(
            LocalDate fecha,
            Asiento.Origen origen,
            String detalle,
            BigDecimal debe,
            BigDecimal haber,
            BigDecimal saldo) {
    }
}
