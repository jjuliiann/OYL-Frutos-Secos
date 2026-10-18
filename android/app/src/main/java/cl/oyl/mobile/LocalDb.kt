package cl.oyl.mobile

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "active_session")
data class Session(
    @PrimaryKey val key: Int = 1,
    val id: Long,
    val alias: String,
    val name: String,
    val role: String,
    val email: String = "",
    val phone: String = "",
    val address: String = ""
)

@Entity(tableName = "cart", primaryKeys = ["userId", "productId"])
data class CartLine(val userId: Long, val productId: Long, val quantity: Int)

@Entity(tableName = "cached_products")
data class CachedProduct(
    @PrimaryKey val id: Long,
    val name: String,
    val category: String,
    val description: String,
    val imageUri: String,
    val presentation: String,
    val price: Int,
    val stock: Int,
    val minStock: Int,
    val active: Boolean
) {
    fun toProduct(): Product = Product(id, name, category, description, imageUri, presentation, price, stock, minStock, active)
    companion object {
        fun fromProduct(p: Product): CachedProduct = CachedProduct(
            p.id ?: 0, p.name, p.category, p.description, p.imageUri,
            p.presentation, p.price, p.stock, p.minStock, p.active
        )
    }
}

@Entity(tableName = "cached_orders", primaryKeys = ["userId", "orderId"])
data class CachedOrder(val userId: Long, val orderId: Long, val total: Int, val status: String, val createdAt: String)

@Entity(tableName = "local_notices")
data class LocalNotice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false
)

@Entity(tableName = "local_avatars")
data class LocalAvatar(@PrimaryKey val userId: Long, val dataUri: String)

@Dao
interface LocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveAvatar(avatar: LocalAvatar)
    @Query("SELECT * FROM local_avatars WHERE userId = :userId") fun avatar(userId: Long): Flow<LocalAvatar?>
    @Query("SELECT * FROM active_session WHERE `key` = 1") fun session(): Flow<Session?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSession(session: Session)
    @Query("DELETE FROM active_session") suspend fun logout()
    @Query("SELECT * FROM cart WHERE userId = :userId ORDER BY productId") fun cart(userId: Long): Flow<List<CartLine>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveCart(line: CartLine)
    @Query("DELETE FROM cart WHERE userId = :userId AND productId = :productId") suspend fun removeCart(userId: Long, productId: Long)
    @Query("DELETE FROM cart WHERE userId = :userId") suspend fun clearCart(userId: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun cacheProduct(product: CachedProduct)
    @Query("SELECT * FROM cached_products ORDER BY name") suspend fun productsCached(): List<CachedProduct>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun cacheOrder(order: CachedOrder)
    @Query("SELECT * FROM cached_orders WHERE userId = :userId ORDER BY createdAt DESC") fun cachedOrders(userId: Long): Flow<List<CachedOrder>>
    @Insert suspend fun saveNotice(notice: LocalNotice)
    @Query("SELECT * FROM local_notices WHERE userId = :userId ORDER BY createdAt DESC") fun notices(userId: Long): Flow<List<LocalNotice>>
    @Query("UPDATE local_notices SET read = 1 WHERE userId = :userId AND id = :id") suspend fun readNotice(userId: Long, id: Long)
}

@Database(
    entities = [Session::class, CartLine::class, CachedProduct::class, CachedOrder::class, LocalNotice::class, LocalAvatar::class],
    version = 1, exportSchema = false
)
abstract class LocalDb : RoomDatabase() {
    abstract fun dao(): LocalDao
    companion object {
        @Volatile private var instance: LocalDb? = null
        fun get(context: Context): LocalDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, LocalDb::class.java, "oyl_v2.db")
                .build().also { instance = it }
        }
    }
}
