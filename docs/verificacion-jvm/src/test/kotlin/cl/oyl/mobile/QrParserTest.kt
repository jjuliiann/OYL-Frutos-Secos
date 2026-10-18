package cl.oyl.mobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrParserTest {
    @Test fun numericCodes() {
        assertEquals(4L, qrProductId("4"))
        assertEquals(12L, qrProductId("OYL:12"))
        assertEquals(20L, qrProductId("https://oyl.demo/product/20"))
    }
    @Test fun invalidCodes() {
        assertNull(qrProductId("OYL"))
        assertNull(qrProductId(""))
    }
}
