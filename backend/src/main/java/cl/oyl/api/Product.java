package cl.oyl.api;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Version;

@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    public String category;
    public String description;
    @Lob public String imageUri;
    public String presentation;
    public int price;
    public int stock;
    public int minStock = 5;
    public boolean active = true;
    @Version public long version;

    public Product() {}
    public Product(String name, String category, int price, int stock, String presentation) {
        this.name = name;
        this.category = category;
        this.description = "Producto natural de demostración";
        this.imageUri = "";
        this.presentation = presentation;
        this.price = price;
        this.stock = stock;
    }
}
