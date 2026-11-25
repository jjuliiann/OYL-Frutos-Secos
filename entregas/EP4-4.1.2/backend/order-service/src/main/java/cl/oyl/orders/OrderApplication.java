package cl.oyl.orders;
import cl.oyl.security.SecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.client.RestClient;
@Import(SecurityConfiguration.class)
@SpringBootApplication public class OrderApplication {
 @Bean org.springframework.boot.ApplicationRunner prepareDataDirectory() { return args -> java.nio.file.Files.createDirectories(java.nio.file.Path.of("./data")); }
 public static void main(String[] args) { SpringApplication.run(OrderApplication.class, args); }
 @Bean @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder() { return RestClient.builder(); }
 @Bean RestClient productClient(RestClient.Builder builder) { return builder.baseUrl(System.getenv().getOrDefault("PRODUCTS_URL", "http://oyl-product-service")).build(); }
}
