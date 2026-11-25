package cl.oyl.orders;
import jakarta.persistence.Embeddable;
@Embeddable public class FulfillmentEmbeddable {
 public String recipientName; public String phone; public String email; public Long pickupLocationId; public String pickupName; public String pickupAddress; public String pickupHours; public String street; public String number; public String apartment; public String commune; public String region; public String postalCode; @jakarta.persistence.Column(length=300) public String instructions;
 protected FulfillmentEmbeddable(){}
 static FulfillmentEmbeddable from(OrderDtos.FulfillmentDetails d){FulfillmentEmbeddable f=new FulfillmentEmbeddable();if(d==null)return f;f.recipientName=d.recipientName();f.phone=d.phone();f.email=d.email();f.pickupLocationId=d.pickupLocationId();f.pickupName=d.pickupName();f.pickupAddress=d.pickupAddress();f.pickupHours=d.pickupHours();f.street=d.street();f.number=d.number();f.apartment=d.apartment();f.commune=d.commune();f.region=d.region();f.postalCode=d.postalCode();f.instructions=d.instructions();return f;}
 OrderDtos.FulfillmentDetails dto(String delivery){return new OrderDtos.FulfillmentDetails(delivery,recipientName,phone,email,pickupLocationId,pickupName,pickupAddress,pickupHours,street,number,apartment,commune,region,postalCode,instructions);}
}
