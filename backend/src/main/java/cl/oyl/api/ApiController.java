package cl.oyl.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final Products products;
    private final Users users;
    private final Orders orders;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public ApiController(Products products, Users users, Orders orders) {
        this.products = products;
        this.users = users;
        this.orders = orders;
    }

    public record Credentials(String alias, String password) {}
    public record Register(String alias, String password, String name, String email, String phone, String address) {}
    public record ProfileUpdate(String name, String email, String phone, String address) {}
    public record Account(Long id, String alias, String name, String email, String phone, String address, String role) {}
    public record OrderItem(Long productId, int quantity) {}
    public record OrderRequest(Long userId, String delivery, String contact, List<OrderItem> items) {}
    public record StockChange(int delta) {}
    public record StatusChange(String status) {}
    public record PickupLocation(long id, String name, String address, String hours, double latitude, double longitude) {}

    private Account view(UserAccount u) {
        return new Account(u.id, u.alias, u.name, u.email, u.phone, u.address, u.role);
    }

    private UserAccount account(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario inexistente"));
    }

    private void requireAdmin(Long id) {
        if (!"ADMIN".equals(account(id).role))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administrador");
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "ok"); }

    @GetMapping("/locations")
    public List<PickupLocation> locations() {
        return List.of(
            new PickupLocation(1, "Punto de prueba Santiago Centro", "Santiago Centro, Chile", "Referencia: 09:00–18:00", -33.4489, -70.6693),
            new PickupLocation(2, "Punto de prueba Providencia", "Providencia, Santiago, Chile", "Referencia: 09:00–18:00", -33.4314, -70.6093),
            new PickupLocation(3, "Punto de prueba Peñalolén", "Peñalolén, Santiago, Chile", "Referencia: 09:00–18:00", -33.4858, -70.5525)
        );
    }

    @PostMapping("/auth/register")
    public Account register(@RequestBody Register r) {
        if (r == null || r.alias() == null || r.alias().isBlank() || r.name() == null || r.name().isBlank()
                || r.password() == null || r.password().length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre, alias y contraseña de 8 caracteres obligatorios");
        String alias = r.alias().trim();
        if (users.findByAlias(alias).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alias ocupado");
        UserAccount u = new UserAccount();
        u.alias = alias;
        u.name = r.name().trim();
        u.email = r.email();
        u.phone = r.phone();
        u.address = r.address();
        u.passwordHash = encoder.encode(r.password());
        return view(users.save(u));
    }

    @PostMapping("/auth/login")
    public Account login(@RequestBody Credentials c) {
        if (c == null || c.alias() == null || c.password() == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        UserAccount u = users.findByAlias(c.alias().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if (!encoder.matches(c.password(), u.passwordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        return view(u);
    }

    // Autenticación deliberadamente simplificada: SOLO MVP con identidades ficticias.
    @PutMapping("/users/{id}")
    public Account updateProfile(@PathVariable Long id, @RequestBody ProfileUpdate p) {
        UserAccount u = account(id);
        if (p == null || p.name() == null || p.name().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        u.name = p.name().trim();
        u.email = p.email();
        u.phone = p.phone();
        u.address = p.address();
        return view(users.save(u));
    }

    @GetMapping("/products")
    public List<Product> products() { return products.findAll(); }

    private void checkProduct(Product p) {
        if (p == null || p.name == null || p.name.isBlank() || p.price < 0 || p.stock < 0 || p.minStock < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inválido");
    }

    @PostMapping("/products")
    public Product create(@RequestParam Long adminId, @RequestBody Product p) {
        requireAdmin(adminId);
        checkProduct(p);
        p.id = null;
        return products.save(p);
    }

    @PutMapping("/products/{id}")
    public Product edit(@PathVariable Long id, @RequestParam Long adminId, @RequestBody Product incoming) {
        requireAdmin(adminId);
        checkProduct(incoming);
        Product p = products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        p.name = incoming.name;
        p.category = incoming.category;
        p.description = incoming.description;
        p.imageUri = incoming.imageUri;
        p.presentation = incoming.presentation;
        p.price = incoming.price;
        p.stock = incoming.stock;
        p.minStock = incoming.minStock;
        p.active = incoming.active;
        return products.save(p);
    }

    @PostMapping("/products/{id}/stock")
    @Transactional
    public Product stock(@PathVariable Long id, @RequestParam Long adminId, @RequestBody StockChange change) {
        requireAdmin(adminId);
        Product p = products.locked(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (change == null || change.delta() == 0 || (long)p.stock + change.delta() < 0 || (long)p.stock + change.delta() > Integer.MAX_VALUE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Movimiento de stock inválido");
        p.stock += change.delta();
        return products.save(p);
    }

    @PostMapping("/orders")
    @Transactional
    public PurchaseOrder order(@RequestBody OrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty() || request.contact() == null || request.contact().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido vacío o falta contacto");
        UserAccount user = account(request.userId());
        if (!"CLIENT".equals(user.role))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo clientes pueden comprar");
        if (!Set.of("RETIRO", "ENTREGA").contains(request.delivery()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modalidad de entrega inválida");
        TreeMap<Long, Integer> requested = new TreeMap<>();
        try {
            for (OrderItem item : request.items()) {
                if (item.productId() == null || item.quantity() <= 0)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida");
                requested.merge(item.productId(), item.quantity(), Math::addExact);
            }
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad fuera de rango");
        }
        PurchaseOrder order = new PurchaseOrder();
        order.userId = user.id;
        order.delivery = request.delivery();
        order.contact = request.contact();
        long total = 0;
        // Locks pesimistas en orden ascendente: inventario central, sin sobreventa.
        for (var item : requested.entrySet()) {
            Product p = products.locked(item.getKey()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente"));
            int quantity = item.getValue();
            if (!p.active || p.stock < quantity)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + p.name);
            p.stock -= quantity;
            OrderLine line = new OrderLine();
            line.order = order;
            line.productId = p.id;
            line.productName = p.name;
            line.unitPrice = p.price;
            line.quantity = quantity;
            order.lines.add(line);
            total += (long)p.price * quantity;
        }
        if (total > Integer.MAX_VALUE)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total fuera de rango");
        order.total = (int)total;
        return orders.save(order);
    }

    @GetMapping("/orders")
    public List<PurchaseOrder> orders(@RequestParam Long userId) {
        UserAccount u = account(userId);
        return "ADMIN".equals(u.role) ? orders.findAll() : orders.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PatchMapping("/orders/{id}/status")
    public PurchaseOrder status(@PathVariable Long id, @RequestParam Long adminId, @RequestBody StatusChange change) {
        requireAdmin(adminId);
        if (change == null || !Set.of("PENDIENTE_VALIDACION", "CONFIRMADO", "PREPARADO", "ENTREGADO").contains(change.status()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        PurchaseOrder o = orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido inexistente"));
        o.status = change.status();
        return orders.save(o);
    }
}
