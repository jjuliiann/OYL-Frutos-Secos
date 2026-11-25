package cl.oyl.mobile.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Solo sustituye textos iniciales conocidos de v1. No borra ni reinicia tablas. */
object DisplayCopyMigration : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        statements.forEach { db.execSQL(it) }
    }
    val statements = listOf(
        """UPDATE accounts SET name = 'Cliente' WHERE alias = 'cliente' AND name = 'Cliente de demostración'""",
        """UPDATE active_session SET name = 'Cliente' WHERE alias = 'cliente' AND name = 'Cliente de demostración'""",
        """UPDATE products SET description = name || ' seleccionadas para tu alimentación diaria.' WHERE description = name || ' seleccionadas para tu alimentación diaria. Producto de demostración OYL.'""",
        """UPDATE cached_products SET description = name || ' seleccionadas para tu alimentación diaria.' WHERE description = name || ' seleccionadas para tu alimentación diaria. Producto de demostración OYL.'""",
        """UPDATE pickup_locations SET name = 'Punto de retiro Santiago Centro' WHERE id = 1 AND name = 'Punto de prueba Santiago Centro'""",
        """UPDATE pickup_locations SET name = 'Punto de retiro Providencia' WHERE id = 2 AND name = 'Punto de prueba Providencia'""",
        """UPDATE pickup_locations SET name = 'Punto de retiro Peñalolén' WHERE id = 3 AND name = 'Punto de prueba Peñalolén'""",
        """UPDATE pickup_locations SET hours = 'Horario: 09:00–18:00' WHERE hours = 'Referencia: 09:00–18:00'""",
        """UPDATE local_notices SET title = 'Novedades OYL' WHERE title = 'Promoción de demostración'"""
    )
}
