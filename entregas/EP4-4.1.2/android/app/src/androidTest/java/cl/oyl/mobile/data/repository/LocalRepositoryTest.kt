package cl.oyl.mobile.data.repository

import cl.oyl.mobile.model.checkout.CheckoutData
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.oyl.mobile.data.local.LocalDb
import cl.oyl.mobile.model.auth.Credentials
import cl.oyl.mobile.model.perfil.ProfileUpdate
import cl.oyl.mobile.model.auth.RegisterData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalRepositoryTest {
    private lateinit var db: LocalDb
    private lateinit var repo: LocalRepository
    @Before fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),LocalDb::class.java).build()
        repo = LocalRepository(db); repo.initialize()
    }
    @After fun close() { db.close() }
    @Test fun checkoutPersistsLinesAndSubtractsStock() = runBlocking {
        val client = repo.login(Credentials("cliente","Cliente1234!"))
        repo.addCart(client.id,2,1,true)
        val id = repo.checkout(client.id,CheckoutData(recipientName = "Cliente OYL", phone = "+56912345678", pickupLocationId = 1))
        assertEquals(17,repo.store.product(2)!!.stock)
        assertTrue(repo.store.cartNow(client.id).isEmpty())
        val order = repo.orderView(repo.store.order(id)!!)
        assertEquals("Nueces",order.lines.single().productName)
        assertEquals(3490,order.total)
        assertEquals(1,db.dao().notices(client.id).first().size)
    }
    @Test fun rejectedCheckoutChangesNothing() = runBlocking {
        val client = repo.login(Credentials("cliente","Cliente1234!"))
        val admin = repo.login(Credentials("admin","Admin1234!"))
        repo.addCart(client.id,1,1,true); repo.addCart(client.id,2,1,true)
        repo.stock(admin.id,2,-18)
        try { repo.checkout(client.id,CheckoutData(recipientName = "Cliente OYL", phone = "+56912345678", pickupLocationId = 1)); fail("Debe rechazar stock insuficiente") } catch (_: IllegalStateException) { }
        assertEquals(25,repo.store.product(1)!!.stock)
        assertEquals(2,repo.store.cartNow(client.id).size)
        assertTrue(repo.store.observeOrders().first().isEmpty())
    }
    @Test fun rejectsDuplicateAliasAndWrongPassword() = runBlocking {
        try { repo.register(RegisterData("ADMIN","Nueva1234!","Prueba","prueba@oyl.demo","123456789","Santiago")); fail("Alias duplicado") } catch (_: IllegalStateException) { }
        try { repo.login(Credentials("admin","Incorrecta")); fail("Contraseña inválida") } catch (_: IllegalStateException) { }
    }
    @Test fun clientsCannotManageAndInactiveProductCannotBeBought() = runBlocking {
        val client = repo.login(Credentials("cliente","Cliente1234!"))
        val admin = repo.login(Credentials("admin","Admin1234!"))
        try { repo.stock(client.id,1,5); fail("Debe verificar rol") } catch (_: IllegalStateException) { }
        repo.addCart(client.id,1,1,true)
        repo.saveProduct(admin.id,repo.store.product(1)!!.copy(active = false))
        try { repo.checkout(client.id,CheckoutData(recipientName = "Cliente OYL", phone = "+56912345678", pickupLocationId = 1)); fail("Desactivado") } catch (_: IllegalStateException) { }
        repo.addCart(client.id,1,-1)
        assertTrue(repo.store.cartNow(client.id).isEmpty())
    }
    @Test fun repeatedInitializationPreservesStockAndAccounts() = runBlocking {
        val admin = repo.login(Credentials("admin","Admin1234!"))
        repo.stock(admin.id,1,-1); repo.initialize()
        assertEquals(24,repo.store.product(1)!!.stock)
        assertEquals(2,repo.store.accountCount())
        assertEquals(3,repo.store.locations().size)
    }
    @Test fun savedProfileAndStatusAreLocal() = runBlocking {
        val client = repo.login(Credentials("cliente","Cliente1234!"))
        val admin = repo.login(Credentials("admin","Admin1234!"))
        repo.updateProfile(client.id,ProfileUpdate("Nombre actualizado","cliente@oyl.demo","123456789","Santiago"))
        assertEquals("Nombre actualizado",repo.login(Credentials("cliente","Cliente1234!")).name)
        repo.addCart(client.id,1,1,true)
        val id=repo.checkout(client.id,CheckoutData(delivery = "ENTREGA", recipientName = "Cliente OYL", phone = "+56912345678", street = "Avenida Central", number = "123", commune = "Santiago", region = "Metropolitana"))
        repo.status(admin.id,id,"CONFIRMADO")
        assertEquals("CONFIRMADO",repo.store.order(id)!!.status)
        assertEquals(2,db.dao().notices(client.id).first().size)
    }
    @Test fun sqliteSurvivesDatabaseCloseAndReopen() = runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val name="oyl_persistence_test.db"
        context.deleteDatabase(name)
        var disk=Room.databaseBuilder(context,LocalDb::class.java,name).build()
        try {
            var r=LocalRepository(disk);r.initialize()
            val admin=r.login(Credentials("admin","Admin1234!"));r.stock(admin.id,2,-1)
            disk.close();disk=Room.databaseBuilder(context,LocalDb::class.java,name).build()
            r=LocalRepository(disk);r.initialize()
            assertEquals(17,r.store.product(2)!!.stock)
        } finally { disk.close();context.deleteDatabase(name) }
    }
}
