package cl.oyl.mobile

import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream

@Composable
fun AdminProductsScreen(
    products: List<Product>,
    save: (Product, () -> Unit) -> Unit,
    refresh: () -> Unit
) {
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
                        }
                        OutlinedButton(onClick = { editing = p; showForm = true }) { Text("Editar") }
                    }
                }
            }
        }
    }
    if (showForm) ProductForm(editing, cancel = { showForm = false }, save = { product ->
        save(product) { showForm = false; refresh() }
    })
}

@Composable
private fun ProductForm(current: Product?, cancel: () -> Unit, save: (Product) -> Unit) {
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var name by remember(current?.id) { mutableStateOf(current?.name.orEmpty()) }
    var category by remember(current?.id) { mutableStateOf(current?.category ?: "Frutos secos") }
    var description by remember(current?.id) { mutableStateOf(current?.description.orEmpty()) }
    var presentation by remember(current?.id) { mutableStateOf(current?.presentation ?: "250 g") }
    var price by remember(current?.id) { mutableStateOf((current?.price ?: 0).toString()) }
    var stock by remember(current?.id) { mutableStateOf((current?.stock ?: 0).toString()) }
    var minStock by remember(current?.id) { mutableStateOf((current?.minStock ?: 5).toString()) }
    var active by remember(current?.id) { mutableStateOf(current?.active ?: true) }
    var imageUri by remember(current?.id) { mutableStateOf(current?.imageUri.orEmpty()) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            val output = ByteArrayOutputStream()
            val scaled = Bitmap.createScaledBitmap(bmp, 320, 320, true)
            scaled.compress(Bitmap.CompressFormat.JPEG, 65, output)
            imageUri = "data:image/jpeg;base64," + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var photoError by remember { mutableStateOf<String?>(null) }
    var photoBusy by remember { mutableStateOf(false) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            photoBusy = true
            val data = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { readPhoto(context, uri) }
            photoBusy = false
            if (data != null) { imageUri = data; photoError = null }
            else photoError = "No se pudo abrir la fotografía."
        }
    }
    AlertDialog(
        onDismissRequest = cancel,
        title = { Text(if (current == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                FormField(name, { name = it; errors = errors - "name" }, "Nombre", error = errors["name"])
                FormField(category, { category = it; errors = errors - "category" }, "Categoría", error = errors["category"])
                FormField(description, { description = it; errors = errors - "description" }, "Descripción", error = errors["description"])
                FormField(presentation, { presentation = it; errors = errors - "presentation" }, "Presentación", error = errors["presentation"])
                FormField(price, { price = it; errors = errors - "price" }, "Precio CLP", numeric = true, error = errors["price"])
                FormField(stock, { stock = it; errors = errors - "stock" }, "Stock inicial", numeric = true, error = errors["stock"])
                FormField(minStock, { minStock = it; errors = errors - "minStock" }, "Alerta stock bajo", numeric = true, error = errors["minStock"])
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = active, onCheckedChange = { active = it })
                    Text("Producto activo")
                }
                OutlinedButton(onClick = {
                    try { camera.launch(null) } catch (_: Exception) { photoError = "No hay una cámara disponible." }
                }) { Text("Fotografiar producto") }
                OutlinedButton(enabled = !photoBusy, onClick = {
                    try { gallery.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    catch (_: Exception) { photoError = "No se pudo abrir la galería." }
                }) { Text(if (photoBusy) "Procesando imagen…" else "Elegir fotografía de galería") }
                ProductPicture(Product(name = name, imageUri = imageUri))
                photoError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                if (imageUri.isNotBlank()) Text("Imagen seleccionada", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = {
            TextButton(enabled = !photoBusy, onClick = {
                errors = Validation.product(name, category, description, presentation, price, stock, minStock)
                if (errors.isEmpty()) save(Product(
                    id = current?.id, name = name.trim(), category = category.trim(),
                    description = description, imageUri = imageUri, presentation = presentation,
                    price = price.toIntOrNull() ?: -1, stock = stock.toIntOrNull() ?: -1,
                    minStock = minStock.toIntOrNull() ?: 5, active = active
                ))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancelar") } }
    )
}

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
        ScreenTitle("Gestión de inventario", "Entradas y salidas registradas en Spring Boot")
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
                    TextButton(onClick = { selectedId = null; clearSelected() }) { Text("Cerrar selección") }
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
                        OutlinedButton(onClick = { selectedId = p.id; clearSelected() }) { Text("Mover") }
                    }
                }
            }
        }
    }
}
