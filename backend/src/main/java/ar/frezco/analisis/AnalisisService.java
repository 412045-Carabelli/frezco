package ar.frezco.analisis;

import ar.frezco.config.Periodo;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Rankings comerciales por producto, cliente y zona. Todo sale de queries, sin tablas nuevas. */
@Service
@Transactional(readOnly = true)
public class AnalisisService {

    private static final int TOP = 10;

    private final AnalisisRepository repositorio;

    public AnalisisService(AnalisisRepository repositorio) {
        this.repositorio = repositorio;
    }

    public AnalisisDTO calcular(LocalDate desde, LocalDate hasta) {
        Periodo periodo = (desde == null && hasta == null)
                ? Periodo.mesDe(LocalDate.now())
                : Periodo.de(desde, hasta);

        var porProducto = repositorio.porProducto(periodo.desde(), periodo.hasta(), PageRequest.of(0, TOP))
                .stream()
                .map(fila -> new AnalisisDTO.PorProducto(
                        fila.getProductoId(), fila.getNombre(), fila.getUnidades(), fila.getImporte()))
                .toList();

        var porCliente = repositorio.porCliente(periodo.desde(), periodo.hasta(), PageRequest.of(0, TOP))
                .stream()
                .map(fila -> new AnalisisDTO.PorCliente(
                        fila.getCuentaId(), fila.getNombre(), fila.getCantidadPedidos(), fila.getImporte()))
                .toList();

        var porZona = repositorio.porZona(periodo.desde(), periodo.hasta()).stream()
                .map(fila -> new AnalisisDTO.PorZona(fila.getZona(), fila.getImporte()))
                .toList();

        return new AnalisisDTO(periodo.desde(), periodo.hasta(), porProducto, porCliente, porZona);
    }
}
