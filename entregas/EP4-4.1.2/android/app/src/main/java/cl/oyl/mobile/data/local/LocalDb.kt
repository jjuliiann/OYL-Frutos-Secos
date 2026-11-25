package cl.oyl.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.oyl.mobile.data.local.dao.LocalDao
import cl.oyl.mobile.data.local.dao.StoreDao
import cl.oyl.mobile.data.local.entity.CachedOrder
import cl.oyl.mobile.data.local.entity.CachedProduct
import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.data.local.entity.LocalAvatar
import cl.oyl.mobile.data.local.entity.LocalNotice
import cl.oyl.mobile.data.local.entity.Session
import cl.oyl.mobile.data.local.entity.StoredAccount
import cl.oyl.mobile.data.local.entity.StoredLine
import cl.oyl.mobile.data.local.entity.StoredLocation
import cl.oyl.mobile.data.local.entity.StoredOrder
import cl.oyl.mobile.data.local.migration.DisplayCopyMigration
import cl.oyl.mobile.data.local.migration.PackageLayoutMigration
import cl.oyl.mobile.data.local.entity.OrderFulfillment
import cl.oyl.mobile.data.local.migration.CheckoutMigration
import cl.oyl.mobile.model.productos.Product

@Database(
    entities = [Session::class, CartLine::class, CachedProduct::class, CachedOrder::class, LocalNotice::class, LocalAvatar::class, StoredAccount::class, StoredOrder::class, StoredLine::class, StoredLocation::class, Product::class, OrderFulfillment::class],
    version = 4, exportSchema = false
)
abstract class LocalDb : RoomDatabase() {
    abstract fun dao(): LocalDao
    abstract fun store(): StoreDao
    companion object {
        private val databases = mutableMapOf<String, LocalDb>()
        @Synchronized fun get(context: Context, name: String = "oyl_sqlite.db"): LocalDb =
            databases.getOrPut(name) {
                Room.databaseBuilder(context.applicationContext, LocalDb::class.java, name)
                    .addMigrations(DisplayCopyMigration, PackageLayoutMigration, CheckoutMigration).build()
            }
    }
}
