package cl.oyl.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.oyl.mobile.data.local.entity.CachedOrder
import cl.oyl.mobile.data.local.entity.CachedProduct
import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.data.local.entity.LocalAvatar
import cl.oyl.mobile.data.local.entity.LocalNotice
import cl.oyl.mobile.data.local.entity.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveAvatar(avatar: LocalAvatar)
    @Query("SELECT * FROM local_avatars WHERE userId = :userId") fun avatar(userId: Long): Flow<LocalAvatar?>
    @Query("SELECT * FROM active_session WHERE `key` = 1") fun session(): Flow<Session?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSession(session: Session)
    @Query("SELECT * FROM active_session WHERE `key` = 1") suspend fun currentSession(): Session?
    @Query("DELETE FROM cached_orders WHERE userId = :userId") suspend fun clearCachedOrders(userId: Long)
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
