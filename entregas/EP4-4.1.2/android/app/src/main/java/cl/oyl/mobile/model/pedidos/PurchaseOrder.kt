package cl.oyl.mobile.model.pedidos

import cl.oyl.mobile.model.checkout.FulfillmentDetails

data class PurchaseOrder(
    val id: Long, val userId: Long, val createdAt: String,
    val status: String, val delivery: String, val contact: String,
    val total: Int, val lines: List<OrderLine> = emptyList(),
    val fulfillment: FulfillmentDetails? = null
)
