package ar.frezco.cuentacorriente;

import java.math.BigDecimal;

public record SaldoDTO(Long cuentaId, String nombre, BigDecimal saldo) {
}
