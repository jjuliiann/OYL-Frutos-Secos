package cl.oyl.mobile.util

import cl.oyl.mobile.model.checkout.CheckoutData

object CheckoutValidation {
    fun validate(input: CheckoutData, pickupIds: Set<Long>): Map<String, String> = buildMap {
        val data = input.normalized()
        if (data.delivery !in setOf("RETIRO", "ENTREGA")) put("delivery", "Selecciona retiro en tienda o envío a domicilio")
        if (data.recipientName.length !in 3..100) put("recipientName", "Ingresa el nombre completo, entre 3 y 100 caracteres")
        val phoneDigits = data.phone.filter(Char::isDigit)
        if (!Regex("^[+0-9() -]+$").matches(data.phone) || phoneDigits.length !in 9..15 || data.phone.length > 25)
            put("phone", "Ingresa un teléfono válido con 9 a 15 dígitos")
        if (data.email.isNotEmpty() && (data.email.length > 120 || !Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""").matches(data.email)))
            put("email", "Ingresa un correo válido o deja el campo vacío")
        if (data.delivery == "RETIRO" && data.pickupLocationId !in pickupIds) put("pickupLocationId", "Selecciona una tienda para el retiro")
        if (data.delivery == "ENTREGA") {
            if (data.street.length !in 2..120) put("street", "Ingresa la calle o avenida")
            if (data.number.isEmpty() || data.number.length > 20) put("number", "Ingresa el número; si no tiene, escribe S/N")
            if (data.apartment.length > 60) put("apartment", "Usa hasta 60 caracteres")
            if (data.commune.length !in 2..80) put("commune", "Ingresa la comuna")
            if (data.region.length !in 2..80) put("region", "Ingresa la región")
            if (data.postalCode.isNotEmpty() && !Regex("^[0-9]{7}$").matches(data.postalCode)) put("postalCode", "Usa 7 dígitos o deja el campo vacío")
        }
        if (data.instructions.length > 300) put("instructions", "Usa hasta 300 caracteres")
    }
}
