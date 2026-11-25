package cl.oyl.mobile.data

import android.app.Application
import cl.oyl.mobile.data.local.LocalDb
import cl.oyl.mobile.data.repository.LocalRepository
import cl.oyl.mobile.data.remote.OylApi
import cl.oyl.mobile.data.remote.GatewayAddress
import cl.oyl.mobile.data.remote.RemoteApis
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Comparte Room, repositorio y Retrofit durante la vida de la aplicación. */
class OylGraph private constructor(app: Application, private val url: String) {
    // Room conserva los datos locales del teléfono.
    val database = LocalDb.get(app, "oyl_sqlite" + GatewayAddress.suffix(url) + ".db")
    val local = database.dao()
    val repository = LocalRepository(database)
    val api: OylApi = RemoteApis.oyl(app, url) // Retrofit usa el Gateway de Spring Boot.
    private val tokenPreferences = GatewayAddress.auth(app, url)
    fun persistAuthToken(token: String) { tokenPreferences.edit().putString("jwt", token).apply() } // Guarda el token de sesión.
    fun clearAuthToken() { tokenPreferences.edit().remove("jwt").apply() }
    fun hasAuthToken(): Boolean = !tokenPreferences.getString("jwt", null).isNullOrBlank()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initialized = applicationScope.async {
        val current = local.currentSession() // Abre Room y aplica las migraciones.
        if (current != null && tokenPreferences.getString("jwt", "")?.count { it == '.' } != 2) {
            clearAuthToken(); local.logout() // Las sesiones de la versión anterior vuelven a autenticarse.
        }
    }
    private val _notice = MutableStateFlow("")
    val notice = _notice.asStateFlow()
    private val _busy = MutableStateFlow(true)
    val busy = _busy.asStateFlow()
    val session = flow { awaitReady(); emitAll(local.session()) }.stateIn(applicationScope, SharingStarted.Eagerly, null)

    init {
        applicationScope.launch {
            try { awaitReady() }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                showNotice("No se pudo preparar SQLite: ${e.message}")
            } finally { _busy.value = false }
        }
    }
    suspend fun awaitReady() { initialized.await() }
    fun showNotice(message: String) { _notice.value = message }
    fun dismissNotice() { _notice.value = "" }

    /** Conserva la exclusión de operaciones que ya usaba OYL. Las llamadas salen del hilo UI. */
    fun operation(scope: CoroutineScope, action: suspend () -> Unit) { // Ejecuta tareas fuera del hilo visual.
        if (_busy.value) return
        _busy.value = true
        scope.launch {
            try { awaitReady(); action() }
            catch (e: Exception) {
                if (e is CancellationException) throw e
                showNotice(networkMessage(e))
            } finally { _busy.value = false }
        }
    }
    fun networkMessage(error: Exception): String = when (error) {
        is java.io.IOException -> "No se pudo contactar al servidor de OYL. Comprueba la conexión y vuelve a intentar."
        is retrofit2.HttpException -> when (error.code()) {
            401 -> "Credenciales incorrectas o sesión vencida. Vuelve a iniciar sesión."
            403 -> "Tu cuenta no tiene permiso para esta operación."
            409 -> "Los datos cambiaron o ya existen. Actualiza y vuelve a intentar."
            502, 503, 504 -> "El servicio no está disponible en este momento. Vuelve a intentar."
            else -> "El servidor rechazó la solicitud (HTTP ${error.code()})."
        }
        else -> error.message ?: "No se pudo completar la operación."
    }
    companion object {
        @Volatile private var instance: OylGraph? = null
        @Synchronized fun get(app: Application): OylGraph {
            val selected = GatewayAddress.current(app)
            instance?.let { if (it.url == selected) return it else it.applicationScope.cancel() }
            return OylGraph(app, selected).also { instance = it }
        }
    }
}
