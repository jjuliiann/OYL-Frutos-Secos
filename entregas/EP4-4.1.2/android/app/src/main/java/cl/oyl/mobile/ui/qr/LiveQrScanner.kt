package cl.oyl.mobile.ui.qr

import cl.oyl.mobile.domain.qr.qrProductId
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.oyl.mobile.ui.components.OylOutlinedButton
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/** Vista previa y análisis continuo. No toma ni guarda fotografías. */
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun LiveQrScanner(onPayload: (String) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val currentPayload by rememberUpdatedState(onPayload)
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        if (!it) error = "Permite el acceso a la cámara para usar el escáner. Puedes seguir usando galería o simulador."
    }
    // Actualiza el permiso también al volver de Ajustes.
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) { if (!granted) permission.launch(Manifest.permission.CAMERA) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!granted) {
            Text(error ?: "La cámara necesita tu autorización.")
            OylOutlinedButton(onClick = { permission.launch(Manifest.permission.CAMERA) }) { Text("Permitir cámara") }
            OylOutlinedButton(onClick = {
                try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }
                catch (_: Exception) { error = "Abre Ajustes del teléfono → Aplicaciones → OYL → Permisos → Cámara." }
            }) { Text("Abrir ajustes de permisos") }
        } else {
            val previewView = remember(context) { PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            } }
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(300.dp))
            Text(error ?: "Buscando QR… Acerca el código y mantén la cámara estable.", style = MaterialTheme.typography.bodyMedium)
            if (error != null) OylOutlinedButton(onClick = { retry++ }) { Text("Reintentar cámara") }
            DisposableEffect(owner, previewView, retry) {
                error = null
                val disposed = AtomicBoolean(false)
                val delivered = AtomicBoolean(false)
                val processing = AtomicBoolean(false)
                val executor = ContextCompat.getMainExecutor(context)
                val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                var provider: ProcessCameraProvider? = null
                var cameraStates: androidx.lifecycle.LiveData<androidx.camera.core.CameraState>? = null
                val cameraObserver = androidx.lifecycle.Observer<androidx.camera.core.CameraState> { state ->
                    if (!disposed.get() && state.error != null) error = "La cámara no está disponible. Cierra otras aplicaciones y reintenta."
                }
                analysis.setAnalyzer(executor) { frame ->
                    if (disposed.get() || delivered.get() || !processing.compareAndSet(false, true)) {
                        frame.close()
                    } else {
                        val image = frame.image
                        if (image == null) {
                            processing.set(false); frame.close()
                        } else try {
                            scanner.process(InputImage.fromMediaImage(image, frame.imageInfo.rotationDegrees))
                                .addOnSuccessListener { codes ->
                                    if (!disposed.get()) {
                                        val code = codes.firstOrNull { qrProductId(it.rawValue.orEmpty()) != null } ?: codes.firstOrNull()
                                        code?.rawValue?.let { value ->
                                            if (delivered.compareAndSet(false, true)) currentPayload(value)
                                        }
                                    }
                                }
                                .addOnFailureListener { if (!disposed.get()) error = "No se pudo leer el QR. Reintenta o usa otra imagen." }
                                .addOnCompleteListener { processing.set(false); frame.close() }
                        } catch (_: Exception) {
                            processing.set(false); frame.close()
                            if (!disposed.get()) error = "No se pudo analizar la cámara. Pulsa Reintentar cámara."
                        }
                    }
                }
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    if (!disposed.get()) try {
                        val cameraProvider = future.get()
                        provider = cameraProvider
                        val selector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) CameraSelector.DEFAULT_BACK_CAMERA
                            else CameraSelector.DEFAULT_FRONT_CAMERA
                        val camera = cameraProvider.bindToLifecycle(owner, selector, preview, analysis)
                        cameraStates = camera.cameraInfo.cameraState
                        cameraStates?.observe(owner, cameraObserver)
                    } catch (_: Exception) {
                        error = "No se pudo abrir la cámara. Reintenta o usa galería o simulador."
                    }
                }, executor)
                onDispose {
                    disposed.set(true)
                    cameraStates?.removeObserver(cameraObserver)
                    analysis.clearAnalyzer()
                    provider?.unbind(preview, analysis)
                    scanner.close()
                }
            }
        }
    }
}
