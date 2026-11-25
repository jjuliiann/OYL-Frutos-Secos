package cl.oyl.mobile.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** No se guardan contraseñas en texto plano. Algoritmo disponible desde minSdk 26. */
object PasswordHash {
    fun salt(): String = Base64.getEncoder().encodeToString(ByteArray(16).also { SecureRandom().nextBytes(it) })
    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 120_000, 256)
        return try { Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded) }
        finally { spec.clearPassword() }
    }
    fun matches(password: String, salt: String, expected: String): Boolean =
        MessageDigest.isEqual(hash(password, salt).toByteArray(), expected.toByteArray())
}
