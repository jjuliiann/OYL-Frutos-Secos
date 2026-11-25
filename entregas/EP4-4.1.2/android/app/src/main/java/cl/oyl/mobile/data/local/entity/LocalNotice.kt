package cl.oyl.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_notices")
data class LocalNotice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false
)
