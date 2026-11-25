package cl.oyl.mobile.viewmodel.perfil
import android.app.Application
import androidx.lifecycle.viewModelScope
import cl.oyl.mobile.data.local.entity.LocalAvatar
import cl.oyl.mobile.data.mapper.asSession
import cl.oyl.mobile.model.perfil.ProfileUpdate
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(app: Application) : BaseOylViewModel(app) {
    // Recupera el avatar de Room para la sesión actual.
    val avatar = graph.session.flatMapLatest { user ->
        if (user == null) flowOf(null) else local.avatar(user.id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    fun updateProfile(data: ProfileUpdate) {
        val user = graph.session.value ?: return
        operation {
            local.saveSession(graph.api.updateProfile(user.id, data).asSession()) // Guarda respuesta del servidor en Room.
            graph.showNotice("Perfil actualizado")
        }
    }
    fun saveAvatar(dataUri: String) {
        val user = graph.session.value ?: return
        operation { local.saveAvatar(LocalAvatar(user.id, dataUri)) } // Persiste foto en SQLite local.
    }
}
