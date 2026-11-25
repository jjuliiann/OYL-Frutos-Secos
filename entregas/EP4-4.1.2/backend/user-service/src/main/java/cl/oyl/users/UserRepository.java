package cl.oyl.users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<UserAccount,Long> { Optional<UserAccount> findByAlias(String alias); }
