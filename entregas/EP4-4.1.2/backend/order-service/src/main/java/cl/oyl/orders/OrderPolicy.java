package cl.oyl.orders;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
/** Reglas puras de pedido para hacer pruebas rápidas y deterministas. */
public final class OrderPolicy {
 private static final Set<String> STATES=Set.of("PENDIENTE_VALIDACION","CONFIRMADO","PREPARADO","ENTREGADO");
 private OrderPolicy(){}
 public static TreeMap<Long,Integer> quantities(List<OrderDtos.OrderItem> items){
  if(items==null||items.isEmpty())throw new IllegalArgumentException("El pedido no contiene productos");
  TreeMap<Long,Integer> result=new TreeMap<>();
  try{for(var item:items){if(item==null||item.productId()==null||item.quantity()<=0)throw new IllegalArgumentException("Producto o cantidad inválida");result.merge(item.productId(),item.quantity(),Math::addExact);}}
  catch(ArithmeticException e){throw new IllegalArgumentException("Cantidad fuera de rango",e);}
  return result;
 }
 public static boolean validFulfillment(String delivery,OrderDtos.FulfillmentDetails f){
  if(f==null||f.recipientName()==null||f.recipientName().trim().length()<3||f.phone()==null)return false;
  String digits=f.phone().replaceAll("\\D","");if(digits.length()<9||digits.length()>15)return false;
  if("RETIRO".equals(delivery))return f.pickupLocationId()!=null&&f.pickupLocationId()>0;
  if("ENTREGA".equals(delivery))return text(f.street(),2,120)&&text(f.number(),1,20)&&text(f.commune(),2,80)&&text(f.region(),2,80);
  return false;
 }
 private static boolean text(String v,int min,int max){return v!=null&&v.trim().length()>=min&&v.trim().length()<=max;}
 public static boolean validDelivery(String delivery){return "RETIRO".equals(delivery)||"ENTREGA".equals(delivery);}
 public static boolean validStatus(String state){return state!=null && STATES.contains(state);}
}
