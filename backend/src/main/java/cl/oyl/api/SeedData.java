package cl.oyl.api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner seed(Products products, Users users) {
        return args -> {
            if (users.findByAlias("admin").isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.alias = "admin";
                admin.name = "Administrador";
                admin.email = "admin@admin.cl";
                admin.phone = "+56 9 1234 1234";
                admin.address = "Santiago";
                admin.role = "ADMIN";
                admin.passwordHash = new BCryptPasswordEncoder().encode("Admin1234!");
                users.save(admin);
            }
            if (products.count() == 0) {
                products.save(new Product("Almendras", "Frutos secos", 2990, 25, "250 g"));
                products.save(new Product("Nueces", "Frutos secos", 3490, 18, "250 g"));
                products.save(new Product("Mix Premium", "Frutos secos", 3990, 40, "250 g"));
                products.save(new Product("Pasas", "Deshidratados", 2490, 30, "250 g"));
                products.save(new Product("Semillas de chía", "Semillas", 2290, 22, "250 g"));
                products.save(new Product("Cranberries", "Deshidratados", 2890, 16, "250 g"));
                products.save(new Product("Avellanas", "Frutos secos", 3290, 20, "250 g"));
            }
        };
    }
}
