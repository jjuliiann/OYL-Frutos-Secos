package cl.oyl.mobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalCoroutinesApi::class)
class OylViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = LocalDb.get(app).dao()
    val session = dao.session().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val products = MutableStateFlow<List<Product>>(emptyList())
    val orders = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val locations = MutableStateFlow<List<PickupLocation>>(emptyList())
    val notice = MutableStateFlow("")
    val busy = MutableStateFlow(false)
    val cart = session.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.cart(account.id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val notifications = session.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.notices(account.id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val avatar = session.flatMapLatest { user ->
        if (user == null) flowOf(null) else dao.avatar(user.id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val cachedOrders = session.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.cachedOrders(account.id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refreshProducts()
        loadLocations()
    }

    fun dismissNotice() { notice.value = "" }
    private fun explain(error: Exception): String = when (error) {
        is HttpException -> "Error HTTP ${error.code()}: ${error.response()?.errorBody()?.string()?.take(160) ?: "revisa la solicitud"}"
        else -> "No se pudo conectar: ${error.message ?: "verifica Spring Boot en puerto 8080"}"
    }

    fun refreshProducts() {
        viewModelScope.launch {
            val cache = dao.productsCached().map { it.toProduct() }
            if (products.value.isEmpty()) products.value = cache
            try {
                val latest = Remote.api.products()
                products.value = latest
                latest.filter { it.id != null }.forEach { dao.cacheProduct(CachedProduct.fromProduct(it)) }
            } catch (e: Exception) {
                if (cache.isEmpty()) notice.value = explain(e)
            }
        }
    }

    fun loadLocations() {
        viewModelScope.launch {
            try { locations.value = Remote.api.locations() }
            catch (e: Exception) {
                notice.value = "No se pudieron actualizar los puntos de retiro. " + explain(e)
            }
        }
    }

    fun login(alias: String, password: String, done: () -> Unit) {
        if (Validation.login(alias, password).isNotEmpty()) { notice.value = "Ingresa alias y contraseña"; return }
        viewModelScope.launch {
            busy.value = true
            try {
                val user = Remote.api.login(Credentials(alias.trim(), password))
                dao.saveSession(user.asSession())
                refreshProducts()
                loadOrders(user.id)
                done()
            } catch (e: Exception) { notice.value = explain(e) }
            finally { busy.value = false }
        }
    }

    fun register(data: RegisterData, done: () -> Unit) {
        if (Validation.register(data).isNotEmpty()) {
            notice.value = "Nombre, alias y contraseña de 8 caracteres son obligatorios"
            return
        }
        viewModelScope.launch {
            busy.value = true
            try {
                val user = Remote.api.register(data)
                dao.saveSession(user.asSession())
                refreshProducts()
                done()
            } catch (e: Exception) { notice.value = explain(e) }
            finally { busy.value = false }
        }
    }

    fun updateProfile(data: ProfileUpdate) {
        val user = session.value ?: return
        if (Validation.profile(data.name, data.email, data.phone, data.address).isNotEmpty()) {
            notice.value = "Revisa los campos del perfil"; return
        }
        viewModelScope.launch {
            try {
                val updated = Remote.api.updateProfile(user.id, data)
                dao.saveSession(updated.asSession())
                notice.value = "Perfil actualizado"
            } catch (e: Exception) { notice.value = explain(e) }
        }
    }

    fun logout(done: () -> Unit) {
        viewModelScope.launch {
            dao.logout()
            orders.value = emptyList()
            done()
        }
    }

    fun addProduct(product: Product, quantity: Int = 1) {
        val user = session.value
        val id = product.id
        if (user == null || id == null || user.role != "CLIENT") {
            notice.value = "Inicia sesión como cliente para comprar"; return
        }
        val existing = cart.value.firstOrNull { it.productId == id }?.quantity ?: 0
        val updated = existing + quantity
        if (quantity <= 0 || updated > product.stock) { notice.value = "No hay existencias suficientes"; return }
        viewModelScope.launch {
            dao.saveCart(CartLine(user.id, id, updated))
            notice.value = "${product.name} agregado al carrito"
        }
    }

    fun adjustCart(productId: Long, delta: Int) {
        val user = session.value ?: return
        val current = cart.value.firstOrNull { it.productId == productId } ?: return
        val maxStock = products.value.firstOrNull { it.id == productId }?.stock ?: 0
        val updated = current.quantity + delta
        if (updated > maxStock) { notice.value = "Stock insuficiente"; return }
        viewModelScope.launch {
            if (updated <= 0) dao.removeCart(user.id, productId)
            else dao.saveCart(current.copy(quantity = updated))
        }
    }

    fun confirmOrder(delivery: String, contact: String, done: (Long) -> Unit) {
        val user = session.value ?: return
        if (cart.value.isEmpty()) { notice.value = "El carrito está vacío"; return }
        if (contact.isBlank()) { notice.value = "Indica un contacto de prueba"; return }
        viewModelScope.launch {
            busy.value = true
            try {
                val order = Remote.api.createOrder(
                    OrderRequest(user.id, delivery, contact, cart.value.map { OrderItem(it.productId, it.quantity) })
                )
                dao.clearCart(user.id)
                dao.cacheOrder(CachedOrder(user.id, order.id, order.total, order.status, order.createdAt))
                dao.saveNotice(LocalNotice(userId = user.id, title = "Pedido #${order.id}", content = "Pedido registrado: ${order.status}"))
                refreshProducts()
                loadOrders(user.id)
                done(order.id)
            } catch (e: Exception) { notice.value = explain(e) }
            finally { busy.value = false }
        }
    }

    fun reloadOrders() {
        val userId = session.value?.id ?: return
        loadOrders(userId)
    }

    fun loadOrders(id: Long) {
        viewModelScope.launch {
            try {
                val list = Remote.api.orders(id)
                orders.value = list
                val ownerId = session.value?.id ?: return@launch
                list.forEach { dao.cacheOrder(CachedOrder(ownerId, it.id, it.total, it.status, it.createdAt)) }
            } catch (e: Exception) { notice.value = explain(e) }
        }
    }

    fun saveProduct(product: Product, done: () -> Unit) {
        val user = session.value ?: return
        if (user.role != "ADMIN") return
        if (Validation.product(product.name, product.category, product.description, product.presentation, product.price.toString(), product.stock.toString(), product.minStock.toString()).isNotEmpty()) {
            notice.value = "Completa los datos válidos del producto"; return
        }
        viewModelScope.launch {
            busy.value = true
            try {
                if (product.id == null) Remote.api.createProduct(user.id, product)
                else Remote.api.updateProduct(product.id, user.id, product)
                refreshProducts()
                done()
            } catch (e: Exception) { notice.value = explain(e) }
            finally { busy.value = false }
        }
    }

    fun changeStock(productId: Long, delta: Int) {
        val user = session.value ?: return
        if (user.role != "ADMIN" || delta == 0) return
        viewModelScope.launch {
            try {
                Remote.api.changeStock(productId, user.id, StockChange(delta))
                refreshProducts()
                if (delta < 0) notice.value = "Salida registrada" else notice.value = "Entrada registrada"
            } catch (e: Exception) { notice.value = explain(e) }
        }
    }

    fun setStatus(orderId: Long, newStatus: String) {
        val user = session.value ?: return
        if (user.role != "ADMIN") return
        viewModelScope.launch {
            try {
                Remote.api.status(orderId, user.id, StatusChange(newStatus))
                loadOrders(user.id)
                dao.saveNotice(LocalNotice(userId = user.id, title = "Pedido actualizado", content = "Pedido #$orderId: $newStatus"))
            } catch (e: Exception) { notice.value = explain(e) }
        }
    }

    fun demoNotice() {
        val user = session.value ?: return
        viewModelScope.launch {
            dao.saveNotice(LocalNotice(userId = user.id, title = "Promoción de demostración", content = "Conoce nuestras nueces y semillas"))
            notice.value = "Notificación de prueba guardada"
        }
    }

    fun saveAvatar(dataUri: String) {
        val user = session.value ?: return
        viewModelScope.launch { dao.saveAvatar(LocalAvatar(user.id, dataUri)) }
    }

    fun markRead(id: Long) {
        val user = session.value ?: return
        viewModelScope.launch { dao.readNotice(user.id, id) }
    }

    private fun Account.asSession() = Session(
        id = id, alias = alias, name = name, role = role,
        email = email.orEmpty(), phone = phone.orEmpty(), address = address.orEmpty()
    )
}
