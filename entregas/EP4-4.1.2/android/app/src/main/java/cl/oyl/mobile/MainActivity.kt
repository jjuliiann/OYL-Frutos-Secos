package cl.oyl.mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import cl.oyl.mobile.ui.navigation.OylNavigation
import cl.oyl.mobile.ui.theme.Cream
import cl.oyl.mobile.ui.theme.Forest
import cl.oyl.mobile.viewmodel.OylViewModel
import cl.oyl.mobile.viewmodel.auth.AuthViewModel
import cl.oyl.mobile.viewmodel.productos.ProductsViewModel
import cl.oyl.mobile.viewmodel.inventario.InventoryViewModel
import cl.oyl.mobile.viewmodel.carrito.CartViewModel
import cl.oyl.mobile.viewmodel.checkout.CheckoutViewModel
import cl.oyl.mobile.viewmodel.pedidos.OrdersViewModel
import cl.oyl.mobile.viewmodel.perfil.ProfileViewModel
import cl.oyl.mobile.viewmodel.mapa.MapViewModel
import cl.oyl.mobile.viewmodel.qr.QrViewModel
import cl.oyl.mobile.viewmodel.notificaciones.NotificationsViewModel

class MainActivity : ComponentActivity() {
    private val appVm by viewModels<OylViewModel>()
    private val authVm by viewModels<AuthViewModel>()
    private val productsVm by viewModels<ProductsViewModel>()
    private val inventoryVm by viewModels<InventoryViewModel>()
    private val cartVm by viewModels<CartViewModel>()
    private val checkoutVm by viewModels<CheckoutViewModel>()
    private val ordersVm by viewModels<OrdersViewModel>()
    private val profileVm by viewModels<ProfileViewModel>()
    private val mapVm by viewModels<MapViewModel>()
    private val qrVm by viewModels<QrViewModel>()
    private val notificationsVm by viewModels<NotificationsViewModel>()
    private var pendingProductLink by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) pendingProductLink = intent.dataString
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingProductLink", pendingProductLink)
        super.onSaveInstanceState(outState)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingProductLink = if (savedInstanceState != null) savedInstanceState.getString("pendingProductLink")
            else if (intent.action == Intent.ACTION_VIEW) intent.dataString else null
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("oyl_demo", "Avisos OYL", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = Forest, background = Cream, surface = Color.White)
            ) {
                OylNavigation(
                    appVm = appVm,
                    authVm = authVm,
                    productsVm = productsVm,
                    inventoryVm = inventoryVm,
                    cartVm = cartVm,
                    checkoutVm = checkoutVm,
                    ordersVm = ordersVm,
                    profileVm = profileVm,
                    mapVm = mapVm,
                    qrVm = qrVm,
                    notificationsVm = notificationsVm,
                    productLink = pendingProductLink, linkConsumed = { pendingProductLink = null }
                )
            }
        }
    }
}
