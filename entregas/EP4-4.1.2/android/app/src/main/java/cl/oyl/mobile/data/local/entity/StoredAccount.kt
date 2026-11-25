package cl.oyl.mobile.data.local.entity

import androidx.room.*
import cl.oyl.mobile.model.auth.Account

@Entity(tableName = "accounts", indices = [Index(value = ["alias"], unique = true)])
data class StoredAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String, val name: String, val email: String, val phone: String,
    val address: String, val role: String = "CLIENT", val salt: String, val passwordHash: String
) {
    fun account() = Account(id, alias, name, email, phone, address, role)
}
