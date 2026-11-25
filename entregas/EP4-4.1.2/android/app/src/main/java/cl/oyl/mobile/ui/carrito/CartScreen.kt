package cl.oyl.mobile.ui.carrito

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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ProductPicture
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.cartTotal
import cl.oyl.mobile.util.money

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
                            OylOutlinedButton(onClick = { adjust(line.productId, -1) }) { Text("−") }
                            OylOutlinedButton(onClick = { adjust(line.productId, 1) }) { Text("+") }
                            OylTextButton(onClick = { adjust(line.productId, -line.quantity) }) { Text("Quitar") }
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
