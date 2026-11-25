package cl.oyl.users;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration public class UserSeedData {
 @Bean CommandLineRunner seedUsers(UserRepository repo) { return args -> {
  BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
  if(repo.findByAlias("admin").isEmpty()) { UserAccount a=new UserAccount();a.alias="admin";a.name="Administrador";a.email="admin@oyl.cl";a.phone="+56912345678";a.address="Santiago, Chile";a.role="ADMIN";a.passwordHash=encoder.encode("Admin1234!");repo.save(a); }
  if(repo.findByAlias("cliente").isEmpty()) { UserAccount c=new UserAccount();c.alias="cliente";c.name="Cliente OYL";c.email="cliente@oyl.cl";c.phone="+56912345678";c.address="Santiago, Chile";c.role="CLIENT";c.passwordHash=encoder.encode("Cliente1234!");repo.save(c); }
 }; }
}
