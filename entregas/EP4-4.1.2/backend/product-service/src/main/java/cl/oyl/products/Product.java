package cl.oyl.products;
import jakarta.persistence.*;
@Entity @Table(name="products")
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=100) public String name;
 @Column(nullable=false,length=60) public String category="Frutos secos";
 @Column(length=500) public String description="";
 // El administrador puede guardar una foto comprimida como data URI en el servicio.
 // El límite ampliado evita truncar el Base64 de una imagen optimizada para móvil.
 @Column(length=1000000) public String imageUri="";
 @Column(nullable=false,length=40) public String presentation="250 g";
 @Column(nullable=false) public int price;
 @Column(nullable=false) public int stock;
 @Column(nullable=false) public int minStock=5;
 @Column(nullable=false) public boolean active=true;
 protected Product() {}
}
