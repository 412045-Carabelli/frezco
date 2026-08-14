package ar.frezco.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository repositorioContexto =
            new HttpSessionSecurityContextRepository();

    public AutenticacionController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public record LoginRequest(@NotBlank String usuario, @NotBlank String clave) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest peticion,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(peticion.usuario(), peticion.clave()));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
            repositorioContexto.saveContext(contexto, request, response);

            return ResponseEntity.ok(Map.of("usuario", autenticacion.getName()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario o clave incorrectos"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /**
     * Publico a proposito: el frontend lo consulta al arrancar para decidir si muestra el
     * login o la aplicacion.
     */
    @GetMapping("/sesion")
    public Map<String, Object> sesion(Authentication autenticacion) {
        boolean autenticado = autenticacion != null && autenticacion.isAuthenticated();
        return Map.of(
                "autenticado", autenticado,
                "usuario", autenticado ? autenticacion.getName() : "");
    }
}
