package cl.oyl.orders;
import jakarta.persistence.*;
@Entity @Table(name="order_lines")
public class OrderLine {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id") public PurchaseOrder order;
 @Column(nullable=false) public Long productId;
 @Column(nullable=false,length=100) public String productName;
 @Column(nullable=false) public int quantity;
 @Column(nullable=false) public int unitPrice;
 protected OrderLine(){}
}
