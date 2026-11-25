package cl.oyl.mobile.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Las tablas se conservan: Room valida el esquema tras reorganizar las clases. */
object PackageLayoutMigration : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) { /* No hay cambios en tablas ni registros. */ }
}
