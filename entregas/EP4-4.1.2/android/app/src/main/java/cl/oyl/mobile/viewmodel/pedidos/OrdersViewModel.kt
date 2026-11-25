package cl.oyl.mobile.viewmodel.pedidos
import androidx.room.withTransaction
import android.app.Application
import androidx.lifecycle.viewModelScope
import cl.oyl.mobile.data.local.entity.CachedOrder
import cl.oyl.mobile.model.pedidos.PurchaseOrder
import cl.oyl.mobile.model.pedidos.StatusChange
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrdersViewModel(app: Application) : BaseOylViewModel(app) {
    private val refresh = MutableStateFlow(0)
    private val _orders = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val orders = _orders.asStateFlow()
    private val _cachedOrders = MutableStateFlow<List<CachedOrder>>(emptyList())
    val cachedOrders = _cachedOrders.asStateFlow()
    init {
        viewModelScope.launch {
            combine(refresh, graph.session) { tick, user -> tick to user }
                .collectLatest { (_, user) ->
                    if (user == null) { _orders.value = emptyList(); _cachedOrders.value = emptyList() }
                    else try {
                        _orders.value = emptyList(); _cachedOrders.value = emptyList()
                        graph.awaitReady()
                        val remote = graph.api.orders(user.id) // Pide los pedidos al servidor.
                        graph.database.withTransaction {
                        graph.local.clearCachedOrders(user.id)
                        remote.forEach { order ->
                            graph.local.cacheOrder(CachedOrder(user.id, order.id, order.total, order.status, order.createdAt))
                        }
                        }
                        _orders.value = remote
                        _cachedOrders.value = graph.local.cachedOrders(user.id).first() // Prepara respaldo SQLite.
                    }
                    catch (e: Exception) {
                        if (e is CancellationException) throw e
                        _cachedOrders.value = graph.local.cachedOrders(user.id).first() // Muestra historial sin conexión.
                        graph.showNotice("No se pudieron cargar los pedidos del servidor")
                    }
                }
        }
    }
    fun reloadOrders() { refresh.value++ }
    fun setStatus(orderId: Long, newStatus: String) {
        val admin = graph.session.value?.takeIf { it.role == "ADMIN" } ?: return
        operation {
            graph.api.changeOrderStatus(orderId, admin.id, StatusChange(newStatus)) // Actualiza el estado remoto.
            reloadOrders()
            graph.showNotice("Estado del pedido actualizado")
        }
    }
}
