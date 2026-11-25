package cl.oyl.mobile.viewmodel.productos
import android.app.Application
import androidx.lifecycle.viewModelScope
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModel(app: Application) : BaseOylViewModel(app) {
    val products = repository.store.observeProducts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _sync = MutableStateFlow("Consultando catálogo…")
    val sync = _sync.asStateFlow()
    private var load: kotlinx.coroutines.Job? = null
    init { refreshProducts() }

    fun refreshProducts() {
        load?.cancel()
        load = viewModelScope.launch {
            _sync.value = "Consultando catálogo…"
            try {
                graph.awaitReady()
                repository.replaceCatalog(graph.api.products())
                _sync.value = "Catálogo actualizado desde OYL"
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) {
                _sync.value = if (products.value.isEmpty()) "No se pudo cargar el catálogo."
                    else "Sin actualizar: se muestra el catálogo guardado."
            }
        }
    }
    fun deactivate(productId: Long) {
        val admin = graph.session.value?.takeIf { it.role == "ADMIN" } ?: return
        operation {
            repository.cacheRemoteProducts(listOf(graph.api.deactivateProduct(productId, admin.id)))
            graph.showNotice("Producto desactivado")
        }
    }

    fun saveProduct(product: Product, done: () -> Unit) {
        val user = graph.session.value?.takeIf { it.role == "ADMIN" } ?: return
        operation {
            // Admin crea o edita el producto directamente en el backend.
            val saved = if (product.id == null) graph.api.createProduct(user.id, product)
                        else graph.api.updateProduct(product.id, user.id, product)
            repository.cacheRemoteProducts(listOf(saved))
            done()
        }
    }
}
