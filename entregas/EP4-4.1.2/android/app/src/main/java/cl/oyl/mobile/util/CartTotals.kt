package cl.oyl.mobile.util

import cl.oyl.mobile.data.local.entity.CartLine
import cl.oyl.mobile.model.productos.Product

fun cartTotal(lines: List<CartLine>, products: List<Product>): Int = lines.sumOf { line ->
    (products.firstOrNull { it.id == line.productId }?.price ?: 0) * line.quantity
}
