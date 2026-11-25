package cl.oyl.mobile.data.local.entity

import androidx.room.Entity

@Entity(tableName = "cart", primaryKeys = ["userId", "productId"])
data class CartLine(val userId: Long, val productId: Long, val quantity: Int)
