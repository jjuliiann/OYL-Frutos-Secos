package cl.oyl.mobile

import java.text.Normalizer
import java.util.Locale

// No usa IDs de base de datos: no cambian contratos y no depende del orden de inserción.
// Nombres canónicos exactos, sin coincidencias parciales que asignen otro producto.
fun productImageResource(name: String): Int {
    val key = Normalizer.normalize(name.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
    return when (key) {
        "almendras" -> R.drawable.almendras
        "nueces" -> R.drawable.nueces
        "mix premium", "mix premium de frutos secos" -> R.drawable.mix_premium
        "pasas" -> R.drawable.pasas
        "semillas de chia", "chia" -> R.drawable.chia
        "cranberries" -> R.drawable.cranberries
        "avellanas" -> R.drawable.avellanas
        else -> R.drawable.producto_predeterminado
    }
}
