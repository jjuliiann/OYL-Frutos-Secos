package cl.oyl.mobile.model.pedidos

data class OrderLine(
    val id: Long, val productId: Long, val productName: String,
    val quantity: Int, val unitPrice: Int
)
