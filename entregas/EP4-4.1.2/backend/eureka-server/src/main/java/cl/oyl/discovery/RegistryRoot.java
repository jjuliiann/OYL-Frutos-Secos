package cl.oyl.discovery;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** La URL base del registro abre el panel. Las rutas de descubrimiento siguen llegando a Eureka. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RegistryRoot extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain next)
            throws IOException, ServletException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod()))
                && (path.equals("/eureka") || path.equals("/eureka/"))) {
            res.sendRedirect(req.getContextPath() + "/");
            return;
        }
        next.doFilter(req, res);
    }
}
