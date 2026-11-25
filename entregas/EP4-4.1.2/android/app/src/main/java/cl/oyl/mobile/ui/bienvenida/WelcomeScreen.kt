package cl.oyl.mobile.ui.bienvenida

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.R
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.ui.components.ActionButton
import cl.oyl.mobile.ui.components.OylOutlinedButton
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.components.ProductPicture
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onCatalog: () -> Unit, onMap: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(70); shown = true }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE5EFDE), Color(0xFFFAF8F2))))
        .verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimatedVisibility(shown, enter = fadeIn(tween(650)) + scaleIn(tween(650), initialScale = .8f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.oyl_logo), "Logo OYL: hojas y fruto seco", Modifier.size(112.dp))
                Text("OYL", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = Color(0xFF1B6A43))
                Text("FRUTOS SECOS", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1B6A43))
            }
        }
        Text("Un pequeño hábito.\nMucho sabor natural.", textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Frutos secos, semillas y mezclas para acompañar tu día.", textAlign = TextAlign.Center, color = Color(0xFF53604F))
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Almendras", "Nueces", "Mix Premium").forEach { name ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProductPicture(Product(name = name), pictureSize = 76.dp)
                        Text(name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        AnimatedVisibility(shown, enter = fadeIn(tween(750)) + slideInVertically(tween(750)) { it / 2 }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Comenzar · Iniciar sesión", onClick = onLogin)
                OylOutlinedButton(onClick = onCatalog, modifier = Modifier.fillMaxWidth()) { Text("Explorar catálogo") }
                OylTextButton(onClick = onMap, modifier = Modifier.fillMaxWidth()) { Text("Conocer puntos de retiro") }
            }
        }
        Text("Naturalmente cerca de ti.", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
    }
}
