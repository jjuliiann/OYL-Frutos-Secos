package cl.oyl.mobile.ui.productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.ProductPicture
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.money

@Composable
fun AdminProductsScreen(
    products: List<Product>,
    save: (Product, () -> Unit) -> Unit,
    deactivate: (Long) -> Unit,
    refresh: () -> Unit
) {
    var toDeactivate by remember { mutableStateOf<Product?>(null) }
    var editing by remember { mutableStateOf<Product?>(null) }
    var showForm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        ScreenTitle("Gestión de productos", "Crear, editar, activar o desactivar")
        ActionButton("+ Nuevo producto", onClick = { editing = null; showForm = true })
        LazyColumn {
            items(products) { p ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProductPicture(p)
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Text("${money(p.price)} · Stock: ${p.stock}")
                            Text(if (p.active) "Activo" else "Desactivado")
                            if (p.active) cl.oyl.mobile.ui.components.OylTextButton(onClick = { toDeactivate = p }) { Text("Desactivar") }
                        }
                        OylOutlinedButton(onClick = { editing = p; showForm = true }) { Text("Editar") }
                    }
                }
            }
        }
    }
    toDeactivate?.let { item ->
        androidx.compose.material3.AlertDialog(onDismissRequest = { toDeactivate = null },
            title = { Text("¿Desactivar ${item.name}?") },
            text = { Text("Dejará de aparecer en el catálogo. Puedes reactivarlo desde Editar.") },
            confirmButton = { OylOutlinedButton(onClick = { toDeactivate = null; item.id?.let(deactivate) }) { Text("Desactivar") } },
            dismissButton = { OylOutlinedButton(onClick = { toDeactivate = null }) { Text("Cancelar") } })
    }
    if (showForm) ProductForm(editing, cancel = { showForm = false }, save = { product ->
        save(product) { showForm = false; refresh() }
    })
}
