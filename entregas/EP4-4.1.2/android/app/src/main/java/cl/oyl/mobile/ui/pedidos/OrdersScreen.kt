package cl.oyl.mobile.ui.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.data.local.entity.CachedOrder
import cl.oyl.mobile.model.pedidos.PurchaseOrder
import cl.oyl.mobile.ui.components.FulfillmentSummary
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.displayDate
import cl.oyl.mobile.util.money
import cl.oyl.mobile.util.statusLabel

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
        ScreenTitle(if (admin) "Gestión de pedidos" else "Mis pedidos", "Compras registradas en OYL")
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
                            if (order.fulfillment != null) FulfillmentSummary(order.fulfillment)
                            else {
                                Text(if (order.delivery == "RETIRO") "Retiro en tienda" else "Envío a domicilio")
                                if (order.contact.isNotBlank()) Text("Contacto: ${order.contact}")
                            }
                            order.lines.forEach { line -> Text("${line.productName} × ${line.quantity} · ${money(line.unitPrice)}") }
                            if (admin) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    listOf("CONFIRMADO", "PREPARADO", "ENTREGADO").forEach { state ->
                                        OylTextButton(onClick = { setStatus(order.id, state) }) { Text(statusLabel(state)) }
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
