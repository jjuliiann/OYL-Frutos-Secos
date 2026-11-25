package cl.oyl.users;

import cl.oyl.security.SecurityConfiguration; // Importa el bean de tokens compartido.
import org.springframework.boot.SpringApplication; // Permite iniciar Spring Boot.
import org.springframework.boot.autoconfigure.SpringBootApplication; // Configura este microservicio.
import org.springframework.context.annotation.Bean; // Registra componentes de Spring.
import org.springframework.context.annotation.Import; // Carga seguridad compartida.

@Import(SecurityConfiguration.class) // Hace disponible TokenService en Usuarios.
@SpringBootApplication // Activa configuración, componentes y auto-configuración.
public class UserApplication {

    @Bean // Crea la carpeta donde H2 guardará sus archivos.
    org.springframework.boot.ApplicationRunner prepareDataDirectory() {
        return args -> java.nio.file.Files.createDirectories(
                java.nio.file.Path.of("./data")
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args); // Inicia el servidor Usuarios.
    }
}
