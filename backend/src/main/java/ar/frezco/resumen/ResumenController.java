package ar.frezco.resumen;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/resumen")
public class ResumenController {

    private final ResumenService servicio;

    public ResumenController(ResumenService servicio) {
        this.servicio = servicio;
    }

    /** Sin parametros devuelve el mes en curso. */
    @GetMapping
    public ResumenDTO resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.calcular(desde, hasta);
    }
}
