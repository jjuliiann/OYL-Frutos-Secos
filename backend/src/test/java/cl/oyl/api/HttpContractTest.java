package cl.oyl.api;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpContractTest {
    @Autowired TestRestTemplate http;

    @Test void androidJsonCheckoutAndPhotoContract() {
        var alias = "http_" + UUID.randomUUID();
        var registered = http.postForEntity("/api/auth/register", Map.of("alias", alias,
            "password", "Clave1234!", "name", "Cliente prueba", "email", "demo@example.com",
            "phone", "+56912345678", "address", "Dirección ficticia"), ApiController.Account.class);
        assertEquals(HttpStatus.OK, registered.getStatusCode());
        var user = registered.getBody();
        var login = http.postForEntity("/api/auth/login", Map.of("alias", alias, "password", "Clave1234!"), ApiController.Account.class);
        assertEquals(user.id(), login.getBody().id());
        var products = http.getForObject("/api/products", Product[].class);
        var p = products[0];
        var order = http.postForEntity("/api/orders", Map.of("userId", user.id(), "delivery", "RETIRO", "contact", "Contacto ficticio", "items", List.of(Map.of("productId", p.id, "quantity", 1))), PurchaseOrder.class);
        assertEquals(HttpStatus.OK, order.getStatusCode());
        assertEquals(p.price, order.getBody().total);
        assertNotNull(order.getBody().createdAt);
        assertEquals("PENDIENTE_VALIDACION", order.getBody().status);
        var updated = http.getForObject("/api/products", Product[].class);
        assertEquals(p.stock - 1, java.util.Arrays.stream(updated).filter(x -> x.id.equals(p.id)).findFirst().orElseThrow().stock);
        assertTrue(http.getForObject("/api/orders?userId=" + user.id(), PurchaseOrder[].class).length > 0);
    }

    @Test void productPhotoActiveStateAndInventoryRoundTrip() {
        var admin = http.postForObject("/api/auth/login", Map.of("alias", "admin", "password", "Admin1234!"), ApiController.Account.class);
        Product product = new Product("Producto HTTP", "Frutos secos", 1000, 5, "250 g");
        product.imageUri = "data:image/jpeg;base64,aW1hZ2VuX2RlX3BydWViYQ==";
        Product created = http.postForObject("/api/products?adminId=" + admin.id(), product, Product.class);
        assertEquals(product.imageUri, created.imageUri);
        created.active = false;
        var edited = http.exchange("/api/products/" + created.id + "?adminId=" + admin.id(), HttpMethod.PUT, new HttpEntity<>(created), Product.class);
        assertEquals(HttpStatus.OK, edited.getStatusCode());
        assertFalse(edited.getBody().active);
        var moved = http.postForObject("/api/products/" + created.id + "/stock?adminId=" + admin.id(), Map.of("delta", 2), Product.class);
        assertEquals(7, moved.stock);
        assertEquals(product.imageUri, moved.imageUri);
        assertEquals(3, http.getForObject("/api/locations", ApiController.PickupLocation[].class).length);
    }
}
