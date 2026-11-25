package cl.oyl.mobile.util

import org.junit.Assert.*
import org.junit.Test
class PasswordHashTest {
    @Test fun saltedHashVerifiesOnlyCorrectPassword() {
        val salt=PasswordHash.salt()
        val hash=PasswordHash.hash("Admin1234!",salt)
        assertTrue(PasswordHash.matches("Admin1234!",salt,hash))
        assertFalse(PasswordHash.matches("OtraClave",salt,hash))
        assertNotEquals(hash,PasswordHash.hash("Admin1234!",PasswordHash.salt()))
    }
}
