package cl.oyl.mobile.data.local.entity

import androidx.room.*

@Entity(tableName = "orders", foreignKeys = [ForeignKey(entity = StoredAccount::class, parentColumns = ["id"], childColumns = ["userId"])], indices = [Index("userId")])
data class StoredOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, val createdAt: String, val status: String,
    val delivery: String, val contact: String, val total: Int
)
