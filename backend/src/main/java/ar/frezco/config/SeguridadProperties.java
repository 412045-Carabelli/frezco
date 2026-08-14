package ar.frezco.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciales del usuario unico. No hay tabla de usuarios ni roles: llegan por variables
 * de entorno (APP_USER / APP_PASSWORD).
 */
@ConfigurationProperties(prefix = "app.seguridad")
public record SeguridadProperties(String usuario, String clave) {
}
