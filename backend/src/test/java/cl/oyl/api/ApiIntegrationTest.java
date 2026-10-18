package cl.oyl.api;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class ApiIntegrationTest {
    @Autowired ApiController api;

    private ApiController.Account customer() {
        String alias = "test_" + UUID.randomUUID().toString().substring(0, 10);
        return api.register(new ApiController.Register(alias, "password123", "Usuario de prueba", "ejemplo@demo.cl", "", ""));
    }

    @Test
    void healthAndLocationsRespond() {
        assertEquals("ok", api.health().get("status"));
        assertFalse(api.locations().isEmpty());
    }

    @Test
    void registerLoginAndProfile() {
        var user = customer();
        assertEquals(user.id(), api.login(new ApiController.Credentials(user.alias(), "password123")).id());
        assertThrows(ResponseStatusException.class, () -> api.login(new ApiController.Credentials(user.alias(), "incorrect")));
        var updated = api.updateProfile(user.id(), new ApiController.ProfileUpdate("Nombre actualizado", "test@demo.cl", "123", "Prueba"));
        assertEquals("Nombre actualizado", updated.name());
    }

    @Test
    void checkoutUpdatesStockAndRejectsOverselling() {
        var user = customer();
        var p = api.products().get(0);
        int before = p.stock;
        var placed = api.order(new ApiController.OrderRequest(user.id(), "RETIRO", "Contacto", List.of(new ApiController.OrderItem(p.id, 1))));
        assertEquals(p.price, placed.total);
        assertEquals(before - 1, api.products().stream().filter(x -> x.id.equals(p.id)).findFirst().orElseThrow().stock);
        assertThrows(ResponseStatusException.class, () -> api.order(new ApiController.OrderRequest(user.id(), "RETIRO", "Contacto", List.of(new ApiController.OrderItem(p.id, 999999)))));
        assertEquals(before - 1, api.products().stream().filter(x -> x.id.equals(p.id)).findFirst().orElseThrow().stock);
        assertFalse(api.orders(user.id()).isEmpty());
    }

    @Test
    void administratorOnlyCanChangeInventory() {
        var user = customer();
        var p = api.products().get(0);
        assertThrows(ResponseStatusException.class, () -> api.stock(p.id, user.id(), new ApiController.StockChange(1)));
        var admin = api.login(new ApiController.Credentials("admin", "Admin1234!"));
        assertEquals(p.stock + 2, api.stock(p.id, admin.id(), new ApiController.StockChange(2)).stock);
    }
}
