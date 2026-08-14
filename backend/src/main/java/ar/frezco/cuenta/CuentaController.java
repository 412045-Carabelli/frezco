package ar.frezco.cuenta;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService servicio;

    public CuentaController(CuentaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<CuentaDTO> listar(@RequestParam(required = false) TipoCuenta tipo,
                                  @RequestParam(required = false) String busqueda,
                                  @RequestParam(defaultValue = "true") boolean soloActivas) {
        return servicio.listar(tipo, busqueda, soloActivas);
    }

    @GetMapping("/{id}")
    public CuentaDTO buscar(@PathVariable Long id) {
        return servicio.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CuentaDTO> crear(@Valid @RequestBody CuentaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(dto));
    }

    @PutMapping("/{id}")
    public CuentaDTO actualizar(@PathVariable Long id, @Valid @RequestBody CuentaDTO dto) {
        return servicio.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        servicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
