package cl.oyl.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.model.checkout.FulfillmentDetails

@Composable
fun FulfillmentSummary(details: FulfillmentDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (details.delivery == "RETIRO") "Retiro en tienda" else "Envío a domicilio", fontWeight = FontWeight.Bold)
        Text("${details.recipientName} · ${details.phone}")
        if (details.email.isNotBlank()) Text(details.email)
        if (details.delivery == "RETIRO") {
            Text(details.pickupName, color = MaterialTheme.colorScheme.primary)
            Text(details.pickupAddress)
            Text(details.pickupHours)
        } else {
            Text("${details.street} ${details.number}")
            if (details.apartment.isNotBlank()) Text("Departamento / casa: ${details.apartment}")
            Text("${details.commune}, ${details.region}")
            if (details.postalCode.isNotBlank()) Text("Código postal: ${details.postalCode}")
        }
        if (details.instructions.isNotBlank()) Text("Indicaciones: ${details.instructions}")
    }
}
