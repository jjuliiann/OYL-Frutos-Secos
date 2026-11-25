package cl.oyl.mobile.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import cl.oyl.mobile.ui.components.FormField
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ProductPicture
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.Validation

@Composable
fun InventoryScreen(
    products: List<Product>,
    change: (Long, Int) -> Unit,
    selected: Product?,
    clearSelected: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var movement by remember { mutableStateOf("Entrada") }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val selectedProduct = selected ?: products.firstOrNull { it.id == selectedId }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Gestión de inventario", "Entradas y salidas guardadas en este dispositivo")
        FormField(query, { query = it }, "Buscar producto")
        if (selectedProduct != null) {
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(selectedProduct.name, fontWeight = FontWeight.Bold)
                    Text("Stock actual: ${selectedProduct.stock}")
                    FormField(quantity, { quantity = it }, "Cantidad", numeric = true, error = Validation.quantity(quantity))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Entrada", "Salida").forEach { type ->
                            FilterChip(selected = movement == type, onClick = { movement = type }, label = { Text(type) })
                        }
                    }
                    ActionButton("Registrar movimiento", enabled = (quantity.toIntOrNull() ?: 0) > 0, onClick = {
                        val delta = (quantity.toIntOrNull() ?: 0) * if (movement == "Entrada") 1 else -1
                        change(selectedProduct.id ?: 0, delta)
                        selectedId = null
                        clearSelected()
                    })
                    OylTextButton(onClick = { selectedId = null; clearSelected() }) { Text("Cerrar selección") }
                }
            }
        }
        LazyColumn {
            items(products.filter { it.name.contains(query, ignoreCase = true) }) { p ->
                Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProductPicture(p)
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Text("Stock: ${p.stock} · Mínimo: ${p.minStock}")
                            if (p.stock <= p.minStock) Text("Stock bajo", color = androidx.compose.ui.graphics.Color.Red)
                        }
                        OylOutlinedButton(onClick = { selectedId = p.id; clearSelected() }) { Text("Mover") }
                    }
                }
            }
        }
    }
}
