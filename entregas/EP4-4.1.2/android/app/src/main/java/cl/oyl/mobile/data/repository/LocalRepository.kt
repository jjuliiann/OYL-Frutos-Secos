package cl.oyl.mobile.data.repository

import cl.oyl.mobile.model.checkout.CheckoutData
import cl.oyl.mobile.data.local.entity.OrderFulfillment
import cl.oyl.mobile.util.CheckoutValidation
import androidx.room.withTransaction
import cl.oyl.mobile.data.local.LocalDb
import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.data.local.entity.LocalNotice
import cl.oyl.mobile.data.local.entity.StoredAccount
import cl.oyl.mobile.data.local.entity.StoredLine
import cl.oyl.mobile.data.local.entity.StoredLocation
import cl.oyl.mobile.data.local.entity.StoredOrder
import cl.oyl.mobile.model.auth.Account
import cl.oyl.mobile.model.auth.Credentials
import cl.oyl.mobile.model.pedidos.OrderLine
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.model.perfil.ProfileUpdate
import cl.oyl.mobile.model.pedidos.PurchaseOrder
import cl.oyl.mobile.model.auth.RegisterData
import cl.oyl.mobile.util.PasswordHash
import cl.oyl.mobile.util.Validation
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Acceso Room local, reglas reutilizables y caché para la app conectada al backend. */
class LocalRepository(private val db: LocalDb) {
    val store = db.store()
    private val local = db.dao()
    /** Sincroniza el catálogo remoto con la tabla local para lectura rápida y soporte del carrito. */
    suspend fun cacheRemoteProducts(products: List<Product>) = db.withTransaction {
        products.forEach { product -> store.insertProduct(product) }
    }

    // Una respuesta completa reemplaza el catálogo; una edición solo actualiza una fila.
    suspend fun replaceCatalog(products: List<Product>) = db.withTransaction {
        store.clearProducts()
        products.forEach { store.insertProduct(it) }
    }

    suspend fun replaceLocations(locations: List<cl.oyl.mobile.model.mapa.PickupLocation>) = db.withTransaction {
        store.clearLocations()
        store.insertLocations(locations.map { StoredLocation(it.id, it.name, it.address, it.hours, it.latitude, it.longitude) })
    }

    /** Conserva el carrito local, pero refleja compra e inventario confirmados por el backend. */
    suspend fun completeRemoteCheckout(userId: Long, orderId: Long) = db.withTransaction {
        local.clearCart(userId)
        local.saveNotice(LocalNotice(userId = userId, title = "Pedido #$orderId", content = "Pedido registrado en el servidor"))
    }

    suspend fun login(c: Credentials): Account = withContext(Dispatchers.IO) {
        val u = store.account(c.alias.trim()) ?: error("Alias o contraseña incorrectos")
        check(PasswordHash.matches(c.password, u.salt, u.passwordHash)) { "Alias o contraseña incorrectos" }; u.account()
    }
    suspend fun register(r: RegisterData): Account = withContext(Dispatchers.IO) {
        require(Validation.register(r).isEmpty()) { "Revisa los campos del registro" }
        val salt = PasswordHash.salt()
        val hash = PasswordHash.hash(r.password, salt)
        db.withTransaction {
            check(store.account(r.alias.trim()) == null) { "El alias ya está registrado" }
            val user = StoredAccount(alias = r.alias.trim(), name = r.name.trim(), email = r.email.trim(), phone = r.phone.trim(), address = r.address.trim(), salt = salt, passwordHash = hash)
            user.copy(id = store.insertAccount(user)).account()
        }
    }
    suspend fun updateProfile(id: Long, p: ProfileUpdate): Account = db.withTransaction {
        require(Validation.profile(p.name,p.email,p.phone,p.address).isEmpty()) { "Revisa los campos del perfil" }
        val u = store.account(id) ?: error("Usuario inexistente")
        val updated = u.copy(name = p.name.trim(), email = p.email.trim(), phone = p.phone.trim(), address = p.address.trim())
        store.updateAccount(updated); updated.account()
    }
    private suspend fun admin(id: Long) { check(store.account(id)?.role == "ADMIN") { "Solo administrador" } }
    suspend fun saveProduct(adminId: Long, product: Product) = db.withTransaction {
        admin(adminId)
        require(Validation.product(product.name, product.category, product.description, product.presentation, product.price.toString(), product.stock.toString(), product.minStock.toString()).isEmpty()) { "Producto inválido" }
        if (product.id == null) store.insertProduct(product)
        else { check(store.product(product.id) != null) { "Producto inexistente" }; store.updateProduct(product) }
    }
    suspend fun stock(adminId: Long, id: Long, delta: Int) = db.withTransaction {
        admin(adminId)
        val p = store.product(id) ?: error("Producto inexistente")
        val stock = p.stock.toLong() + delta
        require(stock in 0..Int.MAX_VALUE.toLong()) { "Stock fuera de rango" }
        store.updateProduct(p.copy(stock = stock.toInt()))
    }
    suspend fun addCart(userId: Long, productId: Long, delta: Int, adding: Boolean = false) = db.withTransaction {
        check(store.account(userId)?.role == "CLIENT") { "Inicia sesión como cliente para comprar" }
        adjustLine(userId, productId, delta, adding)
    }

    // La cuenta autenticada puede haberse creado en otro teléfono.
    suspend fun addForSession(userId: Long, productId: Long, delta: Int, adding: Boolean = false) = db.withTransaction {
        val active = local.currentSession()
        check(active?.id == userId && active.role == "CLIENT") { "Inicia sesión como cliente para comprar" }
        adjustLine(userId, productId, delta, adding)
    }

    private suspend fun adjustLine(userId: Long, productId: Long, delta: Int, adding: Boolean) {
        val product = store.product(productId)
        if (product == null && delta < 0) { local.removeCart(userId, productId); return }
        checkNotNull(product) { "Producto inexistente" }
        if (delta > 0) check(product.active) { "El producto está desactivado" }
        val current = store.cartLine(userId, productId)?.quantity ?: 0
        val next = current.toLong() + delta
        if (adding) require(delta > 0) { "Cantidad inválida" }
        if (delta > 0) check(next <= product.stock) { "Stock insuficiente" }
        if (next <= 0) local.removeCart(userId, productId) else local.saveCart(CartLine(userId, productId, next.toInt()))
    }
    suspend fun checkout(userId: Long, input: CheckoutData): Long = db.withTransaction {
        check(store.account(userId)?.role == "CLIENT") { "Solo clientes pueden comprar" }
        val data = input.normalized()
        val locations = store.locations()
        val errors = CheckoutValidation.validate(data, locations.map { it.id }.toSet())
        require(errors.isEmpty()) { errors.values.first() }
        val pickup = if (data.delivery == "RETIRO") locations.first { it.id == data.pickupLocationId } else null
        val cart = store.cartNow(userId)
        check(cart.isNotEmpty()) { "El carrito está vacío" }
        val lines = cart.map { item ->
            val p = store.product(item.productId) ?: error("Producto inexistente")
            check(p.active && item.quantity > 0 && p.stock >= item.quantity) { "Stock insuficiente o producto desactivado: ${p.name}" }
            p to item.quantity
        }
        val total = lines.sumOf { (p,q) -> p.price.toLong() * q }
        require(total in 0..Int.MAX_VALUE.toLong()) { "Total fuera de rango" }
        val id = store.insertOrder(StoredOrder(userId = userId, createdAt = LocalDateTime.now().toString(), status = "PENDIENTE_VALIDACION", delivery = data.delivery, contact = "${data.recipientName} · ${data.phone}", total = total.toInt()))
        store.insertFulfillment(OrderFulfillment(orderId = id, delivery = data.delivery,
            recipientName = data.recipientName, phone = data.phone, email = data.email,
            pickupLocationId = pickup?.id, pickupName = pickup?.name.orEmpty(),
            pickupAddress = pickup?.address.orEmpty(), pickupHours = pickup?.hours.orEmpty(),
            street = if (pickup == null) data.street else "", number = if (pickup == null) data.number else "",
            apartment = if (pickup == null) data.apartment else "", commune = if (pickup == null) data.commune else "",
            region = if (pickup == null) data.region else "", postalCode = if (pickup == null) data.postalCode else "",
            instructions = data.instructions))
        for ((p,q) in lines) {
            store.updateProduct(p.copy(stock = p.stock - q))
            store.insertLine(StoredLine(orderId = id, productId = p.id!!, productName = p.name, quantity = q, unitPrice = p.price))
        }
        local.clearCart(userId)
        local.saveNotice(LocalNotice(userId = userId, title = "Pedido #$id", content = "Pedido registrado: PENDIENTE_VALIDACION"))
        id
    }
    suspend fun status(adminId: Long, orderId: Long, state: String) = db.withTransaction {
        admin(adminId)
        require(state in listOf("PENDIENTE_VALIDACION","CONFIRMADO","PREPARADO","ENTREGADO")) { "Estado inválido" }
        val order = store.order(orderId) ?: error("Pedido inexistente")
        store.updateOrder(order.copy(status = state))
        local.saveNotice(LocalNotice(userId = order.userId, title = "Pedido actualizado", content = "Pedido #$orderId: $state"))
    }
    suspend fun orderView(order: StoredOrder): PurchaseOrder = PurchaseOrder(order.id, order.userId, order.createdAt, order.status, order.delivery, order.contact, order.total,
        store.lines(order.id).map { OrderLine(it.id,it.productId,it.productName,it.quantity,it.unitPrice) },
        fulfillment = store.fulfillment(order.id)?.details())
}
