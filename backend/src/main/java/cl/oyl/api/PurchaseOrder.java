package cl.oyl.api;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="purchase_orders") public class PurchaseOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long userId;
 public Instant createdAt=Instant.now();
 public String status="PENDIENTE_VALIDACION",delivery="RETIRO",contact="";
 public int total;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) public List<OrderLine> lines=new ArrayList<>();
}
