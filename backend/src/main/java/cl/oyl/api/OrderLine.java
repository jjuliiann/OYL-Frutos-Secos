package cl.oyl.api;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity public class OrderLine {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long productId;
 public String productName;
 public int quantity,unitPrice;
 @ManyToOne @JoinColumn(name="order_id") @JsonIgnore public PurchaseOrder order;
}
