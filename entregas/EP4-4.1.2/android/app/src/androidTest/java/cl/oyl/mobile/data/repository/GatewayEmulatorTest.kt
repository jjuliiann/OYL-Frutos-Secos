package cl.oyl.mobile.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cl.oyl.mobile.data.local.LocalDb
import cl.oyl.mobile.data.mapper.asSession
import cl.oyl.mobile.data.remote.RemoteApis
import cl.oyl.mobile.model.auth.Credentials
import cl.oyl.mobile.model.auth.RegisterData
import cl.oyl.mobile.model.checkout.*
import cl.oyl.mobile.model.pedidos.StatusChange
import cl.oyl.mobile.model.productos.Product
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import retrofit2.HttpException

/** Ejecutar únicamente contra una base de evaluación: esta prueba registra una compra real. */
class GatewayEmulatorTest {
    @Test fun cuentaNuevaCompraYAdminConsultaPedido() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("oylLive") == "true")
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = ctx.getSharedPreferences("oyl_auth", Context.MODE_PRIVATE)
        val previous = prefs.getString("jwt", null)
        val db = Room.inMemoryDatabaseBuilder(ctx, LocalDb::class.java).build()
        try {
            val api = RemoteApis.oyl(ctx)
            val repo = LocalRepository(db)
            assertEquals("users", api.health()["service"])
            val a = api.login(Credentials("admin","Admin1234!"))
            fun token(value: String?) { check(!value.isNullOrBlank()); prefs.edit().putString("jwt",value).commit() }
            token(a.token)
            val suffix = System.currentTimeMillis().toString().takeLast(10)
            val product = api.createProduct(a.id, Product(name="Nueces $suffix", price=1000, stock=3))
            val id = requireNotNull(product.id)
            val edited = api.updateProduct(id,a.id,product.copy(price=1250))
            assertEquals(1250,edited.price)
            val u = api.register(RegisterData("c$suffix","Cliente1234!","Cliente Android","c$suffix@oyl.cl","912345678","Santiago"))
            token(u.token)
            db.dao().saveSession(u.asSession())
            assertNull(repo.store.account(u.id)) // La cuenta nueva solo existe en Spring.
            repo.replaceCatalog(api.products())
            repo.addForSession(u.id, id, 1, adding=true)
            assertEquals(1, repo.store.cartNow(u.id).single().quantity)
            try { api.deactivateProduct(id, u.id); fail("Un cliente no puede administrar") }
            catch (e: HttpException) { assertEquals(403,e.code()) }
            val place = api.locations().first()
            val details = FulfillmentDetails("RETIRO","Cliente Android","912345678","",place.id,place.name,place.address,place.hours,"","","","","","","")
            val order = api.createOrder(OrderRequest(u.id,"RETIRO","Cliente Android",listOf(OrderItem(id,1)),details))
            repo.completeRemoteCheckout(u.id,order.id)
            assertTrue(repo.store.cartNow(u.id).isEmpty())
            assertEquals(1250,order.total)
            assertEquals(id,order.lines.single().productId)
            assertEquals(place.id,order.fulfillment?.pickupLocationId)
            assertTrue(api.orders(u.id).any { it.id==order.id })
            assertEquals(2,api.products().single { it.id==id }.stock)
            token(a.token)
            assertEquals("CONFIRMADO",api.changeOrderStatus(order.id,a.id,StatusChange("CONFIRMADO")).status)
            assertFalse(api.deactivateProduct(id,a.id).active)
            token(u.token)
            assertEquals("CONFIRMADO",api.orders(u.id).single { it.id==order.id }.status)
        } finally {
            db.close(); prefs.edit().putString("jwt",previous).commit()
        }
    }
}
