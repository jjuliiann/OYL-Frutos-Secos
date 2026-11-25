package cl.oyl.products;
import cl.oyl.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {
    final ProductRepository repo = mock(ProductRepository.class);
    final TokenService tokens = new TokenService("A-reliable-unit-test-key-for-oyl-tests", 300);
    final ProductController c = new ProductController(repo,tokens);
    final String admin = "Bearer "+tokens.issue(8,"ADMIN");
    Product nueces() { var p=new Product(); p.id=3L; p.name="Nueces"; p.price=2300;p.stock=3; return p; }

    @Test void laBajaEsLogicaYNoBorraHistorial() {
        var p=nueces();when(repo.findByIdForUpdate(3L)).thenReturn(Optional.of(p));
        when(repo.save(p)).thenReturn(p);assertFalse(c.deactivate(3L,8L,admin).active);
        assertEquals(3,p.stock);verify(repo,never()).delete(p);
        var input=nueces();input.price=2400;when(repo.findById(3L)).thenReturn(Optional.of(p));
        assertTrue(c.update(3L,8L,admin,input).active);assertEquals(2400,p.price);
    }
    @Test void altaGeneraIdYPermisosSeValidanAntesDeEscribir() {
        var p=nueces();when(repo.save(p)).thenReturn(p);
        assertNull(c.create(8L,admin,p).id);
        assertThrows(ResponseStatusException.class,()->c.create(8L,"Bearer "+tokens.issue(8,"CLIENT"),p));
        assertThrows(ResponseStatusException.class,()->c.deactivate(3L,9L,admin));
        assertThrows(ResponseStatusException.class,()->c.stock(3L,8L,"Bearer roto",new ProductDtos.StockChange(1)));
        verify(repo,times(1)).save(p);
    }
    @Test void reservaYCompensacionRespetanElInventario() {
        var p=nueces();when(repo.findByIdForUpdate(3L)).thenReturn(Optional.of(p));when(repo.save(p)).thenReturn(p);
        assertEquals(1,c.reserve(3L,new ProductDtos.Quantity(2)).stock);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->c.reserve(3L,new ProductDtos.Quantity(2))).getStatusCode().value());
        assertEquals(3,c.release(3L,new ProductDtos.Quantity(2)).stock);
        assertEquals(5,c.stock(3L,8L,admin,new ProductDtos.StockChange(2)).stock);
        assertThrows(ResponseStatusException.class,()->c.stock(3L,8L,admin,new ProductDtos.StockChange(-6)));
        assertThrows(ResponseStatusException.class,()->c.internal(999L));
    }
    @Test void inicializarDosVecesNoDuplicaYNoReponeLoVendido() throws Exception {
        when(repo.count()).thenReturn(0L,7L);var seed=new ProductSeedData().seedProducts(repo);
        seed.run();seed.run();verify(repo,times(7)).save(any());
        assertEquals(3,c.locations().size()); assertEquals("products",c.health().get("service"));
    }
}
