package ar.frezco.stock;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService servicio;

    public StockController(StockService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<StockDTO> listar(@RequestParam(defaultValue = "false") boolean soloConStock) {
        return servicio.listar(soloConStock);
    }

    @GetMapping("/{productoId}/movimientos")
    public List<MovimientoStockDTO> movimientos(
            @PathVariable Long productoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.movimientos(productoId, desde, hasta);
    }
}
