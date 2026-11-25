package cl.oyl.products;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class ProductSeedData {
 @Bean CommandLineRunner seedProducts(ProductRepository repo){return args->{if(repo.count()!=0)return;String[] names={"Almendras","Nueces","Mix Premium","Pasas","Semillas de chía","Cranberries","Avellanas"};String[] cats={"Frutos secos","Frutos secos","Mix","Deshidratados","Semillas","Deshidratados","Frutos secos"};int[] prices={2990,3490,3990,2490,2290,2890,3290};int[] stock={25,18,40,30,22,16,20};for(int i=0;i<names.length;i++){Product p=new Product();p.name=names[i];p.category=cats[i];p.description=names[i]+" seleccionadas para tu alimentación diaria.";p.imageUri="";p.presentation="250 g";p.price=prices[i];p.stock=stock[i];p.minStock=5;p.active=true;repo.save(p);}};}
}
