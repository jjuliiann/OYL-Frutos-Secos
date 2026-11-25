package cl.oyl.mobile.model.auth

data class Account(
    val id: Long,
    val alias: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val role: String = "CLIENT",
    val token: String? = null
)
