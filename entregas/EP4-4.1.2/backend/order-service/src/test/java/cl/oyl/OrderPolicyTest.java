package cl.oyl;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import cl.oyl.orders.OrderDtos.OrderItem;
import cl.oyl.orders.OrderPolicy;
import org.junit.jupiter.api.Test;
class OrderPolicyTest {
 @Test void mergesRepeatedProductsInStableOrder(){var q=OrderPolicy.quantities(List.of(new OrderItem(2L,1),new OrderItem(1L,3),new OrderItem(2L,2)));assertEquals(List.of(1L,2L),q.keySet().stream().toList());assertEquals(3,q.get(2L));}
 @Test void rejectsInvalidOrderItems(){assertThrows(IllegalArgumentException.class,()->OrderPolicy.quantities(List.of()));assertThrows(IllegalArgumentException.class,()->OrderPolicy.quantities(List.of(new OrderItem(1L,0))));}
 @Test void requiresContactAndDeliveryDetails(){
  var pickup=new cl.oyl.orders.OrderDtos.FulfillmentDetails("RETIRO","Julia Perez","+56912345678","",1L,"Local","Santiago","09-18","","","","","","","");
  assertTrue(OrderPolicy.validFulfillment("RETIRO",pickup));
  var invalidShipping=new cl.oyl.orders.OrderDtos.FulfillmentDetails("ENTREGA","Julia Perez","+56912345678","",null,"","","","","","","","","","");
  assertFalse(OrderPolicy.validFulfillment("ENTREGA",invalidShipping));
 }
 @Test void validatesDeliveryAndStatus(){assertTrue(OrderPolicy.validDelivery("RETIRO"));assertFalse(OrderPolicy.validDelivery("OTRO"));assertTrue(OrderPolicy.validStatus("PREPARADO"));assertFalse(OrderPolicy.validStatus("ELIMINADO"));}
}
