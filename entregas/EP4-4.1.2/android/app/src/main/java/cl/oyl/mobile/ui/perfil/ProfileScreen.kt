package cl.oyl.mobile.ui.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.data.local.entity.LocalAvatar
import cl.oyl.mobile.data.local.entity.Session
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.model.perfil.ProfileUpdate
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.FormField
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ProductPicture
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.Validation
import cl.oyl.mobile.util.encodePhoto
import cl.oyl.mobile.util.readPhoto
import kotlinx.coroutines.launch

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
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            try { camera.launch(null) } catch (_: Exception) { photoError = "No hay una cámara disponible." }
        } else photoError = "Permite el acceso a la cámara para tomar una fotografía. También puedes elegir desde galería."
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
            OylTextButton(onClick = {
                choosing = false
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    try { camera.launch(null) } catch (_: Exception) { photoError = "No hay una cámara disponible." }
                } else cameraPermission.launch(android.Manifest.permission.CAMERA)
            }) { Text("Tomar fotografía") }
            OylTextButton(onClick = {
                choosing = false
                try { gallery.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                catch (_: Exception) { photoError = "No se pudo abrir el selector de imágenes." }
            }) { Text("Elegir desde galería") }
        } },
        confirmButton = { OylTextButton(onClick = { choosing = false }) { Text("Cancelar") } }
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Mi perfil", "${session?.alias ?: "Invitado"} · ${session?.role ?: "Sin sesión"}")
        if (session == null) { Text("Inicia sesión para editar tu perfil."); return@Column }
        ProductPicture(Product(name = "Avatar", imageUri = avatar?.dataUri.orEmpty()), Modifier.clip(androidx.compose.foundation.shape.CircleShape))
        OylOutlinedButton(onClick = { choosing = true }, enabled = !photoBusy, modifier = Modifier.fillMaxWidth()) { Text(if (photoBusy) "Procesando fotografía…" else "Cambiar foto / avatar") }
        photoError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        FormField(name, { name = it; errors = errors - "name" }, "Nombre", error = errors["name"])
        FormField(email, { email = it; errors = errors - "email" }, "Correo electrónico", error = errors["email"])
        FormField(phone, { phone = it; errors = errors - "phone" }, "Teléfono", error = errors["phone"])
        FormField(address, { address = it; errors = errors - "address" }, "Dirección", error = errors["address"])
        ActionButton("Guardar perfil", onClick = { errors = Validation.profile(name, email, phone, address); if (errors.isEmpty()) update(ProfileUpdate(name.trim(), email.trim(), phone.trim(), address.trim())) })
        OylOutlinedButton(onClick = notifications, modifier = Modifier.fillMaxWidth()) { Text("Configurar notificaciones") }
        OylOutlinedButton(onClick = logout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}
