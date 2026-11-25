package cl.oyl.mobile.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.oyl.mobile.data.OylGraph

/** Infraestructura compartida; la lógica de cada área vive en su ViewModel. */
abstract class BaseOylViewModel(app: Application) : AndroidViewModel(app) {
    protected val graph = OylGraph.get(app)
    protected val repository = graph.repository
    protected val local = graph.local
    protected fun operation(action: suspend () -> Unit) = graph.operation(viewModelScope, action)
}
