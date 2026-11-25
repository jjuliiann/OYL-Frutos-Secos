package cl.oyl.mobile.data.local.migration

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.oyl.mobile.data.local.LocalDb
import cl.oyl.mobile.data.local.entity.StoredOrder
import cl.oyl.mobile.data.repository.LocalRepository
import cl.oyl.mobile.data.repository.initialize
import cl.oyl.mobile.model.auth.Credentials
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutMigrationTest {
    @Test fun upgradeFromThreeKeepsLegacyOrderWithoutInventingAddress() = runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val name="oyl_checkout_migration.db"
        context.deleteDatabase(name)
        var db=Room.databaseBuilder(context,LocalDb::class.java,name).build()
        try {
            val repo=LocalRepository(db);repo.initialize()
            val client=repo.login(Credentials("cliente","Cliente1234!"))
            val id=db.store().insertOrder(StoredOrder(userId=client.id,createdAt="2026-10-24T12:00",status="CONFIRMADO",delivery="RETIRO",contact="Contacto anterior",total=2990))
            db.close()
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,SQLiteDatabase.OPEN_READWRITE).use {
                it.execSQL("DROP TABLE order_fulfillment");it.version=3
            }
            db=Room.databaseBuilder(context,LocalDb::class.java,name).addMigrations(CheckoutMigration).build()
            assertEquals(2990,db.store().order(id)!!.total)
            assertEquals("Contacto anterior",db.store().order(id)!!.contact)
            assertNull(db.store().fulfillment(id))
            assertEquals(25,db.store().product(1)!!.stock)
        } finally { db.close();context.deleteDatabase(name) }
    }
}
