package cl.oyl.mobile.data.local.entity

import androidx.room.*
import cl.oyl.mobile.model.mapa.PickupLocation

@Entity(tableName = "pickup_locations")
data class StoredLocation(
    @PrimaryKey val id: Long, val name: String, val address: String,
    val hours: String, val latitude: Double, val longitude: Double
) { fun location() = PickupLocation(id, name, address, hours, latitude, longitude) }
