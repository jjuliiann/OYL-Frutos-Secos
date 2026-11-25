package cl.oyl.products;

import static cl.oyl.products.ProductDtos.*;

import cl.oyl.security.TokenService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController // Expone catálogo, inventario y puntos al Gateway.
public class ProductController {
    private final ProductRepository products; // Persiste productos en H2.
    private final TokenService tokens; // Comprueba permisos de administrador.

    public ProductController(ProductRepository products, TokenService tokens) {
        this.products = products;
        this.tokens = tokens;
    }

    @GetMapping("/api/health") // Estado del servicio Productos.
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "ok", "service", "products");
    }

    @GetMapping("/api/products") // Devuelve el catálogo central.
    public List<Product> all() { return products.findAll(); }

    @DeleteMapping("/api/products/{id}")
    @Transactional
    public Product deactivate(@PathVariable Long id, @RequestParam Long adminId,
                              @RequestHeader("Authorization") String authorization) {
        requireAdmin(adminId, authorization);
        Product product = locked(id);
        product.active = false; // Baja lógica: conserva las líneas de pedidos anteriores.
        return products.save(product);
    }

    @GetMapping("/internal/products/{id}") // Consulta privada usada por Pedidos.
    public Product internal(@PathVariable Long id) { return get(id); }

    @PostMapping("/api/products") // Crea un producto solo para ADMIN.
    public Product create(
            @RequestParam Long adminId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody Product incoming
    ) {
        requireAdmin(adminId, authorization);
        validate(incoming);
        incoming.id = null; // La base asigna el identificador nuevo.
        return products.save(incoming);
    }

    @PutMapping("/api/products/{id}") // Actualiza los campos de un producto.
    public Product update(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody Product input
    ) {
        requireAdmin(adminId, authorization);
        validate(input);
        Product product = get(id);
        product.name = input.name;
        product.category = input.category;
        product.description = input.description;
        product.imageUri = input.imageUri;
        product.presentation = input.presentation;
        product.price = input.price;
        product.stock = input.stock;
        product.minStock = input.minStock;
        product.active = input.active;
        return products.save(product);
    }

    @PostMapping("/api/products/{id}/stock") // Suma o resta stock con bloqueo transaccional.
    @Transactional
    public Product stock(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody StockChange change
    ) {
        requireAdmin(adminId, authorization);
        if (change == null) throw bad("Movimiento de stock inválido");
        Product product = locked(id);
        try {
            product.stock = ProductRules.nextStock(product.stock, change.delta());
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage());
        }
        return products.save(product);
    }

    @PostMapping("/internal/products/{id}/reserve") // Descuenta stock al confirmar una compra.
    @Transactional
    public Product reserve(@PathVariable Long id, @RequestBody Quantity quantity) {
        Product product = locked(id);
        if (quantity == null || quantity.quantity() <= 0) throw bad("Cantidad inválida");
        if (!product.active || product.stock < quantity.quantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + product.name);
        }
        product.stock -= quantity.quantity();
        return products.save(product);
    }

    @PostMapping("/internal/products/{id}/release") // Devuelve stock si la compra falla.
    @Transactional
    public Product release(@PathVariable Long id, @RequestBody Quantity quantity) {
        Product product = locked(id);
        if (quantity == null || quantity.quantity() <= 0) throw bad("Cantidad inválida");
        product.stock = Math.addExact(product.stock, quantity.quantity());
        return products.save(product);
    }

    @GetMapping("/api/locations") // Mantiene los puntos de retiro ya usados por la app.
    public List<PickupLocation> locations() {
        return List.of(
                new PickupLocation(1, "Punto de retiro Santiago Centro", "Santiago Centro, Chile", "Horario: 09:00–18:00", -33.4489, -70.6693),
                new PickupLocation(2, "Punto de retiro Providencia", "Providencia, Santiago, Chile", "Horario: 09:00–18:00", -33.4314, -70.6093),
                new PickupLocation(3, "Punto de retiro Peñalolén", "Peñalolén, Santiago, Chile", "Horario: 09:00–18:00", -33.4858, -70.5525)
        );
    }

    private Product get(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private Product locked(Long id) {
        return products.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    // Exige token ADMIN y que el id coincida con el dueño del token.
    private void requireAdmin(Long id, String authorization) {
        try {
            TokenService.Identity user = tokens.verify(authorization);
            if (user.userId() != id || !"ADMIN".equals(user.role())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administrador");
            }
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, error.getMessage());
        }
    }

    private static void validate(Product product) {
        try { ProductRules.validate(product); }
        catch (IllegalArgumentException error) { throw bad(error.getMessage()); }
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
