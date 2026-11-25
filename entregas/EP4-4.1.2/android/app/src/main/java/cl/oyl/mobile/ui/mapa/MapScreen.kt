package cl.oyl.mobile.ui.mapa

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.model.mapa.PickupLocation
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ScreenTitle

@Composable
fun MapScreen(locations: List<PickupLocation>, reload: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val place = locations.firstOrNull { it.id == selectedId } ?: locations.firstOrNull()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenTitle("Mapa y puntos de retiro", "Selecciona un punto y consulta cómo llegar")
        if (place == null) {
            Text("No hay puntos de retiro disponibles. Vuelve a cargar.")
            OylOutlinedButton(onClick = reload) { Text("Volver a cargar puntos") }
        } else {
            PickupMap(place, locations, onSelect = { selectedId = it })
            androidx.compose.animation.AnimatedContent(targetState = place, label = "Detalles del punto") { selected ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(selected.name, fontWeight = FontWeight.Bold)
                    Text(selected.address)
                    Text(selected.hours)
                    Text("Coordenadas: ${selected.latitude}, ${selected.longitude}", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    Text("Consulta la dirección y el horario del punto seleccionado.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                } }
            }
            var directionError by remember { mutableStateOf(false) }
            OylOutlinedButton(onClick = {
                val url = "https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}"
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); directionError = false }
                catch (_: Exception) { directionError = true }
            }, modifier = Modifier.fillMaxWidth()) { Text("Cómo llegar a este punto") }
            if (directionError) Text("No se encontró una aplicación para abrir la dirección.")
        }
        locations.forEach { location ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp)) {
                    Text(location.name, fontWeight = FontWeight.Bold)
                    Text(location.address)
                    OylTextButton(onClick = { selectedId = location.id }) { Text(if (location.id == place?.id) "Punto seleccionado" else "Ver marcador y detalles") }
                }
            }
        }
    }
}
