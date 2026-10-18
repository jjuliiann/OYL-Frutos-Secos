package cl.oyl.mobile
import org.junit.Assert.*
import org.junit.Test

class ValidationTest {
    @Test fun registrationErrorsByField() {
        val errors = Validation.register(RegisterData("", "123", "", "bad", "x", ""))
        assertEquals(setOf("alias", "password", "name", "email", "phone", "address"), errors.keys)
        assertTrue(Validation.register(RegisterData("cliente", "Clave1234", "Cliente", "demo@example.com", "+56 9 1234 1234", "Dirección ficticia")).isEmpty())
    }
    @Test fun productRejectsOverflowAndNegatives() {
        assertEquals(setOf("price", "stock", "minStock"), Validation.product("Nueces", "Frutos secos", "Descripción", "250 g", "2147483648", "-1", "abc").keys)
        assertTrue(Validation.product("Nueces", "Frutos secos", "Descripción", "250 g", "0", "0", "0").isEmpty())
    }
    @Test fun quantityAndContact() {
        assertNotNull(Validation.quantity("0"))
        assertNotNull(Validation.quantity("-2"))
        assertNotNull(Validation.quantity("999999999999"))
        assertNull(Validation.quantity("2"))
        assertNotNull(Validation.contact(" "))
    }
    @Test fun dateAcceptsBothServerFormatsAndKeepsUnknown() {
        assertEquals("17/10/2026 20:30", displayDate("2026-10-17T20:30"))
        assertEquals("fecha desconocida", displayDate("fecha desconocida"))
        assertFalse(displayDate("2026-10-17T20:30:00Z").contains("T"))
    }
    @Test fun imagesUseExactNormalizedNames() {
        assertEquals(R.drawable.chia, productImageResource(" Semillas de CHÍA "))
        assertEquals(R.drawable.avellanas, productImageResource("Avellanas"))
        assertEquals(R.drawable.producto_predeterminado, productImageResource("Chocolate de almendras"))
        assertEquals(R.drawable.producto_predeterminado, productImageResource("Producto nuevo"))
    }
}
