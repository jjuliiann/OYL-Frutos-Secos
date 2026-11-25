package cl.oyl.mobile.model.checkout

data class OrderRequest(
    val userId: Long,
    val delivery: String,
    val contact: String,
    val items: List<OrderItem>,
    val fulfillment: FulfillmentDetails? = null
)
