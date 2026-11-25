package cl.oyl.orders;
import java.util.List;
public final class OrderDtos {
 private OrderDtos(){}
 public record OrderItem(Long productId,int quantity){}
 public record FulfillmentDetails(String delivery,String recipientName,String phone,String email,Long pickupLocationId,String pickupName,String pickupAddress,String pickupHours,String street,String number,String apartment,String commune,String region,String postalCode,String instructions){}
 public record OrderRequest(Long userId,String delivery,String contact,List<OrderItem> items,FulfillmentDetails fulfillment){}
 public record StatusChange(String status){}
 public record Account(Long id,String alias,String name,String email,String phone,String address,String role){}
 public record ProductView(Long id,String name,String category,String description,String imageUri,String presentation,int price,int stock,int minStock,boolean active){}
 public record Quantity(int quantity){}
 public record OrderLineView(Long id,Long productId,String productName,int quantity,int unitPrice){}
 public record OrderView(Long id,Long userId,String createdAt,String status,String delivery,String contact,int total,List<OrderLineView> lines,FulfillmentDetails fulfillment){}
}
