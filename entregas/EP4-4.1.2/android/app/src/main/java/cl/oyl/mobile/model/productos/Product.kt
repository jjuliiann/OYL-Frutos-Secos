package cl.oyl.mobile.model.productos

@androidx.room.Entity(tableName = "products")
data class Product(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long? = null,
    val name: String = "",
    val category: String = "Frutos secos",
    val description: String = "",
    val imageUri: String = "",
    val presentation: String = "250 g",
    val price: Int = 0,
    val stock: Int = 0,
    val minStock: Int = 5,
    val active: Boolean = true
)
