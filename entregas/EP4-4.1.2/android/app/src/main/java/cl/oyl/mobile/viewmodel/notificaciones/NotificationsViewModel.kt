package cl.oyl.mobile.viewmodel.notificaciones
import android.app.Application
import androidx.lifecycle.viewModelScope
import cl.oyl.mobile.data.local.entity.LocalNotice
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModel(app: Application) : BaseOylViewModel(app) {
    val notifications = graph.session.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else local.notices(user.id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun demoNotice() {
        val user = graph.session.value ?: return
        operation {
            local.saveNotice(LocalNotice(userId = user.id, title = "Novedades OYL", content = "Conoce nuestras nueces y semillas"))
            graph.showNotice("Notificación guardada")
        }
    }
    fun markRead(id: Long) {
        val user = graph.session.value ?: return
        operation { local.readNotice(user.id, id) }
    }
}
