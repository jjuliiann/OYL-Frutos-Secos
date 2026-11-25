package cl.oyl.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_session")
data class Session(
    @PrimaryKey val key: Int = 1,
    val id: Long,
    val alias: String,
    val name: String,
    val role: String,
    val email: String = "",
    val phone: String = "",
    val address: String = ""
)
