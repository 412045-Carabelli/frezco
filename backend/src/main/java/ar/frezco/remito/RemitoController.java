package ar.frezco.remito;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/remitos")
public class RemitoController {

    private final RemitoService servicio;

    public RemitoController(RemitoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/cliente/{pedidoId}")
    public RemitoClienteDTO cliente(@PathVariable Long pedidoId) {
        return servicio.deCliente(pedidoId);
    }

    @GetMapping("/proveedor")
    public RemitoProveedorDTO proveedor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam Long proveedorId) {
        return servicio.deProveedor(desde, hasta, proveedorId);
    }
}
