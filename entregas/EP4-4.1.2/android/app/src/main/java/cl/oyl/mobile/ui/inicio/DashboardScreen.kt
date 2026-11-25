package cl.oyl.mobile.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.data.local.entity.Session
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.model.pedidos.PurchaseOrder
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.ProductCard
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.ui.navigation.Page

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
    val client = session?.role == "CLIENT"
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
        } else if (client) {
            ActionButton("Explorar catálogo", onClick = { open(Page.CATALOG) })
            ActionButton("Mi carrito (${cart.sumOf { it.quantity }})", onClick = { open(Page.CART) })
            ActionButton("Mis pedidos", onClick = { open(Page.ORDERS) })
        } else {
            ActionButton("Explorar catálogo", onClick = { open(Page.CATALOG) })
            ActionButton("Iniciar sesión", onClick = { open(Page.LOGIN) })
        }
        Text("Productos destacados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        products.filter { it.active }.take(4).forEach { p ->
            ProductCard(p, open = { detail(p) }, add = if (client) {{ add(p, 1) }} else null)
        }
    }
}
