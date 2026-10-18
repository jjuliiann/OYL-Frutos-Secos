package cl.oyl.mobile

data class Account(
    val id: Long,
    val alias: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val role: String = "CLIENT"
)
data class Credentials(val alias: String, val password: String)
data class RegisterData(
    val alias: String, val password: String, val name: String,
    val email: String, val phone: String, val address: String
)
data class ProfileUpdate(val name: String, val email: String, val phone: String, val address: String)
data class Product(
    val id: Long? = null,
    val name: String = "",
    val category: String = "Frutos secos",
    val description: String = "",
    val imageUri: String = "",
    val presentation: String = "250 g",
    val price: Int = 0,
    val stock: Int = 0,
    val minStock: Int = 5,
    val active: Boolean = true
)
data class OrderItem(val productId: Long, val quantity: Int)
data class OrderRequest(val userId: Long, val delivery: String, val contact: String, val items: List<OrderItem>)
data class OrderLine(
    val id: Long, val productId: Long, val productName: String,
    val quantity: Int, val unitPrice: Int
)
data class PurchaseOrder(
    val id: Long, val userId: Long, val createdAt: String,
    val status: String, val delivery: String, val contact: String,
    val total: Int, val lines: List<OrderLine> = emptyList()
)
data class StockChange(val delta: Int)
data class StatusChange(val status: String)
data class PickupLocation(
    val id: Long, val name: String, val address: String,
    val hours: String, val latitude: Double, val longitude: Double
)
fun money(amount: Int): String = "$" + "%,d".format(java.util.Locale.US, amount).replace(',', '.')
fun cartTotal(lines: List<CartLine>, products: List<Product>): Int = lines.sumOf { line ->
    (products.firstOrNull { it.id == line.productId }?.price ?: 0) * line.quantity
}
