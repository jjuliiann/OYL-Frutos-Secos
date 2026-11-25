package cl.oyl.mobile.data.local.entity

import androidx.room.Entity

@Entity(tableName = "cached_orders", primaryKeys = ["userId", "orderId"])
data class CachedOrder(val userId: Long, val orderId: Long, val total: Int, val status: String, val createdAt: String)
