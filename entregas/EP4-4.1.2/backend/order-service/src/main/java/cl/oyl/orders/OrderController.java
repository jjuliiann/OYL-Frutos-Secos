package cl.oyl.orders;

import static cl.oyl.orders.OrderDtos.*;

import cl.oyl.security.TokenService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController // Recibe y consulta pedidos desde el Gateway.
public class OrderController {
    private final OrderRepository orders; // Persiste pedidos y líneas en H2.
    private final RestClient products; // Llama a Productos para validar y reservar stock.
    private final TokenService tokens; // Valida usuario y rol de la sesión.

    public OrderController(OrderRepository orders, RestClient productClient, TokenService tokens) {
        this.orders = orders;
        this.products = productClient;
        this.tokens = tokens;
    }

    @GetMapping("/api/health") // Estado del servicio Pedidos.
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "orders");
    }

    @PostMapping("/api/orders") // Valida compra, reserva inventario y guarda el pedido.
    @Transactional
    public OrderView create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OrderRequest request
    ) {
        if (request == null || request.userId() == null
                || request.items() == null || request.items().isEmpty()) {
            throw bad("El pedido no contiene productos");
        }

        TokenService.Identity user = identity(authorization);
        if (user.userId() != request.userId() || !"CLIENT".equals(user.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo clientes pueden comprar");
        }

        String delivery = request.fulfillment() != null && request.fulfillment().delivery() != null
                ? request.fulfillment().delivery() : request.delivery();
        if (!OrderPolicy.validDelivery(delivery)) {
            throw bad("Modalidad de entrega inválida");
        }
        if (!OrderPolicy.validFulfillment(delivery, request.fulfillment())) {
            throw bad("Revisa los datos de contacto y entrega del pedido");
        }

        TreeMap<Long, Integer> quantities;
        try {
            quantities = OrderPolicy.quantities(request.items()); // Combina productos repetidos.
        } catch (IllegalArgumentException error) {
            throw bad(error.getMessage());
        }

        PurchaseOrder order = new PurchaseOrder();
        order.userId = user.userId();
        order.createdAt = OffsetDateTime.now().toString();
        order.delivery = delivery;
        order.status = "PENDIENTE_VALIDACION";
        order.contact = request.contact() == null ? "" : request.contact().trim();
        order.fulfillment = FulfillmentEmbeddable.from(request.fulfillment());
        if (request.fulfillment() != null) {
            String name = request.fulfillment().recipientName();
            String phone = request.fulfillment().phone();
            if (name != null && !name.isBlank()) {
                order.contact = name.trim() + (phone == null || phone.isBlank() ? "" : " · " + phone.trim());
            }
        }

        List<Map.Entry<Long, Integer>> reserved = new ArrayList<>(); // Permite devolver stock si hay un error.
        long total = 0;
        try {
            for (var item : quantities.entrySet()) {
                ProductView product = products.post()
                        .uri("/internal/products/{id}/reserve", item.getKey())
                        .body(new Quantity(item.getValue()))
                        .retrieve()
                        .body(ProductView.class);
                if (product == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY, "No se recibió confirmación del inventario");
                }

                reserved.add(item);
                total = Math.addExact(total, Math.multiplyExact((long) product.price(), item.getValue()));

                OrderLine line = new OrderLine();
                line.order = order;
                line.productId = product.id();
                line.productName = product.name();
                line.quantity = item.getValue();
                line.unitPrice = product.price();
                order.lines.add(line);
            }

            if (total > Integer.MAX_VALUE) {
                throw bad("Total del pedido fuera de rango");
            }
            order.total = (int) total;
            return view(orders.saveAndFlush(order));
        } catch (RuntimeException error) {
            releaseReservedStock(reserved); // Compensa reservas si no se guardó el pedido.
            if (error instanceof org.springframework.web.client.RestClientResponseException upstream) {
                if (upstream.getStatusCode().value() == 409)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente o producto inactivo");
                if (upstream.getStatusCode().value() == 404)
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Inventario no disponible");
            }
            if (error instanceof org.springframework.web.client.ResourceAccessException)
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Inventario no disponible");
            throw error;
        }
    }

    @GetMapping("/api/orders") // Cliente ve sus pedidos; ADMIN puede consultar todos.
    public List<OrderView> list(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Long userId
    ) {
        TokenService.Identity user = identity(authorization);
        if (user.userId() != userId && !"ADMIN".equals(user.role())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No puedes consultar pedidos de otro usuario");
        }

        List<PurchaseOrder> found = "ADMIN".equals(user.role())
                ? orders.findAllByOrderByIdDesc()
                : orders.findByUserIdOrderByIdDesc(userId);
        return found.stream().map(OrderController::view).toList();
    }

    @PatchMapping("/api/orders/{id}/status") // ADMIN actualiza el estado de un pedido.
    public OrderView status(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody StatusChange change
    ) {
        requireAdmin(adminId, authorization);
        if (change == null || !OrderPolicy.validStatus(change.status())) {
            throw bad("Estado de pedido inválido");
        }

        PurchaseOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido inexistente"));
        order.status = change.status();
        return view(orders.save(order));
    }

    private void releaseReservedStock(List<Map.Entry<Long, Integer>> reserved) {
        for (var item : reserved) {
            try {
                products.post()
                        .uri("/internal/products/{id}/release", item.getKey())
                        .body(new Quantity(item.getValue()))
                        .retrieve()
                        .body(ProductView.class);
            } catch (RuntimeException ignored) {
                // No reemplaza la excepción original que provocó la compensación.
            }
        }
    }

    private TokenService.Identity identity(String authorization) {
        try {
            return tokens.verify(authorization);
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, error.getMessage());
        }
    }

    private void requireAdmin(Long id, String authorization) {
        TokenService.Identity user = identity(authorization);
        if (user.userId() != id || !"ADMIN".equals(user.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administrador");
        }
    }

    // Convierte la entidad guardada al JSON que consume Retrofit.
    private static OrderView view(PurchaseOrder order) {
        List<OrderLineView> lines = order.lines.stream()
                .map(line -> new OrderLineView(
                        line.id, line.productId, line.productName, line.quantity, line.unitPrice))
                .toList();
        return new OrderView(
                order.id,
                order.userId,
                order.createdAt,
                order.status,
                order.delivery,
                order.contact,
                order.total,
                lines,
                order.fulfillment == null ? null : order.fulfillment.dto(order.delivery)
        );
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
