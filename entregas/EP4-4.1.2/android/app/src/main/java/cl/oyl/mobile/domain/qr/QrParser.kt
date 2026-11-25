package cl.oyl.mobile.domain.qr

/** Lectura compatible con QR antiguos y enlaces de producto. No acepta hosts ni rutas arbitrarios. */
fun qrProductId(value: String): Long? {
    val text = value.trim()
    val match = Regex("""^(?:OYL:|oyl://product/|https://oyl\.demo/product/)?([1-9][0-9]*)$""")
        .matchEntire(text) ?: return null
    return match.groupValues[1].toLongOrNull()
}

/** Sólo este formato está registrado para abrir OYL desde otra aplicación. */
fun externalQrProductId(value: String): Long? =
    if (value.startsWith("oyl://product/")) qrProductId(value) else null

fun productQrLink(id: Long): String {
    require(id > 0) { "El identificador debe ser positivo" }
    return "oyl://product/$id"
}
