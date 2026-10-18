package cl.oyl.mobile

// Reglas puras compartidas; la UI sólo presenta el mapa de errores.
object Validation {
    fun login(alias: String, password: String): Map<String, String> = buildMap {
        if (alias.isBlank()) put("alias", "Ingresa tu alias")
        if (password.isBlank()) put("password", "Ingresa tu contraseña")
    }
    fun profile(name: String, email: String, phone: String, address: String): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Ingresa tu nombre")
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) put("email", "Ingresa un correo válido de prueba")
        if (!Regex("^[+0-9() -]{7,20}$").matches(phone.trim())) put("phone", "Ingresa entre 7 y 20 caracteres de teléfono")
        if (address.isBlank()) put("address", "Ingresa una dirección de prueba")
    }
    fun register(data: RegisterData): Map<String, String> = buildMap {
        putAll(profile(data.name, data.email, data.phone, data.address))
        if (data.alias.isBlank()) put("alias", "Ingresa un alias")
        if (data.password.length < 8) put("password", "Usa al menos 8 caracteres")
    }
    fun product(name: String, category: String, description: String, presentation: String,
                price: String, stock: String, minStock: String): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Ingresa el nombre")
        if (category !in listOf("Frutos secos", "Semillas", "Deshidratados")) put("category", "Usa Frutos secos, Semillas o Deshidratados")
        if (description.isBlank()) put("description", "Ingresa una descripción")
        if (presentation.isBlank()) put("presentation", "Ingresa la presentación")
        listOf("price" to price, "stock" to stock, "minStock" to minStock).forEach { (key, value) ->
            if (value.toIntOrNull() == null || (value.toIntOrNull() ?: -1) < 0) put(key, "Ingresa un entero mayor o igual a cero")
        }
    }
    fun contact(value: String): String? = if (value.isBlank()) "Ingresa un contacto de prueba" else null
    fun quantity(value: String): String? = if ((value.toIntOrNull() ?: 0) <= 0) "Ingresa una cantidad entera mayor a cero" else null
}
