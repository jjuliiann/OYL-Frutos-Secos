package cl.oyl.mobile.model.auth

data class RegisterData(
    val alias: String, val password: String, val name: String,
    val email: String, val phone: String, val address: String
)
