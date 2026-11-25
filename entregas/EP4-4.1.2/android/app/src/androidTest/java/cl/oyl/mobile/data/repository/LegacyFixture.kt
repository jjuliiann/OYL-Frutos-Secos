package cl.oyl.mobile.data.repository

import cl.oyl.mobile.data.local.entity.*
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.util.PasswordHash
import kotlinx.coroutines.*

suspend fun LocalRepository.initialize() = withContext(Dispatchers.IO) {
        // Datos iniciales exclusivos de las pruebas históricas SQLite.
        store.run {
            if (store.accountCount() == 0) {
                for ((alias, name, role, pass) in listOf(
                    listOf("admin", "Administrador", "ADMIN", "Admin1234!"),
                    listOf("cliente", "Cliente", "CLIENT", "Cliente1234!")
                )) {
                    val salt = PasswordHash.salt()
                    store.insertAccount(StoredAccount(alias = alias, name = name, email = "$alias@oyl.demo", phone = "+56912345678", address = "Santiago, Chile", role = role, salt = salt, passwordHash = PasswordHash.hash(pass, salt)))
                }
            }
            if (store.productCount() == 0) {
                val names = listOf("Almendras", "Nueces", "Mix Premium", "Pasas", "Semillas de chía", "Cranberries", "Avellanas")
                val prices = listOf(2990, 3490, 3990, 2490, 2290, 2890, 3290)
                val stocks = listOf(25,18,40,30,22,16,20)
                names.forEachIndexed { i, name -> store.insertProduct(Product(id = i + 1L, name = name,
                    category = if (i == 4) "Semillas" else if (i in listOf(3,5)) "Deshidratados" else "Frutos secos",
                    description = "$name seleccionadas para tu alimentación diaria.", price = prices[i], stock = stocks[i])) }
            }
            if (store.locationCount() == 0) store.insertLocations(listOf(
                StoredLocation(1, "Punto de retiro Santiago Centro", "Santiago Centro, Chile", "Horario: 09:00–18:00", -33.4489, -70.6693),
                StoredLocation(2, "Punto de retiro Providencia", "Providencia, Santiago, Chile", "Horario: 09:00–18:00", -33.4314, -70.6093),
                StoredLocation(3, "Punto de retiro Peñalolén", "Peñalolén, Santiago, Chile", "Horario: 09:00–18:00", -33.4858, -70.5525)))
        }
    }

