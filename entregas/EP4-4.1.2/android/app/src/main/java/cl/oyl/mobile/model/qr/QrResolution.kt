package cl.oyl.mobile.model.qr
import cl.oyl.mobile.model.productos.Product

sealed class QrResolution(val message: String) {
    data object Idle : QrResolution("")
    data object NoCode : QrResolution("No se detectó un QR. Usa una imagen nítida y completa.")
    data object Invalid : QrResolution("El código QR no corresponde a un producto OYL.")
    data object InvalidLink : QrResolution("El enlace de producto no es válido.")
    data class Missing(val productId: Long) : QrResolution("Producto inexistente. No existe un producto con el código #$productId.")
    data object Inactive : QrResolution("Este producto está desactivado.")
    data class Found(val product: Product) : QrResolution("Producto encontrado")
    data object Failure : QrResolution("No se pudo consultar el producto. Vuelve a intentarlo.")
}
