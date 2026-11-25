package cl.oyl.mobile.data.local.dao

import androidx.room.*
import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.data.local.entity.StoredAccount
import cl.oyl.mobile.data.local.entity.StoredLine
import cl.oyl.mobile.data.local.entity.StoredLocation
import cl.oyl.mobile.data.local.entity.StoredOrder
import cl.oyl.mobile.data.local.entity.OrderFulfillment
import cl.oyl.mobile.model.productos.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Insert suspend fun insertFulfillment(details: OrderFulfillment)
    @Query("SELECT * FROM order_fulfillment WHERE orderId = :orderId") suspend fun fulfillment(orderId: Long): OrderFulfillment?
    @Query("SELECT * FROM accounts WHERE alias = :alias COLLATE NOCASE LIMIT 1") suspend fun account(alias: String): StoredAccount?
    @Query("SELECT * FROM accounts WHERE id = :id") suspend fun account(id: Long): StoredAccount?
    @Query("SELECT COUNT(*) FROM accounts") suspend fun accountCount(): Int
    @Insert suspend fun insertAccount(account: StoredAccount): Long
    @Update suspend fun updateAccount(account: StoredAccount)
    @Query("SELECT * FROM products ORDER BY name") fun observeProducts(): Flow<List<Product>>
    @Query("SELECT COUNT(*) FROM products") suspend fun productCount(): Int
    @Query("SELECT * FROM products WHERE id = :id") suspend fun product(id: Long): Product?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProduct(product: Product): Long
    @Query("DELETE FROM products") suspend fun clearProducts()
    @Query("DELETE FROM pickup_locations") suspend fun clearLocations()
    @Update suspend fun updateProduct(product: Product)
    @Query("SELECT * FROM orders ORDER BY id DESC") fun observeOrders(): Flow<List<StoredOrder>>
    @Query("SELECT * FROM orders WHERE id = :id") suspend fun order(id: Long): StoredOrder?
    @Insert suspend fun insertOrder(order: StoredOrder): Long
    @Update suspend fun updateOrder(order: StoredOrder)
    @Insert suspend fun insertLine(line: StoredLine)
    @Query("SELECT * FROM order_lines WHERE orderId = :id ORDER BY id") suspend fun lines(id: Long): List<StoredLine>
    @Query("SELECT * FROM cart WHERE userId = :userId") suspend fun cartNow(userId: Long): List<CartLine>
    @Query("SELECT * FROM cart WHERE userId = :userId AND productId = :productId") suspend fun cartLine(userId: Long, productId: Long): CartLine?
    @Insert suspend fun insertLocations(locations: List<StoredLocation>)
    @Query("SELECT COUNT(*) FROM pickup_locations") suspend fun locationCount(): Int
    @Query("SELECT * FROM pickup_locations ORDER BY id") suspend fun locations(): List<StoredLocation>
}
