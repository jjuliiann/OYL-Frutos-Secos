package cl.oyl.mobile.ui.qr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.R
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.model.qr.QrResolution
import cl.oyl.mobile.viewmodel.qr.QrViewModel
import cl.oyl.mobile.domain.qr.qrProductId
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.FormField
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ProductPicture
import cl.oyl.mobile.ui.components.ScreenTitle
import cl.oyl.mobile.util.money
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QrSample(val label: String, val resource: Int, val payload: String)
val qrSamples = listOf(
    QrSample("Almendras",R.drawable.qr_almendras,"oyl://product/1"), QrSample("Nueces",R.drawable.qr_nueces,"oyl://product/2"),
    QrSample("Mix Premium",R.drawable.qr_mix_premium,"oyl://product/3"), QrSample("Pasas",R.drawable.qr_pasas,"oyl://product/4"),
    QrSample("Semillas de chía",R.drawable.qr_chia,"oyl://product/5"), QrSample("Cranberries",R.drawable.qr_cranberries,"oyl://product/6"),
    QrSample("Avellanas",R.drawable.qr_avellanas,"oyl://product/7"), QrSample("Producto inexistente",R.drawable.qr_inexistente,"oyl://product/999999"),
    QrSample("Código ajeno",R.drawable.qr_invalido,"CODIGO AJENO A OYL"))

@Composable
fun QrScreen(vm: QrViewModel, canManage: Boolean, found: (Product) -> Unit,
             inventory: (Product) -> Unit) {
    val context = LocalContext.current
    val scanner = remember { BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()) }
    DisposableEffect(scanner) { onDispose { scanner.close() } }
    val resolution by vm.result.collectAsState()
    val scanned = (resolution as? QrResolution.Found)?.product
    LaunchedEffect(vm) { vm.reset() }
    var message by remember { mutableStateOf<String?>(null) }
    var decoding by remember { mutableStateOf(false) }
    var manual by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var customBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var simulator by remember { mutableStateOf(false) }
    var liveScanning by remember { mutableStateOf(false) }
    var sampleIndex by remember { mutableIntStateOf(0) }
    fun resolve(payload: String?, openDetail: Boolean = false) {
        decoding = true
        vm.reset()
        message = "Consultando producto…"
        scope.launch {
            try {
                val result = vm.resolve(payload)
                message = null
                if (openDetail && result is QrResolution.Found) found(result.product)
            } finally { decoding = false }
        }
    }
    fun scan(image: InputImage) {
        if (decoding) return
        decoding = true; vm.reset(); message = "Analizando imagen…"
        scanner.process(image).addOnSuccessListener { codes ->
            val code = codes.firstOrNull { qrProductId(it.rawValue.orEmpty()) != null } ?: codes.firstOrNull()
            resolve(code?.rawValue)
        }.addOnFailureListener { message = "No se pudo analizar la imagen. Intenta otra."; decoding = false }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            decoding = true; vm.reset(); message = "Abriendo imagen…"
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    var sample = 1
                    while (bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) sample *= 2
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
                } catch (_: Exception) { null }
            }
            decoding = false
            if (bitmap == null) message = "No se pudo abrir esa imagen."
            else { customBitmap = bitmap; simulator = true; scan(InputImage.fromBitmap(bitmap,0)) }
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenTitle("Escanear QR", "Apunta la cámara al código del producto")
        ActionButton(if (liveScanning) "Detener escáner" else "Abrir escáner QR", enabled = !decoding, onClick = {
            liveScanning = !liveScanning
            if (liveScanning) simulator = false
            vm.reset()
            message = null
        })
        if (liveScanning) LiveQrScanner(onPayload = { payload ->
            liveScanning = false
            resolve(payload, openDetail = true)
        })
        OylOutlinedButton(onClick = { liveScanning = false; try { gallery.launch("image/*") } catch (_: Exception) { message = "No se pudo abrir la galería." } }, enabled = !decoding, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar imagen QR") }
        OylOutlinedButton(onClick = { liveScanning = false; simulator = !simulator }, modifier = Modifier.fillMaxWidth()) { Text(if (simulator) "Cerrar cámara simulada" else "Abrir cámara simulada") }
        AnimatedVisibility(simulator) {
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cámara simulada · lector QR", style = MaterialTheme.typography.titleMedium)
                    Text("No usa la cámara física. ML Kit decodifica realmente la imagen mostrada.", style = MaterialTheme.typography.bodySmall)
                    val sample = qrSamples[sampleIndex]
                    val bundled = remember(sample.resource) { BitmapFactory.decodeResource(context.resources,sample.resource) }
                    val bitmap = customBitmap ?: bundled
                    val transition = rememberInfiniteTransition(label = "Escáner")
                    val line by transition.animateFloat(0f, 200f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "Línea de lectura")
                    Box(Modifier.fillMaxWidth().height(240.dp).background(Color.White), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Image(bitmap.asImageBitmap(),"Imagen QR: ${sample.label}",Modifier.size(232.dp))
                        Box(Modifier.offset(y = (line - 100).dp).fillMaxWidth(.8f).height(2.dp).background(MaterialTheme.colorScheme.primary))
                    }
                    Text(if (customBitmap != null) "Tu imagen seleccionada" else sample.label, style = MaterialTheme.typography.titleSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OylTextButton(onClick = { customBitmap = null; sampleIndex = (sampleIndex + qrSamples.size - 1) % qrSamples.size; vm.reset(); message = null }, enabled = !decoding) { Text("Anterior") }
                        Text("${sampleIndex+1} / ${qrSamples.size}")
                        OylTextButton(onClick = { customBitmap = null; sampleIndex = (sampleIndex+1) % qrSamples.size; vm.reset(); message = null }, enabled = !decoding) { Text("Siguiente") }
                    }
                    ActionButton("Analizar imagen del simulador", enabled = !decoding, onClick = { scan(InputImage.fromBitmap(bitmap,0)) })
                    OylOutlinedButton(onClick = { liveScanning = false; try { gallery.launch("image/*") } catch (_: Exception) { message = "No se pudo abrir la galería." } }, enabled = !decoding, modifier = Modifier.fillMaxWidth()) { Text("Cargar mi QR en el simulador") }
                    Text("También puedes cargar una imagen propia con ‘Seleccionar imagen QR’.", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        FormField(manual, { manual = it }, "Código manual (ej.: OYL:1)")
        ActionButton("Verificar código manual", enabled = !decoding, onClick = { liveScanning = false; resolve(manual) })
        if (decoding) LinearProgressIndicator(Modifier.fillMaxWidth())
        (message ?: resolution.message.takeIf { it.isNotBlank() })?.let { Text(it, color = if (scanned != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, modifier = Modifier.padding(4.dp)) }
        AnimatedVisibility(scanned != null) {
            scanned?.let { product -> Card { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProductPicture(product)
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("${money(product.price)} · Stock: ${product.stock}")
                ActionButton("Ver detalles del producto", onClick = { found(product) })
                if (canManage) OylOutlinedButton(onClick = { inventory(product) }) { Text("Ajustar inventario") }
            } } }
        }
    }
}
