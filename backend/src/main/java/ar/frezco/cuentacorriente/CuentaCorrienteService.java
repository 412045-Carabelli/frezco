package ar.frezco.cuentacorriente;

import ar.frezco.config.ExcepcionesNegocio;
import ar.frezco.config.Periodo;
import ar.frezco.cuenta.Cuenta;
import ar.frezco.cuenta.CuentaRepository;
import ar.frezco.cuenta.CuentaService;
import ar.frezco.cuenta.TipoCuenta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CuentaCorrienteService {

    private final Map<TipoCuenta, EstrategiaCuentaCorriente> estrategias = new EnumMap<>(TipoCuenta.class);
    private final CuentaService cuentas;
    private final CuentaRepository cuentaRepository;

    public CuentaCorrienteService(List<EstrategiaCuentaCorriente> estrategias,
                                  CuentaService cuentas,
                                  CuentaRepository cuentaRepository) {
        estrategias.forEach(estrategia -> this.estrategias.put(estrategia.tipoSoportado(), estrategia));
        this.cuentas = cuentas;
        this.cuentaRepository = cuentaRepository;
    }

    public CuentaCorrienteDTO consultar(Long cuentaId, LocalDate desde, LocalDate hasta) {
        Cuenta cuenta = cuentas.obtener(cuentaId);
        EstrategiaCuentaCorriente estrategia = estrategiaPara(cuenta.getTipo());
        Periodo periodo = Periodo.de(desde, hasta);

        BigDecimal saldoAnterior = saldoPrevio(cuenta, estrategia, periodo);
        List<Asiento> asientos = ordenados(estrategia.asientos(cuenta, periodo));

        List<CuentaCorrienteDTO.LineaDTO> lineas = new ArrayList<>();
        BigDecimal saldo = saldoAnterior;
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;

        for (Asiento asiento : asientos) {
            saldo = saldo.add(asiento.debe()).subtract(asiento.haber());
            totalDebe = totalDebe.add(asiento.debe());
            totalHaber = totalHaber.add(asiento.haber());
            lineas.add(new CuentaCorrienteDTO.LineaDTO(asiento.fecha(), asiento.origen(),
                    asiento.detalle(), asiento.debe(), asiento.haber(), saldo));
        }

        return new CuentaCorrienteDTO(
                new CuentaCorrienteDTO.CuentaResumenDTO(cuenta.getId(), cuenta.getNombre(), cuenta.getTipo()),
                saldoAnterior, totalDebe, totalHaber, saldo, estrategia.esDeuda(), lineas);
    }

    /** Saldos de todas las cuentas de un tipo. Con 30 cuentas no hace falta optimizar. */
    public List<SaldoDTO> saldos(TipoCuenta tipo, boolean soloConSaldo) {
        Periodo historico = Periodo.de(null, null);

        return cuentaRepository.findByActivoTrueOrderByNombreAsc().stream()
                .filter(cuenta -> tipo == null || cuenta.getTipo() == tipo)
                .map(cuenta -> new SaldoDTO(cuenta.getId(), cuenta.getNombre(),
                        saldoDe(cuenta, estrategiaPara(cuenta.getTipo()), historico)))
                .filter(saldo -> !soloConSaldo || saldo.saldo().signum() != 0)
                .sorted(Comparator.comparing(SaldoDTO::saldo).reversed())
                .toList();
    }

    /** Total adeudado por los clientes, para el resumen. */
    public BigDecimal saldoACobrar() {
        return saldos(TipoCuenta.CLIENTE, false).stream()
                .map(SaldoDTO::saldo)
                .filter(saldo -> saldo.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Suma de lo que se debe a todos los proveedores, para el resumen. */
    public BigDecimal saldoAPagar() {
        return saldos(TipoCuenta.PROVEEDOR, false).stream()
                .map(SaldoDTO::saldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Saldo actual (histórico completo) de una cuenta puntual. */
    public BigDecimal saldoActual(Cuenta cuenta) {
        return saldoDe(cuenta, estrategiaPara(cuenta.getTipo()), Periodo.de(null, null));
    }

    private BigDecimal saldoPrevio(Cuenta cuenta, EstrategiaCuentaCorriente estrategia, Periodo periodo) {
        if (periodo.sinInicio()) {
            return BigDecimal.ZERO;
        }
        return saldoDe(cuenta, estrategia, Periodo.de(null, periodo.desde().minusDays(1)));
    }

    private BigDecimal saldoDe(Cuenta cuenta, EstrategiaCuentaCorriente estrategia, Periodo periodo) {
        return estrategia.asientos(cuenta, periodo).stream()
                .map(asiento -> asiento.debe().subtract(asiento.haber()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Asiento> ordenados(List<Asiento> asientos) {
        return asientos.stream()
                .sorted(Comparator.comparing(Asiento::fecha).thenComparing(Asiento::origen))
                .toList();
    }

    private EstrategiaCuentaCorriente estrategiaPara(TipoCuenta tipo) {
        EstrategiaCuentaCorriente estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new ExcepcionesNegocio.Conflicto("No hay cuenta corriente para el tipo " + tipo);
        }
        return estrategia;
    }
}
