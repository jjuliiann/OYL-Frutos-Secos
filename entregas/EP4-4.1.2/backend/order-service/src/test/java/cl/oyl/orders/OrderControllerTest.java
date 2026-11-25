package cl.oyl.orders;
import cl.oyl.security.TokenService;
import static cl.oyl.orders.OrderDtos.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

class OrderControllerTest {
    final OrderRepository repo=mock(OrderRepository.class);
    final TokenService jwt=new TokenService("A-reliable-unit-test-key-for-oyl-tests",300);
    final RestClient.Builder builder=RestClient.builder().baseUrl("http://products");
    final MockRestServiceServer server=MockRestServiceServer.bindTo(builder).build();
    final OrderController c=new OrderController(repo,builder.build(),jwt);
    final String client="Bearer "+jwt.issue(4,"CLIENT"), admin="Bearer "+jwt.issue(6,"ADMIN");
    final FulfillmentDetails f=new FulfillmentDetails("RETIRO","Ana Gomez","912345678","",1L,"Tienda","Centro","09:00","","","","","","","");
    OrderRequest cart(OrderItem... items){return new OrderRequest(4L,"RETIRO","Ana",List.of(items),f);}
    void reserve(long id, int price) {
        server.expect(requestTo("http://products/internal/products/"+id+"/reserve")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":"+id+",\"name\":\"Nuez\",\"price\":"+price+",\"active\":true}",MediaType.APPLICATION_JSON));
    }
    @Test void compraConservaPrecioYDetalleAunqueCambieElCatalogo(){
        reserve(3,1500);when(repo.saveAndFlush(any())).thenAnswer(i->{PurchaseOrder o=i.getArgument(0);o.id=2L;o.lines.get(0).id=1L;return o;});
        var r=c.create(client,cart(new OrderItem(3L,1),new OrderItem(3L,1)));
        assertEquals(3000,r.total());assertEquals(2,r.lines().get(0).quantity());assertEquals("Ana Gomez",r.fulfillment().recipientName());server.verify();
    }
    @Test void siFallaElSegundoProductoDevuelvePrimeraReserva(){
        reserve(3,1500);
        server.expect(requestTo("http://products/internal/products/4/reserve")).andRespond(withStatus(HttpStatus.CONFLICT));
        server.expect(requestTo("http://products/internal/products/3/release")).andExpect(method(HttpMethod.POST)).andRespond(withSuccess("{}",MediaType.APPLICATION_JSON));
        var e=assertThrows(ResponseStatusException.class,()->c.create(client,cart(new OrderItem(3L,1),new OrderItem(4L,40))));
        assertEquals(409,e.getStatusCode().value());verify(repo,never()).saveAndFlush(any());server.verify();
    }
    @Test void rechazaComprasYAccesosSinRol(){
        assertThrows(ResponseStatusException.class,()->c.create(admin,cart(new OrderItem(3L,1))));
        assertThrows(ResponseStatusException.class,()->c.create(client,null));
        assertThrows(ResponseStatusException.class,()->c.create(client,cart(new OrderItem(3L,0))));
        assertThrows(ResponseStatusException.class,()->c.list(client,6L));
        assertThrows(ResponseStatusException.class,()->c.list("Bearer roto",4L));
        assertThrows(ResponseStatusException.class,()->c.status(9L,4L,client,new StatusChange("ENTREGADO")));
        verifyNoInteractions(repo);server.verify();
    }
    @Test void soloElAdminCambiaEstadoYElHistorialTieneAlcancePorCuenta(){
        var o=new PurchaseOrder();o.id=9L;o.userId=4L;o.delivery="RETIRO";
        when(repo.findById(9L)).thenReturn(Optional.of(o));when(repo.save(o)).thenReturn(o);
        assertEquals("CONFIRMADO",c.status(9L,6L,admin,new StatusChange("CONFIRMADO")).status());
        when(repo.findByUserIdOrderByIdDesc(4L)).thenReturn(List.of(o));
        when(repo.findAllByOrderByIdDesc()).thenReturn(List.of(o));
        assertEquals(9L,c.list(client,4L).get(0).id());assertEquals(1,c.list(admin,6L).size());
        assertThrows(ResponseStatusException.class,()->c.status(9L,6L,admin,new StatusChange(null)));
    }
}
