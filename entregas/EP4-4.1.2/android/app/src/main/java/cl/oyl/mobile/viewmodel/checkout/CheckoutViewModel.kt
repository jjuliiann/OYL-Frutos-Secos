package cl.oyl.mobile.viewmodel.checkout
import android.app.Application
import cl.oyl.mobile.model.checkout.CheckoutData
import cl.oyl.mobile.model.checkout.FulfillmentDetails
import cl.oyl.mobile.model.checkout.OrderRequest
import cl.oyl.mobile.model.checkout.OrderItem
import cl.oyl.mobile.viewmodel.BaseOylViewModel
import cl.oyl.mobile.util.CheckoutValidation

class CheckoutViewModel(app: Application) : BaseOylViewModel(app) {
    // Envía la compra al backend y conserva el carrito en Room hasta confirmación.
    fun confirmOrder(input: CheckoutData, done: (Long) -> Unit) {
        val user = graph.session.value?.takeIf { it.role == "CLIENT" } ?: return
        operation {
            val data = input.normalized()
            val locationIds = repository.store.locations().map { it.id }.toSet()
            val errors = CheckoutValidation.validate(data, locationIds) // Valida entrega y retiro.
            require(errors.isEmpty()) { errors.values.first() }
            val cart = repository.store.cartNow(user.id)
            check(cart.isNotEmpty()) { "El carrito está vacío" }
            val pickup = if (data.delivery == "RETIRO") repository.store.locations().firstOrNull { it.id == data.pickupLocationId } else null
            val fulfillment = FulfillmentDetails(
                data.delivery, data.recipientName, data.phone, data.email, data.pickupLocationId,
                pickup?.name.orEmpty(), pickup?.address.orEmpty(), pickup?.hours.orEmpty(),
                data.street, data.number, data.apartment, data.commune,
                data.region, data.postalCode, data.instructions
            )
            val request = OrderRequest(
                userId = user.id,
                delivery = data.delivery,
                contact = "${data.recipientName} · ${data.phone}",
                items = cart.map { OrderItem(it.productId, it.quantity) },
                fulfillment = fulfillment
            )
            val created = graph.api.createOrder(request) // Pedido confirmado por el servidor.
            // El pedido ya existe; una consulta posterior no debe hacer repetir la compra.
            repository.completeRemoteCheckout(user.id, created.id)
            done(created.id)
        }
    }
}
