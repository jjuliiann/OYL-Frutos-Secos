package cl.oyl.mobile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.SystemClock
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.coroutines.resume
import androidx.compose.ui.viewinterop.AndroidView

private suspend fun WebView.mapReport(): String? = withTimeoutOrNull(3000) {
    suspendCancellableCoroutine { continuation ->
        evaluateJavascript("JSON.stringify(window.oylMapReport || {})") { value ->
            if (continuation.isActive) continuation.resume(value)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PickupMap(place: PickupLocation, locations: List<PickupLocation>) {
    val context = LocalContext.current
    val assets = remember(context) {
        runCatching { listOf("leaflet.css", "leaflet.js", "pickup-map.js").map { file ->
            context.assets.open("leaflet/$file").bufferedReader().use { it.readText() }
        } }.getOrNull()
    }
    var attempt by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>("Cargando mapa de OpenStreetMap…") }
    var failed by remember { mutableStateOf(false) }
    // Instancia estable y tamaño MATCH_PARENT: no depende del alto del contenido HTML.
    val web = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            settings.javaScriptEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.userAgentString += " OYLFrutosSecosAcademico/2.2"
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (request.url.scheme == "https" && request.url.host == "www.openstreetmap.org") {
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, request.url)) } catch (_: Exception) { }
                    }
                    return true
                }
            }
        }
    }
    LaunchedEffect(web, place, locations, attempt, assets) {
        message = "Cargando mapa de OpenStreetMap…"
        failed = false
        if (assets == null) {
            failed = true; message = "No se pudieron abrir los recursos del mapa."; return@LaunchedEffect
        }
        val token = UUID.randomUUID().toString()
        val points = JSONArray().apply {
            locations.forEach { p -> put(JSONObject().apply {
                put("id", p.id); put("name", p.name)
                put("latitude", p.latitude); put("longitude", p.longitude)
            }) }
        }.toString().replace("<", "\\u003c")
        val html = """
            <!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>${assets[0]}
            html,body{margin:0;padding:0;width:100%;height:100%;overflow:hidden;background:#edf4e6;}
            #map{position:absolute;top:0;left:0;right:0;bottom:0;min-height:220px;}</style></head>
            <body><div id="map" role="img" aria-label="Puntos de retiro OYL"></div>
            <script>
            ${assets[1]}
            </script><script>
            ${assets[2]}
            initPickupMap($points, ${place.id}, '$token');
            </script></body></html>
        """.trimIndent()
        // Esperar al frame donde AndroidView ya se incorporó al árbol visual.
        withFrameNanos { }
        web.loadDataWithBaseURL("https://oyl.local/", html, "text/html", "UTF-8", null)
        var waitingSince = SystemClock.elapsedRealtime()
        var wasReady = false
        while (isActive) {
            delay(750)
            val raw = web.mapReport()
            val report = runCatching {
                // evaluateJavascript devuelve JSON doblemente codificado para una cadena.
                val decoded = org.json.JSONTokener(raw ?: "null").nextValue() as? String
                if (decoded == null) null else JSONObject(decoded)
            }.getOrNull()
            val current = report?.optString("token") == token
            val ready = report != null && current && report.optString("status") == "ready" &&
                (report.optDouble("width", 0.0) > 0) && (report.optDouble("height", 0.0) > 0) &&
                (report.optInt("visibleTiles", 0) > 0)
            if (ready) {
                message = null; failed = false; wasReady = true
            } else {
                if (wasReady) { waitingSince = SystemClock.elapsedRealtime(); wasReady = false }
                val timedOut = SystemClock.elapsedRealtime() - waitingSince >= 25000
                failed = timedOut
                message = if (timedOut) "No se pudo mostrar el mapa. Comprueba internet y vuelve a intentar."
                    else "Cargando mapa de OpenStreetMap…"
            }
        }
    }
    DisposableEffect(web) { onDispose { web.stopLoading(); web.destroy() } }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.fillMaxWidth().height(220.dp).clipToBounds()) {
            AndroidView(factory = { web }, modifier = Modifier.fillMaxSize())
            // Un estado pendiente/fallido nunca queda representado por un rectángulo vacío.
            if (message != null) Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    if (!failed) CircularProgressIndicator(Modifier.size(28.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(message.orEmpty())
                    if (failed) OutlinedButton(onClick = { attempt++ }) { Text("Reintentar mapa") }
                }
            }
        }
        Text("© OpenStreetMap contributors · Selecciona un punto para centrar su marcador.", style = MaterialTheme.typography.labelSmall)
    }
}
