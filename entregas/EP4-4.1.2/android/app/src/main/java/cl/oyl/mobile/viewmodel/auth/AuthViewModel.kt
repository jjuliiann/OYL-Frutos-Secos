package cl.oyl.mobile.viewmodel.auth
import android.app.Application
import cl.oyl.mobile.data.mapper.asSession
import cl.oyl.mobile.model.auth.Credentials
import cl.oyl.mobile.model.auth.RegisterData
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : BaseOylViewModel(app) {
    val session = graph.session
    // Login real: Retrofit valida credenciales y devuelve la sesión.
    fun login(alias: String, password: String, done: () -> Unit) = operation {
        val user = graph.api.login(Credentials(alias, password))
        // Guarda token para futuras llamadas protegidas.
        graph.persistAuthToken(user.token?.takeIf { it.isNotBlank() } ?: error("El servidor no entregó token de sesión"))
        local.saveSession(user.asSession()); done()
    }
    // Registro remoto y persistencia local de la sesión recibida.
    fun register(data: RegisterData, done: () -> Unit) = operation {
        val user = graph.api.register(data)
        graph.persistAuthToken(user.token?.takeIf { it.isNotBlank() } ?: error("El servidor no entregó token de sesión"))
        local.saveSession(user.asSession()); done()
    }
    // Cierra sesión en Room y elimina el token guardado.
    fun logout(done: () -> Unit) = operation { graph.clearAuthToken(); local.logout(); done() }
}
