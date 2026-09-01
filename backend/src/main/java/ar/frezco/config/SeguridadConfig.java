package ar.frezco.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Autenticacion delegada al API Gateway compartido de Buildr: el Gateway valida el JWT y
 * este backend solo confia en los headers de identidad que inyecta (ver GatewayAuthFilter).
 * Sin sesion propia: cada request se autentica por sus headers.
 */
@Configuration
public class SeguridadConfig {

    private final String origenesPermitidos;
    private final ObjectMapper objectMapper;

    public SeguridadConfig(@Value("${app.cors.origenes}") String origenesPermitidos,
                           ObjectMapper objectMapper) {
        this.origenesPermitidos = origenesPermitidos;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Sin CSRF ni cookie de sesion: la identidad viaja en headers del Gateway en
                // cada request, no hay estado de sesion que proteger.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new GatewayAuthFilter(objectMapper), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", configuracion);
        return fuente;
    }
}
