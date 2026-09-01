package ar.frezco.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Confia en los headers de identidad que inyecta el API Gateway compartido de Buildr
 * (JwtAuthFilter, ya en produccion) despues de validar el JWT. Este backend nunca ve el
 * JWT ni la contrasena: si los headers no estan, la request no paso por el Gateway.
 *
 * Frezco es tenant unico en la base de auth compartida (organizacion id fija = 1), asi que
 * ademas de identidad esto valida que la organizacion del token sea esa.
 */
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String ORGANIZACION_FREZCO = "1";

    private final ObjectMapper objectMapper;

    public GatewayAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String rol = request.getHeader("X-User-Rol");
        String organizacionId = request.getHeader("X-Organizacion-Id");

        if (userId == null || username == null || rol == null || organizacionId == null) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");
            return;
        }
        if (!ORGANIZACION_FREZCO.equals(organizacionId)) {
            responder(response, HttpServletResponse.SC_FORBIDDEN, "Organizacion no autorizada");
            return;
        }

        List<GrantedAuthority> autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
        SecurityContextHolder.getContext().setAuthentication(
                new IdentidadGateway(userId, username, autoridades));

        chain.doFilter(request, response);
    }

    private void responder(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, null);
        objectMapper.writeValue(response.getWriter(), Map.of("mensaje", mensaje));
    }

    /** Identidad ya autenticada por el Gateway: no hay credenciales que verificar aca. */
    private static final class IdentidadGateway extends AbstractAuthenticationToken {
        private final String userId;
        private final String username;

        IdentidadGateway(String userId, String username, List<GrantedAuthority> autoridades) {
            super(autoridades);
            this.userId = userId;
            this.username = username;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return userId;
        }

        @Override
        public String getName() {
            return username;
        }
    }
}
