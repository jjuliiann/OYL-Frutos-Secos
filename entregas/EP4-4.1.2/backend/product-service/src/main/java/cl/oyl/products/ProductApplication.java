package cl.oyl.products;
import cl.oyl.security.SecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
@Import(SecurityConfiguration.class)
@SpringBootApplication public class ProductApplication {
 @Bean org.springframework.boot.ApplicationRunner prepareDataDirectory() { return args -> java.nio.file.Files.createDirectories(java.nio.file.Path.of("./data")); }
 public static void main(String[] args) { SpringApplication.run(ProductApplication.class, args); }
}
