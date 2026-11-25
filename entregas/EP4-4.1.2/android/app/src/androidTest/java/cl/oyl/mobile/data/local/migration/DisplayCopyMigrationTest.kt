package cl.oyl.mobile.data.local.migration

import cl.oyl.mobile.data.local.migration.CheckoutMigration
import cl.oyl.mobile.model.checkout.CheckoutData
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.oyl.mobile.data.local.LocalDb
import cl.oyl.mobile.data.local.migration.PackageLayoutMigration
import cl.oyl.mobile.data.repository.LocalRepository
import cl.oyl.mobile.data.repository.initialize
import cl.oyl.mobile.model.auth.Credentials
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayCopyMigrationTest {
    @Test fun upgradePreservesExistingOrderAndStock() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "oyl_copy_migration_test.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context,LocalDb::class.java,name).build()
        try {
            val repo = LocalRepository(db)
            repo.initialize()
            val client = repo.login(Credentials("cliente","Cliente1234!"))
            repo.addCart(client.id,2,1,true)
            val orderId = repo.checkout(client.id,CheckoutData(recipientName = "Cliente OYL", phone = "+56912345678", pickupLocationId = 1))
            val sql = db.openHelper.writableDatabase
            sql.execSQL("UPDATE accounts SET name = 'Cliente de demostración' WHERE alias = 'cliente'")
            sql.execSQL("UPDATE pickup_locations SET name = 'Punto de prueba Santiago Centro' WHERE id = 1")
            db.close()
            // v1, v2 y v3 tienen las mismas tablas: solo cambian sus textos iniciales.
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READWRITE).use { it.version = 1 }
            db = Room.databaseBuilder(context,LocalDb::class.java,name).addMigrations(DisplayCopyMigration, PackageLayoutMigration, CheckoutMigration).build()
            assertEquals("Cliente",db.store().account(client.id)!!.name)
            assertEquals("Punto de retiro Santiago Centro",db.store().locations().first().name)
            assertEquals(17,db.store().product(2)!!.stock)
            assertEquals(3490,db.store().order(orderId)!!.total)
            assertEquals(1,db.store().lines(orderId).size)
        } finally { db.close();context.deleteDatabase(name) }
    }
}
