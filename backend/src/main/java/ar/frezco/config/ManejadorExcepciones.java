package ar.frezco.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Formato de error unico para toda la API: {@code { "mensaje": "...", "campo": "..." }}.
 * Ver docs/04-api.md.
 */
@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(ExcepcionesNegocio.NoEncontrado.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(ExcepcionesNegocio.NoEncontrado e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo(e.getMessage(), null));
    }

    @ExceptionHandler(ExcepcionesNegocio.Conflicto.class)
    public ResponseEntity<Map<String, Object>> conflicto(ExcepcionesNegocio.Conflicto e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo(e.getMessage(), null));
    }

    @ExceptionHandler(ExcepcionesNegocio.DatoInvalido.class)
    public ResponseEntity<Map<String, Object>> datoInvalido(ExcepcionesNegocio.DatoInvalido e) {
        return ResponseEntity.badRequest().body(cuerpo(e.getMessage(), e.getCampo()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        FieldError primero = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String mensaje = primero != null ? primero.getDefaultMessage() : "Datos invalidos";
        String campo = primero != null ? primero.getField() : null;
        return ResponseEntity.badRequest().body(cuerpo(mensaje, campo));
    }

    private Map<String, Object> cuerpo(String mensaje, String campo) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("campo", campo);
        return cuerpo;
    }
}
