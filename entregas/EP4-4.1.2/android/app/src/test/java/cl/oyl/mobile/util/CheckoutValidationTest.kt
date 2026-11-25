package cl.oyl.mobile.util

import cl.oyl.mobile.model.checkout.CheckoutData
import org.junit.Test
import org.junit.Assert.*

class CheckoutValidationTest {
    private val pickup = CheckoutData(recipientName = "Julian Martinez", phone = "+56912345678", pickupLocationId = 1)
    @Test fun pickupRequiresExistingStoreButNoShippingAddress() {
        assertTrue(CheckoutValidation.validate(pickup,setOf(1L,2L)).isEmpty())
        assertEquals(setOf("pickupLocationId"),CheckoutValidation.validate(pickup.copy(pickupLocationId = 99),setOf(1L)).keys)
        assertTrue(CheckoutValidation.validate(pickup.copy(pickupLocationId = null),setOf(1L)).containsKey("pickupLocationId"))
    }
    @Test fun shippingRequiresFullAddress() {
        val errors = CheckoutValidation.validate(pickup.copy(delivery = "ENTREGA"),emptySet())
        assertEquals(setOf("street","number","commune","region"),errors.keys)
    }
    @Test fun shippingAcceptsCompleteAddressWithoutStore() {
        val shipping = pickup.copy(delivery = "ENTREGA", pickupLocationId = null, street = "Avenida Central", number = "S/N", commune = "Santiago", region = "Metropolitana")
        assertTrue(CheckoutValidation.validate(shipping,emptySet()).isEmpty())
    }
    @Test fun rejectsMissingRecipientAndPhone() {
        val errors = CheckoutValidation.validate(pickup.copy(recipientName = " ", phone = "abc"),setOf(1L))
        assertEquals(setOf("recipientName","phone"),errors.keys)
        assertTrue(CheckoutValidation.validate(pickup.copy(phone = "123"),setOf(1L)).containsKey("phone"))
    }
    @Test fun optionalEmailAndPostalAreValidatedWhenPresent() {
        assertTrue(CheckoutValidation.validate(pickup.copy(email = "julian@example.com"),setOf(1L)).isEmpty())
        assertTrue(CheckoutValidation.validate(pickup.copy(email = "julian example.com"),setOf(1L)).containsKey("email"))
        val shipping = pickup.copy(delivery = "ENTREGA", street = "Central", number = "123", commune = "Santiago", region = "Metropolitana", postalCode = "1234567")
        assertTrue(CheckoutValidation.validate(shipping,emptySet()).isEmpty())
        assertTrue(CheckoutValidation.validate(shipping.copy(postalCode = "1234"),emptySet()).containsKey("postalCode"))
    }
    @Test fun normalizationKeepsFieldsAndTrimsSpaces() {
        val value = pickup.copy(recipientName = " Julian Martinez ", street = " Central ", instructions = " Portón azul ").normalized()
        assertEquals("Julian Martinez",value.recipientName)
        assertEquals("Central",value.street)
        assertEquals("Portón azul",value.instructions)
        assertEquals(1L,value.pickupLocationId)
    }
    @Test fun rejectsUnknownModeAndExcessiveInstructions() {
        val errors = CheckoutValidation.validate(pickup.copy(delivery = "OTRO",instructions = "a".repeat(301)),setOf(1L))
        assertEquals(setOf("delivery","instructions"),errors.keys)
    }
}
