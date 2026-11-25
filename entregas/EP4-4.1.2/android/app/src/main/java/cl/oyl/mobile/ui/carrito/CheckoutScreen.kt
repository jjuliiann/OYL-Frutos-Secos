package cl.oyl.mobile.ui.carrito

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.data.local.entity.Session
import cl.oyl.mobile.model.checkout.CheckoutData
import cl.oyl.mobile.model.mapa.PickupLocation
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.model.pedidos.PurchaseOrder
import cl.oyl.mobile.ui.components.*
import cl.oyl.mobile.util.CheckoutValidation
import cl.oyl.mobile.util.cartTotal
import cl.oyl.mobile.util.money

@Composable
fun CheckoutScreen(
    cart: List<CartLine>, products: List<Product>, confirmedOrder: Long?, busy: Boolean,
    locations: List<PickupLocation>, session: Session?, completedOrder: PurchaseOrder?,
    confirm: (CheckoutData, (Long) -> Unit) -> Unit,
    after: (Long) -> Unit, orders: () -> Unit, catalog: () -> Unit
) {
    var delivery by rememberSaveable { mutableStateOf("RETIRO") }
    var name by rememberSaveable(session?.id) { mutableStateOf(session?.name.orEmpty()) }
    var phone by rememberSaveable(session?.id) { mutableStateOf(session?.phone.orEmpty()) }
    var email by rememberSaveable(session?.id) { mutableStateOf(session?.email.orEmpty()) }
    var pickupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var street by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var apartment by rememberSaveable { mutableStateOf("") }
    var commune by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }
    var postal by rememberSaveable { mutableStateOf("") }
    var instructions by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    fun clear(key: String) { errors = errors - key }
    val input = CheckoutData(delivery, name, phone, email, pickupId, street, number, apartment, commune, region, postal, instructions)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (confirmedOrder != null) {
            ScreenTitle("¡Pedido registrado!", "Pedido #$confirmedOrder · Pendiente de validación")
            completedOrder?.let { order ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    order.fulfillment?.let { FulfillmentSummary(it) }
                    Text("Total de productos: ${money(order.total)}", fontWeight = FontWeight.Bold)
                } }
            }
            ActionButton("Ver mis pedidos", onClick = orders)
            OylOutlinedButton(onClick = catalog, modifier = Modifier.fillMaxWidth()) { Text("Seguir comprando") }
        } else {
            ScreenTitle("Confirmar compra", "Revisa tu pedido y elige cómo recibirlo")
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tu pedido", style = MaterialTheme.typography.titleMedium)
                cart.forEach { line ->
                    val product = products.firstOrNull { it.id == line.productId }
                    Text("${product?.name ?: "Producto"} × ${line.quantity} · ${money((product?.price ?: 0) * line.quantity)}")
                }
                Text("Subtotal: ${money(cartTotal(cart,products))}", fontWeight = FontWeight.Bold)
            } }
            Text("¿Cómo quieres recibirlo?", style = MaterialTheme.typography.titleMedium)
            listOf("RETIRO" to "Retiro en tienda", "ENTREGA" to "Envío a domicilio").forEach { (mode,label) ->
                Card(Modifier.fillMaxWidth().clickable(enabled = !busy) { delivery = mode; errors = emptyMap() },
                    colors = CardDefaults.cardColors(containerColor = if (delivery == mode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = delivery == mode, onClick = { delivery = mode; errors = emptyMap() }, enabled = !busy)
                        Text(label, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            errors["delivery"]?.let { Text(it,color = MaterialTheme.colorScheme.error) }
            Text(if (delivery == "RETIRO") "Datos de quien retira" else "Datos del destinatario", style = MaterialTheme.typography.titleMedium)
            FormField(name, { name = it; clear("recipientName") }, "Nombre completo", error = errors["recipientName"])
            FormField(phone, { phone = it; clear("phone") }, "Teléfono de contacto", error = errors["phone"])
            FormField(email, { email = it; clear("email") }, "Correo electrónico (opcional)", error = errors["email"])
            AnimatedContent(targetState = delivery, label = "Modalidad de recepción") { mode ->
                Column(Modifier.fillMaxWidth().animateContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (mode == "RETIRO") {
                        Text("Selecciona la tienda de retiro", style = MaterialTheme.typography.titleMedium)
                        if (locations.isEmpty()) Text("No hay puntos de retiro disponibles. Vuelve a intentarlo en unos instantes.")
                        locations.forEach { location ->
                            Card(Modifier.fillMaxWidth().clickable(enabled = !busy) { pickupId = location.id; clear("pickupLocationId") },
                                colors = CardDefaults.cardColors(containerColor = if (pickupId == location.id) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = pickupId == location.id, onClick = { pickupId = location.id; clear("pickupLocationId") }, enabled = !busy)
                                    Column(Modifier.weight(1f)) {
                                        Text(location.name, fontWeight = FontWeight.Bold)
                                        Text(location.address)
                                        Text(location.hours, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        errors["pickupLocationId"]?.let { Text(it,color = MaterialTheme.colorScheme.error) }
                        Text("Te avisaremos cuando tu pedido esté preparado para retirar.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Dirección de envío", style = MaterialTheme.typography.titleMedium)
                        FormField(street, { street = it; clear("street") }, "Calle o avenida", error = errors["street"])
                        FormField(number, { number = it; clear("number") }, "Número (o S/N)", error = errors["number"])
                        FormField(apartment, { apartment = it; clear("apartment") }, "Departamento, casa o bloque (opcional)", error = errors["apartment"])
                        FormField(commune, { commune = it; clear("commune") }, "Comuna", error = errors["commune"])
                        FormField(region, { region = it; clear("region") }, "Región", error = errors["region"])
                        FormField(postal, { postal = it; clear("postalCode") }, "Código postal (opcional)", numeric = true, error = errors["postalCode"])
                    }
                }
            }
            OutlinedTextField(value = instructions, onValueChange = { instructions = it; clear("instructions") },
                label = { Text(if (delivery == "RETIRO") "Observaciones del retiro (opcional)" else "Indicaciones de entrega (opcional)") },
                minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth(), isError = errors["instructions"] != null,
                supportingText = { Text(errors["instructions"] ?: "${instructions.length}/300 caracteres") })
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Total de productos: ${money(cartTotal(cart,products))}", fontWeight = FontWeight.Bold)
                Text(if (delivery == "RETIRO") "Retiro en tienda sin costo de envío." else "El costo y plazo del envío se coordinan con la tienda; no están incluidos en este subtotal.", style = MaterialTheme.typography.bodySmall)
            } }
            if (errors.isNotEmpty()) Text("Revisa los campos marcados antes de confirmar.", color = MaterialTheme.colorScheme.error)
            ActionButton(if (busy) "Guardando pedido…" else "Confirmar pedido", enabled = cart.isNotEmpty() && !busy && session != null, onClick = {
                errors = CheckoutValidation.validate(input,locations.map { it.id }.toSet())
                if (errors.isEmpty()) confirm(input.normalized(),after)
            })
        }
    }
}
