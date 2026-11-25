package cl.oyl.mobile.ui.productos

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.ui.components.FormField
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ProductPicture
import cl.oyl.mobile.util.Validation
import cl.oyl.mobile.util.readPhoto
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch

@Composable
internal fun ProductForm(current: Product?, cancel: () -> Unit, save: (Product) -> Unit) {
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
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            try { camera.launch(null) } catch (_: Exception) { photoError = "No hay una cámara disponible." }
        } else photoError = "Permite el acceso a la cámara para tomar una fotografía. Puedes elegir desde galería."
    }
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
                OylOutlinedButton(onClick = {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        try { camera.launch(null) } catch (_: Exception) { photoError = "No hay una cámara disponible." }
                    } else cameraPermission.launch(android.Manifest.permission.CAMERA)
                }) { Text("Fotografiar producto") }
                OylOutlinedButton(enabled = !photoBusy, onClick = {
                    try { gallery.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    catch (_: Exception) { photoError = "No se pudo abrir la galería." }
                }) { Text(if (photoBusy) "Procesando imagen…" else "Elegir fotografía de galería") }
                ProductPicture(Product(name = name, imageUri = imageUri))
                photoError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                if (imageUri.isNotBlank()) Text("Imagen seleccionada", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = {
            OylTextButton(enabled = !photoBusy, onClick = {
                errors = Validation.product(name, category, description, presentation, price, stock, minStock)
                if (errors.isEmpty()) save(Product(
                    id = current?.id, name = name.trim(), category = category.trim(),
                    description = description, imageUri = imageUri, presentation = presentation,
                    price = price.toIntOrNull() ?: -1, stock = stock.toIntOrNull() ?: -1,
                    minStock = minStock.toIntOrNull() ?: 5, active = active
                ))
            }) { Text("Guardar") }
        },
        dismissButton = { OylTextButton(onClick = cancel) { Text("Cancelar") } }
    )
}
