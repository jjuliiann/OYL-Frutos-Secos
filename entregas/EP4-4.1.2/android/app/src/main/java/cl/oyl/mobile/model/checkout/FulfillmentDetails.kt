package cl.oyl.mobile.model.checkout

/** Copia de los datos de entrega en el momento de comprar, independiente del perfil actual. */
data class FulfillmentDetails(
    val delivery: String,
    val recipientName: String,
    val phone: String,
    val email: String,
    val pickupLocationId: Long?,
    val pickupName: String,
    val pickupAddress: String,
    val pickupHours: String,
    val street: String,
    val number: String,
    val apartment: String,
    val commune: String,
    val region: String,
    val postalCode: String,
    val instructions: String
)
