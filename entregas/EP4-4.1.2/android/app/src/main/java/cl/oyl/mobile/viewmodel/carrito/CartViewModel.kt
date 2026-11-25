package cl.oyl.mobile.viewmodel.carrito
import android.app.Application
import androidx.lifecycle.viewModelScope
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModel(app: Application) : BaseOylViewModel(app) {
    val cart = graph.session.flatMapLatest { user ->
        if (user?.role == "CLIENT") local.cart(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun addProduct(product: Product, quantity: Int = 1) {
        val user = graph.session.value
        val id = product.id
        if (user?.role != "CLIENT" || id == null) {
            graph.showNotice("El carrito está disponible para cuentas de cliente"); return
        }
        operation {
            repository.addForSession(user.id, id, quantity, adding = true)
            graph.showNotice("${product.name} agregado al carrito")
        }
    }
    fun adjustCart(productId: Long, delta: Int) {
        val user = graph.session.value?.takeIf { it.role == "CLIENT" } ?: return
        operation { repository.addForSession(user.id, productId, delta) }
    }
}
