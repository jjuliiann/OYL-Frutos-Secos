package cl.oyl.mobile.domain.qr

import org.junit.Assert.*
import org.junit.Test

class QrParserTest {
    @Test fun supportedFormatsRemainCompatible() {
        assertEquals(1L, qrProductId(" OYL:1 "))
        assertEquals(7L, qrProductId("7"))
        assertEquals(20L, qrProductId("https://oyl.demo/product/20"))
        assertEquals(2L, qrProductId("oyl://product/2"))
    }
    @Test fun rejectsInvalidAndOverflow() {
        listOf("", "OYL", "basura1", "0", "-1", "OYL:abc", "1.5", "https://otro.cl/1", "99999999999999999999999",
            "oyl://product/0", "oyl://product/-1", "oyl://product/1/extra", "oyl://other/1", "oyl://product/1?admin=true",
            "oyl://product/01", "oyl://product/1#fragment", "oyl://product/99999999999999999999999").forEach { assertNull(it, qrProductId(it)) }
    }
    @Test fun externalLinksAreRestrictedToRegisteredScheme() {
        assertEquals(1L, externalQrProductId("oyl://product/1"))
        assertEquals(999999L, externalQrProductId("oyl://product/999999"))
        listOf("OYL:1", "1", "https://oyl.demo/product/1", "https://otro.cl/1", "oyl://other/1", "oyl://product/abc").forEach {
            assertNull(it, externalQrProductId(it))
        }
    }
    @Test fun generatedLinkRoundTrips() {
        for (id in listOf(1L, 7L, 999999L, Long.MAX_VALUE)) assertEquals(id, externalQrProductId(productQrLink(id)))
    }
    @Test(expected = IllegalArgumentException::class) fun refusesZero() { productQrLink(0) }
    @Test(expected = IllegalArgumentException::class) fun refusesNegative() { productQrLink(-1) }
}
