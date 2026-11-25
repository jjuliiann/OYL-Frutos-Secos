package cl.oyl.mobile.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.oyl.mobile.data.local.LocalDb
import cl.oyl.mobile.model.checkout.CheckoutData
import cl.oyl.mobile.model.auth.Credentials
import cl.oyl.mobile.model.perfil.ProfileUpdate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutRepositoryTest {
    private lateinit var db: LocalDb
    private lateinit var repo: LocalRepository
    @Before fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),LocalDb::class.java).build()
        repo = LocalRepository(db);repo.initialize()
    }
    @After fun close() { db.close() }
    @Test fun pickupStoresChosenLocationSnapshot() = runBlocking {
        val client=repo.login(Credentials("cliente","Cliente1234!"))
        repo.addCart(client.id,1,1,true)
        val id=repo.checkout(client.id,CheckoutData(recipientName="Persona Retiro",phone="912345678",pickupLocationId=2))
        val details=repo.orderView(repo.store.order(id)!!).fulfillment!!
        assertEquals(2L,details.pickupLocationId)
        assertEquals("Punto de retiro Providencia",details.pickupName)
        assertEquals("Providencia, Santiago, Chile",details.pickupAddress)
        assertEquals("",details.street)
        assertEquals(24,repo.store.product(1)!!.stock)
    }
    @Test fun shippingAndRecipientPersistIndependentlyFromProfile() = runBlocking {
        val client=repo.login(Credentials("cliente","Cliente1234!"))
        repo.addCart(client.id,2,1,true)
        val id=repo.checkout(client.id,CheckoutData(delivery="ENTREGA",recipientName="Ana Martinez",phone="912345678",email="ana@example.com",
            street="Avenida Central",number="123",apartment="Depto 4",commune="Santiago",region="Metropolitana",postalCode="1234567",instructions="Portón azul"))
        repo.updateProfile(client.id,ProfileUpdate("Otro Nombre","otro@example.com","987654321","Otra dirección"))
        val details=repo.orderView(repo.store.order(id)!!).fulfillment!!
        assertEquals("Ana Martinez",details.recipientName)
        assertEquals("Avenida Central",details.street)
        assertEquals("123",details.number)
        assertEquals("Depto 4",details.apartment)
        assertEquals("Metropolitana",details.region)
        assertEquals("Portón azul",details.instructions)
        assertNull(details.pickupLocationId)
    }
    @Test fun invalidShippingDoesNotCreateOrderOrChangeInventory() = runBlocking {
        val client=repo.login(Credentials("cliente","Cliente1234!"))
        repo.addCart(client.id,1,1,true)
        try { repo.checkout(client.id,CheckoutData(delivery="ENTREGA",recipientName="Ana Martinez",phone="912345678"));fail("Dirección incompleta") }
        catch (_: IllegalArgumentException) { }
        assertEquals(25,repo.store.product(1)!!.stock)
        assertEquals(1,repo.store.cartNow(client.id).size)
        assertTrue(repo.store.observeOrders().first().isEmpty())
    }
}
