package cl.oyl.mobile.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.data.local.entity.Session
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ProductPicture
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.money

@Composable
fun ProductDetailScreen(
    product: Product?, session: Session?,
    back: () -> Unit,
    add: (Product, Int) -> Unit,
    signIn: () -> Unit,
    manageProducts: () -> Unit,
    manageInventory: (Product) -> Unit,
    unavailableMessage: String? = null
) {
    val client = session?.role == "CLIENT"
    val admin = session?.role == "ADMIN"
    var quantity by remember(product?.id) { mutableIntStateOf(1) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (product == null) { Text(unavailableMessage ?: "Selecciona un producto."); ActionButton("Volver", onClick = back); return@Column }
        ScreenTitle(product.name, if (admin) "Administración · ${product.category}" else product.category)
        ProductPicture(product, pictureSize = 200.dp)
        Text(product.description)
        Text("Presentación: ${product.presentation}")
        Text("Precio: ${money(product.price)}", fontWeight = FontWeight.Bold)
        Text("Disponibles: ${product.stock}")
        if (client) Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OylOutlinedButton(onClick = { quantity = (quantity - 1).coerceAtLeast(1) }) { Text("−") }
            Text("$quantity", Modifier.padding(top = 12.dp))
            OylOutlinedButton(onClick = { quantity = (quantity + 1).coerceAtMost(product.stock.coerceAtLeast(1)) }) { Text("+") }
        }
        if (admin) {
            ActionButton("Gestionar productos", onClick = manageProducts)
            OylOutlinedButton(onClick = { manageInventory(product) }) { Text("Ajustar inventario") }
        } else if (client) {
            ActionButton("Agregar al carrito", enabled = product.active && product.stock >= quantity, onClick = { add(product, quantity) })
        } else if (session == null) {
            ActionButton("Iniciar sesión para comprar", onClick = signIn)
        }
        OylTextButton(onClick = back) { Text("Volver al catálogo") }
    }
}
