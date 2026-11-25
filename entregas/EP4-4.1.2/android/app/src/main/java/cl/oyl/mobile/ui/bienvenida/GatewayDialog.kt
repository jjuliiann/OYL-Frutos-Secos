package cl.oyl.mobile.ui.bienvenida

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.MainActivity
import cl.oyl.mobile.data.remote.GatewayAddress
import cl.oyl.mobile.data.remote.RemoteApis
import kotlinx.coroutines.*

@Composable
internal fun GatewayDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var address by remember { mutableStateOf(GatewayAddress.current(ctx)) }
    var message by remember { mutableStateOf("") }
    var testing by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Servidor de aula") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("En el emulador usa la dirección predeterminada. En el celular usa la IP del computador conectado a la misma Wi-Fi.")
            OutlinedTextField(address, { address = it; verified = null; message = "" },
                enabled = !testing, singleLine = true, label = { Text("Dirección de la API") })
            TextButton(enabled = !testing, onClick = { address = GatewayAddress.emulator; verified = null; message = "" }) { Text("Usar emulador") }
            if (testing) LinearProgressIndicator()
            if (message.isNotEmpty()) Text(message)
        } },
        confirmButton = { TextButton(enabled = !testing, onClick = {
            val selected = verified
            if (selected == null) scope.launch {
                testing = true
                try {
                    val url = GatewayAddress.normalize(address)
                    val api = RemoteApis.publicProbe(url)
                    for (service in listOf("users", "products", "orders")) {
                        message = "Consultando $service…"
                        val response = api.serviceHealth(service)
                        check(response["status"] == "ok" && response["service"] == service) { "Respuesta inesperada de $service" }
                    }
                    verified = url; message = "Usuarios, productos y pedidos respondieron."
                } catch (e: CancellationException) { throw e }
                  catch (e: Exception) { message = e.localizedMessage ?: "No se pudo alcanzar el servidor." }
                finally { testing = false }
            } else if (selected == GatewayAddress.current(ctx)) onDismiss()
            else scope.launch {
                if (withContext(Dispatchers.IO) { GatewayAddress.save(ctx, selected) }) {
                    ctx.startActivity(Intent(ctx, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                } else message = "No se pudo guardar la dirección."
            }
        }) { Text(if (verified == null) "Probar conexión" else "Usar esta conexión") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}
