package cl.oyl.orders;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderRepository extends JpaRepository<PurchaseOrder,Long> { List<PurchaseOrder> findAllByOrderByIdDesc(); List<PurchaseOrder> findByUserIdOrderByIdDesc(Long userId); }
