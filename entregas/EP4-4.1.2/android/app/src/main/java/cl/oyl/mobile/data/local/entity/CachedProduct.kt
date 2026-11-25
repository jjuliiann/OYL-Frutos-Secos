package cl.oyl.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.oyl.mobile.model.productos.Product

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
