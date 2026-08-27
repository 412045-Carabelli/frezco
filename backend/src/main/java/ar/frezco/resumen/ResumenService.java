package ar.frezco.resumen;

import ar.frezco.config.Periodo;
import ar.frezco.cuentacorriente.CuentaCorrienteService;
import ar.frezco.stock.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class ResumenService {

    private final ResumenRepository repositorio;
    private final CuentaCorrienteService cuentasCorrientes;
    private final StockService stock;

    public ResumenService(ResumenRepository repositorio,
                          CuentaCorrienteService cuentasCorrientes,
                          StockService stock) {
        this.repositorio = repositorio;
        this.cuentasCorrientes = cuentasCorrientes;
        this.stock = stock;
    }

    public ResumenDTO calcular(LocalDate desde, LocalDate hasta) {
        Periodo periodo = Periodo.de(desde, hasta);

        ResumenRepository.TotalesDeVenta totales =
                repositorio.totalesDeVenta(periodo.desde(), periodo.hasta());
        long cantidadPedidos = repositorio.cantidadDePedidos(periodo.desde(), periodo.hasta());

        BigDecimal ventas = totales.getVentas();
        BigDecimal costo = totales.getCosto();
        BigDecimal margen = ventas.subtract(costo);

        return new ResumenDTO(
                desde,
                hasta,
                ventas,
                costo,
                margen,
                proporcion(margen, ventas),
                cantidadPedidos,
                cantidadPedidos == 0
                        ? BigDecimal.ZERO
                        : ventas.divide(BigDecimal.valueOf(cantidadPedidos), 2, RoundingMode.HALF_UP),
                cuentasCorrientes.saldoACobrar(),
                cuentasCorrientes.saldoAPagar(),
                stock.sinStock());
    }

    private BigDecimal proporcion(BigDecimal parte, BigDecimal total) {
        return total.signum() == 0
                ? BigDecimal.ZERO
                : parte.divide(total, 4, RoundingMode.HALF_UP);
    }
}
