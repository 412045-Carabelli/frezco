package ar.frezco.cuentacorriente;

import ar.frezco.cuenta.TipoCuenta;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas-corrientes")
public class CuentaCorrienteController {

    private final CuentaCorrienteService servicio;

    public CuentaCorrienteController(CuentaCorrienteService servicio) {
        this.servicio = servicio;
    }

    /** Va antes que /{cuentaId} para que 'saldos' no se interprete como identificador. */
    @GetMapping("/saldos")
    public List<SaldoDTO> saldos(@RequestParam(required = false) TipoCuenta tipo,
                                 @RequestParam(defaultValue = "false") boolean soloConSaldo) {
        return servicio.saldos(tipo, soloConSaldo);
    }

    @GetMapping("/{cuentaId}")
    public CuentaCorrienteDTO consultar(
            @PathVariable Long cuentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.consultar(cuentaId, desde, hasta);
    }
}
