package cl.oyl.mobile.model.mapa

data class PickupLocation(
    val id: Long, val name: String, val address: String,
    val hours: String, val latitude: Double, val longitude: Double
)
