package cl.oyl.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Permite compartir esta configuración entre microservicios.
public class SecurityConfiguration {

    @Bean // Registra el servicio que emite y valida tokens.
    TokenService tokenService() {
        String secret = System.getenv().getOrDefault(
                "OYL_JWT_SECRET",
                "OYL-dev-secret-change-before-publishing-2026-11"
        ); // Lee la clave común o usa una clave académica local.
        return new TokenService(secret, 8 * 60 * 60); // El token dura ocho horas.
    }
}
