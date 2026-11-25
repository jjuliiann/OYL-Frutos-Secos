package cl.oyl.mobile.model.checkout

/** Datos de una nueva compra. La dirección solo es obligatoria para envío. */
data class CheckoutData(
    val delivery: String = "RETIRO",
    val recipientName: String = "",
    val phone: String = "",
    val email: String = "",
    val pickupLocationId: Long? = null,
    val street: String = "",
    val number: String = "",
    val apartment: String = "",
    val commune: String = "",
    val region: String = "",
    val postalCode: String = "",
    val instructions: String = ""
) {
    fun normalized() = copy(recipientName = recipientName.trim(), phone = phone.trim(),
        email = email.trim(), street = street.trim(), number = number.trim(), apartment = apartment.trim(),
        commune = commune.trim(), region = region.trim(), postalCode = postalCode.trim(), instructions = instructions.trim())
}
