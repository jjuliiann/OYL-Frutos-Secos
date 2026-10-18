package cl.oyl.mobile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CartScreen(
    cart: List<CartLine>,
    products: List<Product>,
    adjust: (Long, Int) -> Unit,
    checkout: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTitle("Mi carrito", "Se guarda por cliente en Room / SQLite")
        if (cart.isEmpty()) { Text("Aún no agregas productos."); return@Column }
        LazyColumn(Modifier.weight(1f)) {
            items(cart) { line ->
                val p = products.firstOrNull { it.id == line.productId }
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        if (p != null) ProductPicture(p)
                        Text(p?.name ?: "Producto #${line.productId}", fontWeight = FontWeight.Bold)
                        Text("${money((p?.price ?: 0) * line.quantity)} · Cantidad ${line.quantity}")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(onClick = { adjust(line.productId, -1) }) { Text("−") }
                            OutlinedButton(onClick = { adjust(line.productId, 1) }) { Text("+") }
                            TextButton(onClick = { adjust(line.productId, -line.quantity) }) { Text("Quitar") }
                        }
                    }
                }
            }
        }
        Text("Total estimado: ${money(cartTotal(cart, products))}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        ActionButton("Confirmar compra", onClick = checkout)
    }
}

@Composable
fun CheckoutScreen(
    cart: List<CartLine>,
    products: List<Product>,
    confirmedOrder: Long?,
    busy: Boolean,
    confirm: (String, String, (Long) -> Unit) -> Unit,
    after: (Long) -> Unit,
    orders: () -> Unit,
    catalog: () -> Unit
) {
    var delivery by remember { mutableStateOf("RETIRO") }
    var contactError by remember { mutableStateOf<String?>(null) }
    var contact by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (confirmedOrder != null) {
            ScreenTitle("¡Pedido confirmado!", "Número de solicitud #$confirmedOrder")
            Text("Estado inicial: Pendiente de validación")
            Text("El servidor ha validado precios, stock y cantidades.")
            ActionButton("Ver mis pedidos", onClick = orders)
            OutlinedButton(onClick = catalog, modifier = Modifier.fillMaxWidth()) { Text("Seguir comprando") }
        } else {
            ScreenTitle("Confirmar pedido", "Revisión final antes de enviar al servidor")
            cart.forEach { line ->
                val p = products.firstOrNull { it.id == line.productId }
                Text("${p?.name ?: "Producto"} × ${line.quantity} — ${money((p?.price ?: 0) * line.quantity)}")
            }
            Text("Total estimado: ${money(cartTotal(cart, products))}", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = delivery == "RETIRO", onClick = { delivery = "RETIRO" })
                Text("Retiro")
                RadioButton(selected = delivery == "ENTREGA", onClick = { delivery = "ENTREGA" })
                Text("Entrega")
            }
            FormField(contact, { contact = it; contactError = null }, "Contacto ficticio", error = contactError)
            Text("El backend recalcula el total y bloquea los productos al descontar stock.", style = MaterialTheme.typography.bodySmall)
            ActionButton("Enviar pedido", enabled = cart.isNotEmpty() && !busy, onClick = {
                contactError = Validation.contact(contact)
                if (contactError == null) confirm(delivery, contact.trim(), after)
            })
        }
    }
}

@Composable
fun OrdersScreen(
    orders: List<PurchaseOrder>,
    cachedOrders: List<CachedOrder>,
    admin: Boolean,
    setStatus: (Long, String) -> Unit,
    reload: () -> Unit
) {
    var filter by remember { mutableStateOf("Todos") }
    val options = listOf("Todos", "PENDIENTE_VALIDACION", "CONFIRMADO", "PREPARADO", "ENTREGADO")
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        ScreenTitle(if (admin) "Gestión de pedidos" else "Mis pedidos", "Estados oficiales entregados por Spring Boot")
        ActionButton("Actualizar pedidos", onClick = reload)
        androidx.compose.material3.ScrollableTabRow(selectedTabIndex = options.indexOf(filter), edgePadding = 0.dp) {
            options.forEach { state ->
                androidx.compose.material3.Tab(selected = filter == state, onClick = { filter = state }, text = { Text(statusLabel(state)) })
            }
        }
        if (orders.isEmpty() && !admin && cachedOrders.isNotEmpty()) {
            Text("Historial local en caché (sin conexión)", style = MaterialTheme.typography.labelSmall)
            LazyColumn {
                items(cachedOrders.filter { filter == "Todos" || it.status == filter }) { item ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Pedido #${item.orderId}", fontWeight = FontWeight.Bold)
                            Text("${money(item.total)} · ${statusLabel(item.status)}")
                            Text(displayDate(item.createdAt))
                        }
                    }
                }
            }
        } else {
            val shown = orders.filter { filter == "Todos" || it.status == filter }
            if (shown.isEmpty()) Text("Sin pedidos para mostrar.", Modifier.padding(10.dp))
            LazyColumn {
                items(shown) { order ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Pedido #${order.id} · ${money(order.total)}", fontWeight = FontWeight.Bold)
                            Text("${statusLabel(order.status)} · ${displayDate(order.createdAt)}")
                            Text("Modalidad: ${order.delivery}")
                            order.lines.forEach { line -> Text("${line.productName} × ${line.quantity} · ${money(line.unitPrice)}") }
                            if (admin) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    listOf("CONFIRMADO", "PREPARADO", "ENTREGADO").forEach { state ->
                                        TextButton(onClick = { setStatus(order.id, state) }) { Text(statusLabel(state)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
