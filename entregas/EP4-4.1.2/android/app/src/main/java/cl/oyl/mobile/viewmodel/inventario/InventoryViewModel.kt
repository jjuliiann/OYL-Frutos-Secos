package cl.oyl.mobile.viewmodel.inventario
import android.app.Application
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import cl.oyl.mobile.data.remote.StockChange

class InventoryViewModel(app: Application) : BaseOylViewModel(app) {
    // Solo ADMIN puede cambiar existencias en el backend.
    fun changeStock(productId: Long, delta: Int) {
        val user = graph.session.value?.takeIf { it.role == "ADMIN" } ?: return
        operation {
            val updated = graph.api.changeStock(productId, user.id, StockChange(delta)) // Guarda el cambio remoto.
            repository.cacheRemoteProducts(listOf(updated))
            graph.showNotice("Inventario actualizado")
        }
    }
}
