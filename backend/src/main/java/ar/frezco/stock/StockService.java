package ar.frezco.stock;

import ar.frezco.config.Periodo;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;

@Service
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository repositorio;

    public StockService(StockRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Foto del stock de todos los productos, para repartir las lineas de un pedido. */
    public StockDisponible disponible() {
        Map<Long, BigDecimal> porProducto = repositorio.stockPorProducto().stream()
                .collect(Collectors.toMap(
                        StockRepository.StockDeProducto::getProductoId,
                        StockRepository.StockDeProducto::getStock));
        return new StockDisponible(porProducto);
    }

    public BigDecimal deProducto(Long productoId) {
        return disponible().de(productoId);
    }

    public List<StockDTO> listar(boolean soloConStock) {
        return repositorio.vistaDeStock().stream()
                .map(vista -> new StockDTO(
                        vista.getProductoId(),
                        vista.getNombre(),
                        vista.getCategoria(),
                        vista.getEntradas(),
                        vista.getSalidas(),
                        vista.getEntradas().subtract(vista.getSalidas()),
                        vista.getUltimaEntrada(),
                        vista.getUltimaSalida()))
                .filter(dto -> !soloConStock || dto.stockActual().signum() > 0)
                .toList();
    }

    /** Detalle de movimientos de un producto con el saldo de stock corriendo. */
    public List<MovimientoStockDTO> movimientos(Long productoId, LocalDate desde, LocalDate hasta) {
        Periodo periodo = Periodo.de(desde, hasta);
        List<MovimientoStockDTO> movimientos = new ArrayList<>();
        BigDecimal saldo = BigDecimal.ZERO;

        for (StockRepository.MovimientoDeStock fila
                : repositorio.movimientosDe(productoId, periodo.desde(), periodo.hasta())) {
            saldo = saldo.add(fila.getEntrada()).subtract(fila.getSalida());
            movimientos.add(new MovimientoStockDTO(
                    fila.getFecha(), fila.getNumero(), fila.getCuenta(),
                    fila.getEntrada(), fila.getSalida(), saldo));
        }
        return movimientos;
    }

    /**
     * Top 5 mas vendidos en los ultimos N dias, con sugerencia de reposicion. Solo ventas a
     * clientes: no cuenta consumo propio ni refuerzos.
     */
    public List<RankingProductoDTO> rankingVentas(int dias) {
        StockDisponible stockActual = disponible();
        LocalDate desde = LocalDate.now().minusDays(dias);
        return repositorio.rankingVentas(desde, PageRequest.of(0, 5)).stream()
                .map(fila -> {
                    BigDecimal vendido = fila.getUnidadesVendidas();
                    BigDecimal disponibleActual = stockActual.de(fila.getProductoId());
                    BigDecimal sugerido = vendido.subtract(disponibleActual).max(ZERO);
                    return new RankingProductoDTO(fila.getProductoId(), fila.getNombre(), vendido, sugerido);
                })
                .toList();
    }

    /** Productos sin stock, para el aviso del resumen. */
    public List<String> sinStock() {
        return listar(false).stream()
                .filter(dto -> dto.stockActual().signum() <= 0)
                .map(StockDTO::nombre)
                .toList();
    }
}
