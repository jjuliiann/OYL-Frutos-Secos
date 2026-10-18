package cl.oyl.mobile

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun statusLabel(value: String): String = when (value) {
    "PENDIENTE_VALIDACION" -> "Pendiente de validación"
    "CONFIRMADO" -> "Confirmado"
    "PREPARADO" -> "Preparado"
    "ENTREGADO" -> "Entregado"
    else -> value
}
fun displayDate(value: String): String {
    val format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))
    return try { Instant.parse(value).atZone(ZoneId.systemDefault()).format(format) }
    catch (_: Exception) {
        try { LocalDateTime.parse(value).format(format) }
        catch (_: Exception) { value }
    }
}
