package cl.oyl.mobile.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.model.productos.Product

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
