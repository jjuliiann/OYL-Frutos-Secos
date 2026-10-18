package cl.oyl.mobile

import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream

@Composable
fun MapScreen(locations: List<PickupLocation>, reload: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val place = locations.firstOrNull { it.id == selectedId } ?: locations.firstOrNull()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Mapa y puntos de retiro", "Puntos ficticios originales para evaluación académica")
        if (place == null) {
            Text("No hay puntos disponibles. Comprueba internet y que Spring Boot esté iniciado.")
            OutlinedButton(onClick = reload) { Text("Volver a cargar puntos") }
        } else {
            PickupMap(place, locations)
            Text("${place.name} · ${place.address}", fontWeight = FontWeight.Bold)
            Text(place.hours)
            var directionError by remember { mutableStateOf(false) }
            OutlinedButton(onClick = {
                val url = "https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}"
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); directionError = false }
                catch (_: Exception) { directionError = true }
            }, modifier = Modifier.fillMaxWidth()) { Text("Cómo llegar") }
            if (directionError) Text("No se encontró una aplicación para abrir la dirección.")
        }
        locations.forEach { location ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp)) {
                    Text(location.name, fontWeight = FontWeight.Bold)
                    Text(location.address)
                    TextButton(onClick = { selectedId = location.id }) { Text("Ver marcador") }
                }
            }
        }
    }
}

@Composable
fun QrScreen(
    products: List<Product>,
    canManage: Boolean,
    found: (Product) -> Unit,
    notFound: (String) -> Unit,
    inventory: (Product) -> Unit
) {
    val context = LocalContext.current
    val scanner = remember { BarcodeScanning.getClient() }
    DisposableEffect(scanner) { onDispose { scanner.close() } }
    var manualError by remember { mutableStateOf<String?>(null) }
    var manual by remember { mutableStateOf("") }
    var scanned by remember { mutableStateOf<Product?>(null) }
    fun scan(image: InputImage) {
        scanner.process(image)
            .addOnSuccessListener { codes ->
                val id = codes.firstNotNullOfOrNull { qrProductId(it.rawValue.orEmpty()) }
                val product = products.firstOrNull { it.id == id }
                if (product == null) notFound("QR no reconocido. Prueba un código con ID válido (ej.: OYL:1).")
                else scanned = product
            }
            .addOnFailureListener { notFound("No se pudo analizar la imagen del QR") }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) scan(InputImage.fromBitmap(bmp, 0))
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try { scan(InputImage.fromFilePath(context, uri)) }
            catch (_: Exception) { notFound("No se pudo abrir la imagen seleccionada") }
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("Escáner QR de productos", "Escanea un código que contenga el ID del producto")
        ActionButton("Fotografiar código QR", onClick = { camera.launch(null) })
        OutlinedButton(onClick = { gallery.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Leer QR desde galería") }
        FormField(manual, { manual = it; manualError = null }, "ID del producto (prueba manual)", error = manualError)
        ActionButton("Buscar producto", onClick = {
            val product = products.firstOrNull { it.id == qrProductId(manual) }
            if (product == null) { manualError = "Ingresa un ID de producto existente"; notFound("Producto no encontrado") } else { manualError = null; scanned = product }
        })
        if (scanned != null) {
            val product = scanned!!
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("QR reconocido: ${product.name}", fontWeight = FontWeight.Bold)
                    ActionButton("Ver ficha de producto", onClick = { found(product) })
                    if (canManage) OutlinedButton(onClick = { inventory(product) }, modifier = Modifier.fillMaxWidth()) { Text("Ajustar inventario") }
                }
            }
        }
        Text("El lector utiliza ML Kit. Si la cámara del emulador no enfoca el QR, usa una imagen desde Galería.")
        Text("Para el administrador, el producto se puede seleccionar después para ajustar inventario.")
    }
}

@Composable
fun NotificationsScreen(
    notices: List<LocalNotice>,
    markRead: (Long) -> Unit,
    demo: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Notificaciones", "Avisos de prueba separados por cliente")
        ActionButton("Permitir notificaciones", onClick = {
            if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        })
        OutlinedButton(onClick = {
            demo()
            val allowed = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (allowed) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = NotificationCompat.Builder(context, "oyl_demo")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("OYL Frutos Secos")
                    .setContentText("Promoción de demostración")
                    .setAutoCancel(true)
                    .build()
                manager.notify(System.currentTimeMillis().toInt(), notification)
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Enviar notificación de demostración") }
        if (notices.isEmpty()) Text("Sin notificaciones guardadas")
        LazyColumn {
            items(notices) { notice ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(notice.title + if (notice.read) "" else " • Nueva", fontWeight = FontWeight.Bold)
                        Text(notice.content)
                        TextButton(onClick = { markRead(notice.id) }) { Text("Marcar como leída") }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    session: Session?,
    avatar: LocalAvatar?,
    saveAvatar: (String) -> Unit,
    update: (ProfileUpdate) -> Unit,
    logout: () -> Unit,
    notifications: () -> Unit
) {
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var name by remember(session?.id) { mutableStateOf(session?.name.orEmpty()) }
    var email by remember(session?.id) { mutableStateOf(session?.email.orEmpty()) }
    var phone by remember(session?.id) { mutableStateOf(session?.phone.orEmpty()) }
    var address by remember(session?.id) { mutableStateOf(session?.address.orEmpty()) }
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var choosing by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var photoBusy by remember { mutableStateOf(false) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            val data = encodePhoto(bmp)
            if (data != null) { saveAvatar(data); photoError = null }
            else photoError = "No se pudo procesar la fotografía."
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            photoBusy = true
            val data = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { readPhoto(context, uri) }
            photoBusy = false
            if (data != null) { saveAvatar(data); photoError = null }
            else photoError = "No se pudo abrir esa imagen. Elige otra fotografía."
        }
    }
    if (choosing) androidx.compose.material3.AlertDialog(
        onDismissRequest = { choosing = false }, title = { Text("Cambiar foto / avatar") },
        text = { Column {
            TextButton(onClick = {
                choosing = false
                try { camera.launch(null) } catch (_: Exception) { photoError = "No hay una cámara disponible." }
            }) { Text("Tomar fotografía") }
            TextButton(onClick = {
                choosing = false
                try { gallery.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                catch (_: Exception) { photoError = "No se pudo abrir el selector de imágenes." }
            }) { Text("Elegir desde galería") }
        } },
        confirmButton = { TextButton(onClick = { choosing = false }) { Text("Cancelar") } }
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Mi perfil", "${session?.alias ?: "Invitado"} · ${session?.role ?: "Sin sesión"}")
        if (session == null) { Text("Inicia sesión para editar tu perfil."); return@Column }
        ProductPicture(Product(name = "Avatar", imageUri = avatar?.dataUri.orEmpty()), Modifier.clip(androidx.compose.foundation.shape.CircleShape))
        OutlinedButton(onClick = { choosing = true }, enabled = !photoBusy, modifier = Modifier.fillMaxWidth()) { Text(if (photoBusy) "Procesando fotografía…" else "Cambiar foto / avatar") }
        photoError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        FormField(name, { name = it; errors = errors - "name" }, "Nombre", error = errors["name"])
        FormField(email, { email = it; errors = errors - "email" }, "Correo ficticio", error = errors["email"])
        FormField(phone, { phone = it; errors = errors - "phone" }, "Teléfono ficticio", error = errors["phone"])
        FormField(address, { address = it; errors = errors - "address" }, "Dirección de prueba", error = errors["address"])
        ActionButton("Guardar perfil", onClick = { errors = Validation.profile(name, email, phone, address); if (errors.isEmpty()) update(ProfileUpdate(name.trim(), email.trim(), phone.trim(), address.trim())) })
        OutlinedButton(onClick = notifications, modifier = Modifier.fillMaxWidth()) { Text("Configurar notificaciones") }
        OutlinedButton(onClick = logout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}
