package cl.oyl.mobile.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Los pedidos previos se conservan sin inventarles dirección ni tienda. */
object CheckoutMigration : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) { db.execSQL(CREATE_TABLE) }
    const val CREATE_TABLE = """CREATE TABLE IF NOT EXISTS `order_fulfillment` (`orderId` INTEGER NOT NULL, `delivery` TEXT NOT NULL, `recipientName` TEXT NOT NULL, `phone` TEXT NOT NULL, `email` TEXT NOT NULL, `pickupLocationId` INTEGER, `pickupName` TEXT NOT NULL, `pickupAddress` TEXT NOT NULL, `pickupHours` TEXT NOT NULL, `street` TEXT NOT NULL, `number` TEXT NOT NULL, `apartment` TEXT NOT NULL, `commune` TEXT NOT NULL, `region` TEXT NOT NULL, `postalCode` TEXT NOT NULL, `instructions` TEXT NOT NULL, PRIMARY KEY(`orderId`), FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"""
}
