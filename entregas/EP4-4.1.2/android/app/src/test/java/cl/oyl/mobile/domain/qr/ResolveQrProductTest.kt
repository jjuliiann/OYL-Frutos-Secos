package cl.oyl.mobile.domain.qr

import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.model.qr.QrResolution
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ResolveQrProductTest {
    private val almonds = Product(id = 1, name = "Almendras", price = 2990, stock = 25)

    @Test fun validIdReturnsTheProductFromLookup() = runBlocking {
        var queried: Long? = null
        val result = resolveQrProduct("oyl://product/1", true, false) { id -> queried = id; almonds }
        assertEquals(1L, queried)
        assertSame(almonds, (result as QrResolution.Found).product)
        assertEquals("Producto encontrado", result.message)
    }
    @Test fun missingProductDoesNotCreateAPlaceholder() = runBlocking {
        val result = resolveQrProduct("oyl://product/999999", true, false) { null }
        assertEquals(999999L, (result as QrResolution.Missing).productId)
        assertTrue(result.message.contains("Producto inexistente"))
    }
    @Test fun invalidPayloadNeverQueriesStorage() = runBlocking {
        var calls = 0
        for (value in listOf("basura", "oyl://other/1", "oyl://product/0", "oyl://product/1?admin=true")) {
            assertEquals(QrResolution.Invalid, resolveQrProduct(value, false, false) { calls++; almonds })
        }
        assertEquals(0, calls)
    }
    @Test fun inactiveProductIsHiddenFromClientAndVisibleToAdmin() = runBlocking {
        val inactive = almonds.copy(active = false)
        assertEquals(QrResolution.Inactive, resolveQrProduct("OYL:1", false, false) { inactive })
        assertSame(inactive, (resolveQrProduct("OYL:1", false, true) { inactive } as QrResolution.Found).product)
    }
    @Test fun externalEntryRequiresARegisteredLink() = runBlocking {
        var calls = 0
        assertEquals(QrResolution.InvalidLink, resolveQrProduct("OYL:1", true, false) { calls++; almonds })
        assertEquals(0, calls)
        assertTrue(resolveQrProduct("OYL:1", false, false) { almonds } is QrResolution.Found)
    }
    @Test fun noCodeDoesNotQueryStorage() = runBlocking {
        assertEquals(QrResolution.NoCode, resolveQrProduct(null, false, false) { error("No debería consultar") })
    }
    @Test fun storageFailureIsNotReportedAsMissing() = runBlocking {
        try {
            resolveQrProduct("oyl://product/1", true, false) { throw IllegalStateException("DB no disponible") }
            fail("Debe propagar el error para que QrViewModel muestre Failure")
        } catch (e: IllegalStateException) { assertEquals("DB no disponible", e.message) }
    }
}
