package cl.oyl.mobile.ui.catalogo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.data.local.entity.Session
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.ui.components.FormField
import cl.oyl.mobile.ui.components.ProductCard
import cl.oyl.mobile.ui.components.ScreenTitle

@Composable
fun CatalogScreen(
    products: List<Product>,
    session: Session?,
    sync: String, reload: () -> Unit,
    detail: (Product) -> Unit,
    add: (Product, Int) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos") + products.filter { it.active }.map { it.category }.distinct().sorted()
    val visible = products.filter {
        it.active && it.name.contains(search, ignoreCase = true) && (category == "Todos" || it.category == category)
    }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        ScreenTitle("Catálogo de productos", "Disponibilidad oficial al confirmar la compra")
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(sync, Modifier.weight(1f))
            cl.oyl.mobile.ui.components.OylTextButton(onClick = reload) { Text("Actualizar") }
        }
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
