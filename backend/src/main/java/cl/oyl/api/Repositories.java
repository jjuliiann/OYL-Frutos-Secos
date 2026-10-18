package cl.oyl.api;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
interface Products extends JpaRepository<Product,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id") Optional<Product> locked(@Param("id") Long id);
}
interface Users extends JpaRepository<UserAccount,Long>{ Optional<UserAccount> findByAlias(String alias); }
interface Orders extends JpaRepository<PurchaseOrder,Long>{ List<PurchaseOrder> findByUserIdOrderByCreatedAtDesc(Long userId); }
