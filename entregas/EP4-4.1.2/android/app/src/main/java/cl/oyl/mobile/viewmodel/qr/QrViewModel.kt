package cl.oyl.mobile.viewmodel.qr
import android.app.Application
import cl.oyl.mobile.domain.qr.resolveQrProduct
import cl.oyl.mobile.model.qr.QrResolution
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*

class QrViewModel(app: Application) : BaseOylViewModel(app) {
    private val _result = MutableStateFlow<QrResolution>(QrResolution.Idle)
    val result = _result.asStateFlow()
    fun reset() { _result.value = QrResolution.Idle }
    suspend fun resolve(payload: String?, external: Boolean = false): QrResolution {
        // Busca primero el catálogo remoto y conserva caché local para el lector QR.
        val resolved = try {
            graph.awaitReady()
            // Consulta inicial real: no supone que null inicial de StateFlow sea una sesión cerrada.
            val canManage = local.session().first()?.role == "ADMIN"
            val remote = try {
                graph.api.products().also { repository.replaceCatalog(it) }
            } catch (e: CancellationException) { throw e }
              catch (_: java.io.IOException) { null }
              catch (_: retrofit2.HttpException) { null }
            resolveQrProduct(payload, external, canManage) { id ->
                if (remote != null) remote.firstOrNull { it.id == id }
                else repository.store.product(id) ?: error("Catálogo no disponible")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            QrResolution.Failure
        }
        _result.value = resolved
        return resolved
    }
}
