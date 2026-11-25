package cl.oyl.products;
public final class ProductDtos { private ProductDtos(){} public record StockChange(int delta){} public record Quantity(int quantity){} public record Account(Long id,String alias,String name,String email,String phone,String address,String role){} public record PickupLocation(long id,String name,String address,String hours,double latitude,double longitude){} }
