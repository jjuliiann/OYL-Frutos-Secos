package cl.oyl.mobile.data.remote

import android.content.Context
import cl.oyl.mobile.BuildConfig
import java.net.URI
import java.security.MessageDigest

object GatewayAddress {
    const val emulator = "http://10.0.2.2:8080/api/"
    fun normalize(input: String): String {
        val uri = runCatching { URI(input.trim()) }.getOrNull()
        require(uri != null && uri.scheme in setOf("http", "https") && uri.host != null &&
            uri.port in -1..65535 && uri.port != 0 && uri.rawUserInfo == null &&
            uri.rawQuery == null && uri.rawFragment == null && uri.path.trimEnd('/') == "/api") {
            "Escribe la dirección completa, por ejemplo http://192.168.1.20:8080/api/"
        }
        return "${uri.scheme}://${uri.rawAuthority.lowercase()}/api/"
    }
    private fun settings(ctx: Context) = ctx.getSharedPreferences("oyl_gateway", Context.MODE_PRIVATE)
    fun current(ctx: Context): String = settings(ctx).getString("url", null) ?: BuildConfig.OYL_API_BASE_URL
    fun save(ctx: Context, url: String) = settings(ctx).edit().putString("url", normalize(url)).commit()
    // Cada servidor mantiene sus propios datos y credenciales en el dispositivo.
    fun suffix(url: String): String = if (url == BuildConfig.OYL_API_BASE_URL) "" else "_" +
        MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).take(12).joinToString("") { "%02x".format(it) }
    fun auth(ctx: Context, url: String) = ctx.getSharedPreferences("oyl_auth" + suffix(url), Context.MODE_PRIVATE)
}
