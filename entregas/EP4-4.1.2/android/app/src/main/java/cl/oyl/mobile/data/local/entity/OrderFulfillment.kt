package cl.oyl.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import cl.oyl.mobile.model.checkout.FulfillmentDetails

@Entity(tableName = "order_fulfillment", foreignKeys = [ForeignKey(entity = StoredOrder::class, parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE)])
data class OrderFulfillment(
    @PrimaryKey val orderId: Long,
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
) {
    fun details() = FulfillmentDetails(
        delivery = delivery,
        recipientName = recipientName,
        phone = phone,
        email = email,
        pickupLocationId = pickupLocationId,
        pickupName = pickupName,
        pickupAddress = pickupAddress,
        pickupHours = pickupHours,
        street = street,
        number = number,
        apartment = apartment,
        commune = commune,
        region = region,
        postalCode = postalCode,
        instructions = instructions
    )
}
