package cl.oyl.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onCatalog: () -> Unit, onMap: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        AnimatedVisibility(visible = shown, enter = fadeIn(tween(850)) + slideInVertically(tween(850)) { it / 3 }) {
            Column { ScreenTitle("Bienvenido a OYL", "FRUTOS SECOS · Semillas · Deshidratados") }
        }
        Text("Productos naturales más cerca de ti. Consulta el catálogo, prepara tu pedido y retíralo en los puntos de demostración.")
        Spacer(Modifier.height(20.dp))
        ActionButton("Comenzar / Iniciar sesión", onClick = onLogin)
        OutlinedButton(onClick = onCatalog, modifier = Modifier.fillMaxWidth()) { Text("Ver catálogo") }
        OutlinedButton(onClick = onMap, modifier = Modifier.fillMaxWidth()) { Text("Ver mapa y puntos de retiro") }
        Text("MVP académico. Productos, usuarios y direcciones son ficticios.", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun LoginScreen(
    busy: Boolean,
    login: (String, String, () -> Unit) -> Unit,
    register: () -> Unit,
    done: () -> Unit
) {
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenTitle("Iniciar sesión", "Ingresa como cliente o administrador")
        FormField(alias, { alias = it; errors = errors - "alias" }, "Alias", error = errors["alias"])
        FormField(password, { password = it; errors = errors - "password" }, "Contraseña", password = true, error = errors["password"])
        ActionButton("Ingresar", enabled = !busy, onClick = { errors = Validation.login(alias, password); if (errors.isEmpty()) login(alias, password, done) })
        OutlinedButton(onClick = register, modifier = Modifier.fillMaxWidth()) { Text("Crear cuenta de cliente") }
        Card { Column(Modifier.padding(14.dp)) {
            Text("Acceso de demostración", fontWeight = FontWeight.Bold)
            Text("Administrador: admin / Admin1234!")
            Text("No utilices credenciales reales.", style = MaterialTheme.typography.labelSmall)
        } }
    }
}

@Composable
fun RegisterScreen(
    busy: Boolean,
    register: (RegisterData, () -> Unit) -> Unit,
    done: () -> Unit
) {
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var name by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Crear cuenta", "Solo registro de clientes · datos de prueba")
        FormField(name, { name = it; errors = errors - "name" }, "Nombre completo", error = errors["name"])
        FormField(alias, { alias = it; errors = errors - "alias" }, "Alias único", error = errors["alias"])
        FormField(password, { password = it; errors = errors - "password" }, "Contraseña (mínimo 8)", password = true, error = errors["password"])
        FormField(email, { email = it; errors = errors - "email" }, "Correo ficticio", error = errors["email"])
        FormField(phone, { phone = it; errors = errors - "phone" }, "Teléfono ficticio", error = errors["phone"])
        FormField(address, { address = it; errors = errors - "address" }, "Dirección de prueba", error = errors["address"])
        ActionButton("Registrar cliente", enabled = !busy, onClick = {
            val data = RegisterData(alias.trim(), password, name.trim(), email.trim(), phone.trim(), address.trim())
            errors = Validation.register(data)
            if (errors.isEmpty()) register(data, done)
        })
    }
}

@Composable
fun DashboardScreen(
    session: Session?,
    products: List<Product>,
    orders: List<PurchaseOrder>,
    cart: List<CartLine>,
    open: (Page) -> Unit,
    detail: (Product) -> Unit,
    add: (Product, Int) -> Unit
) {
    val admin = session?.role == "ADMIN"
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Hola, ${session?.name ?: "visitante"}", if (admin) "Panel de administración" else "Compra saludable, vive mejor")
        if (admin) {
            Card { Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text("Pedidos recibidos: ${orders.size}")
                Text("Productos con stock bajo: ${products.count { it.stock <= it.minStock }}")
            } }
            ActionButton("Gestionar productos", onClick = { open(Page.PRODUCTS) })
            ActionButton("Gestionar inventario", onClick = { open(Page.INVENTORY) })
            ActionButton("Gestionar pedidos", onClick = { open(Page.ORDERS) })
        } else {
            ActionButton("Explorar catálogo", onClick = { open(Page.CATALOG) })
            ActionButton("Mi carrito (${cart.sumOf { it.quantity }})", onClick = { open(Page.CART) })
            ActionButton("Mis pedidos", onClick = { open(Page.ORDERS) })
        }
        Text("Productos destacados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        products.filter { it.active }.take(4).forEach { p ->
            ProductCard(p, open = { detail(p) }, add = if (!admin && session != null) {{ add(p, 1) }} else null)
        }
    }
}

@Composable
fun CatalogScreen(
    products: List<Product>,
    session: Session?,
    detail: (Product) -> Unit,
    add: (Product, Int) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Frutos secos", "Semillas", "Deshidratados")
    val visible = products.filter {
        it.active && it.name.contains(search, ignoreCase = true) && (category == "Todos" || it.category == category)
    }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        ScreenTitle("Catálogo de productos", "Disponibilidad oficial al confirmar la compra")
        FormField(search, { search = it }, "Buscar producto")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            categories.forEach { c -> FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }) }
        }
        if (visible.isEmpty()) Text("No hay productos para mostrar.", Modifier.padding(16.dp))
        LazyColumn {
            items(visible) { p ->
                ProductCard(p, open = { detail(p) }, add = if (session?.role == "CLIENT") {{ add(p, 1) }} else null)
            }
        }
    }
}

@Composable
fun ProductDetailScreen(
    product: Product?, session: Session?,
    back: () -> Unit,
    add: (Product, Int) -> Unit,
    signIn: () -> Unit
) {
    var quantity by remember(product?.id) { mutableIntStateOf(1) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (product == null) { Text("Selecciona un producto."); ActionButton("Volver", onClick = back); return@Column }
        ScreenTitle(product.name, product.category)
        ProductPicture(product, pictureSize = 200.dp)
        Text(product.description)
        Text("Presentación: ${product.presentation}")
        Text("Precio: ${money(product.price)}", fontWeight = FontWeight.Bold)
        Text("Disponibles: ${product.stock}")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { quantity = (quantity - 1).coerceAtLeast(1) }) { Text("−") }
            Text("$quantity", Modifier.padding(top = 12.dp))
            OutlinedButton(onClick = { quantity = (quantity + 1).coerceAtMost(product.stock.coerceAtLeast(1)) }) { Text("+") }
        }
        if (session?.role == "CLIENT") {
            ActionButton("Agregar al carrito", enabled = product.stock >= quantity, onClick = { add(product, quantity) })
        } else if (session == null) {
            ActionButton("Iniciar sesión para comprar", onClick = signIn)
        }
        TextButton(onClick = back) { Text("Volver al catálogo") }
    }
}
