package cl.oyl.mobile.data.local.entity

import androidx.room.*

@Entity(tableName = "order_lines", foreignKeys = [ForeignKey(entity = StoredOrder::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE)], indices = [Index("orderId")])
data class StoredLine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val orderId: Long,
    val productId: Long, val productName: String, val quantity: Int, val unitPrice: Int
)
