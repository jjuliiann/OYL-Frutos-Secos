package cl.oyl.mobile.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    if (subtitle != null) {
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(12.dp))
}
