package cl.oyl.mobile

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.remember
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    if (subtitle != null) {
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
fun FormField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    numeric: Boolean = false,
    password: Boolean = false,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        trailingIcon = { if (error != null) Icon(Icons.Default.ErrorOutline, contentDescription = "Error") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = when {
            numeric -> KeyboardType.Number
            password -> KeyboardType.Password
            else -> KeyboardType.Text
        }),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
fun ProductPicture(p: Product, modifier: Modifier = Modifier, pictureSize: androidx.compose.ui.unit.Dp = 72.dp) {
    val bitmap = remember(p.imageUri) { try {
        if (!p.imageUri.startsWith("data:image/")) null
        else {
            val encoded = p.imageUri.substringAfter("base64,", "")
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    } catch (_: Exception) { null } }
    Box(
        modifier.size(pictureSize).background(Color(0xFFEDF4E6), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap == null) Image(painterResource(productImageResource(p.name)), contentDescription = p.name, modifier = Modifier.fillMaxWidth().height(pictureSize).padding(4.dp), contentScale = ContentScale.Fit)
        else Image(bitmap.asImageBitmap(), contentDescription = p.name, modifier = Modifier.fillMaxWidth().height(pictureSize).padding(4.dp), contentScale = ContentScale.Crop)
    }
}

@Composable
fun ProductCard(p: Product, open: () -> Unit, add: (() -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = open).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductPicture(p)
            Column(Modifier.weight(1f)) {
                Text(p.name, fontWeight = FontWeight.Bold)
                Text("${p.category} · ${p.presentation}", style = MaterialTheme.typography.bodySmall)
                Text(money(p.price), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("Stock: ${p.stock}", style = MaterialTheme.typography.labelSmall,
                    color = if (p.stock <= p.minStock) Color(0xFFB34B31) else Color.Gray)
            }
            if (add != null) IconButton(onClick = add, enabled = p.stock > 0) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = "Agregar al carrito")
            }
        }
    }
}
