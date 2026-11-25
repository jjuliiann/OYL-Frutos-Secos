package cl.oyl.mobile.viewmodel.mapa
import android.app.Application
import androidx.lifecycle.viewModelScope
import cl.oyl.mobile.model.mapa.PickupLocation
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MapViewModel(app: Application) : BaseOylViewModel(app) {
    private val _locations = MutableStateFlow<List<PickupLocation>>(emptyList())
    val locations = _locations.asStateFlow()
    init { loadLocations() } // Carga puntos al crear el ViewModel.
    fun loadLocations() { viewModelScope.launch {
        try {
            graph.awaitReady()
            _locations.value = graph.api.locations().also { repository.replaceLocations(it) } // Consulta puntos del backend.
        }
        catch (e: Exception) {
            if (e is CancellationException) throw e
            _locations.value = repository.store.locations().map { it.location() } // Usa copia Room sin conexión.
            graph.showNotice("Sin conexión: se muestran los puntos guardados en el teléfono")
        }
    } }
}
