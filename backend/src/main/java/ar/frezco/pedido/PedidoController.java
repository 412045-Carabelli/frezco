package ar.frezco.pedido;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PedidoDTO> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long cuentaId,
            @RequestParam(defaultValue = "false") boolean incluirAnulados) {
        return servicio.listar(desde, hasta, cuentaId, incluirAnulados);
    }

    /** Va antes que /{id} para que 'precio' no se interprete como identificador. */
    @GetMapping("/precio")
    public PrecioPreviewDTO precio(@RequestParam Long productoId,
                                   @RequestParam(required = false) Long cuentaId,
                                   @RequestParam(required = false) CondicionVenta condicion,
                                   @RequestParam(required = false) BigDecimal descuentoPct) {
        return servicio.previsualizarPrecio(productoId, cuentaId, condicion, descuentoPct);
    }

    @GetMapping("/{id}")
    public PedidoDTO buscar(@PathVariable Long id) {
        return servicio.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crear(@Valid @RequestBody CrearPedidoRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(peticion));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        servicio.anular(id);
        return ResponseEntity.noContent().build();
    }
}
