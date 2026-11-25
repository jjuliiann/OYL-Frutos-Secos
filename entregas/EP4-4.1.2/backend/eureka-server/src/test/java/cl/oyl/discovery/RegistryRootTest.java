package cl.oyl.discovery;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
class RegistryRootTest {
    @Test void enlaceDeNavegadorAbreElPanel() throws Exception {
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        new RegistryRoot().doFilter(new MockHttpServletRequest("GET","/eureka/"),response,chain);
        assertEquals(302,response.getStatus()); assertEquals("/",response.getRedirectedUrl());
        assertNull(chain.getRequest());
    }
    @Test void unRegistroDeInstanciaLlegaAEureka() throws Exception {
        var chain = new MockFilterChain(); var request = new MockHttpServletRequest("POST","/eureka/apps/OYL-USER-SERVICE");
        new RegistryRoot().doFilter(request,new MockHttpServletResponse(),chain);
        assertSame(request,chain.getRequest());
    }
}
