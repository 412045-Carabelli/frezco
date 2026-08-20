package ar.frezco.analisis;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analisis")
public class AnalisisController {

    private final AnalisisService servicio;

    public AnalisisController(AnalisisService servicio) {
        this.servicio = servicio;
    }

    /** Sin parametros devuelve el mes en curso. */
    @GetMapping
    public AnalisisDTO analisis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.calcular(desde, hasta);
    }
}
