package cl.oyl.orders;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="purchase_orders")
public class PurchaseOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long userId;
 @Column(nullable=false,length=50) public String createdAt;
 @Column(nullable=false,length=32) public String status="PENDIENTE_VALIDACION";
 @Column(nullable=false,length=20) public String delivery;
 @Column(nullable=false,length=240) public String contact;
 @Column(nullable=false) public int total;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) public List<OrderLine> lines=new ArrayList<>();
 @Embedded public FulfillmentEmbeddable fulfillment;
 protected PurchaseOrder(){}
}
