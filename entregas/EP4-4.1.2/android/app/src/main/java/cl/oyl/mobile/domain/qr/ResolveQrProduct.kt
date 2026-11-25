package cl.oyl.mobile.domain.qr
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.model.qr.QrResolution

/** La búsqueda real se inyecta; códigos inválidos no ejecutan findProduct ni crean productos. */
suspend fun resolveQrProduct(payload: String?, external: Boolean, canManage: Boolean,
                             findProduct: suspend (Long) -> Product?): QrResolution {
    if (payload == null) return QrResolution.NoCode
    val id = if (external) externalQrProductId(payload) else qrProductId(payload)
    if (id == null) return if (external) QrResolution.InvalidLink else QrResolution.Invalid
    val product = findProduct(id) ?: return QrResolution.Missing(id)
    if (!product.active && !canManage) return QrResolution.Inactive
    return QrResolution.Found(product)
}
