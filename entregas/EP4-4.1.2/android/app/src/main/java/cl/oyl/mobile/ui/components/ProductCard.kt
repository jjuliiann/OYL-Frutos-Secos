package cl.oyl.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.util.money

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
            if (add != null) OylIconButton(onClick = add, enabled = p.stock > 0) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = "Agregar al carrito")
            }
        }
    }
}
