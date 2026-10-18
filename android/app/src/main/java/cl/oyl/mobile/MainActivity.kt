package cl.oyl.mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Forest = Color(0xFF1B6A43)
private val Cream = Color(0xFFFAF8F2)

enum class Page {
    WELCOME, REGISTER, LOGIN, DASHBOARD, MAP, CATALOG,
    DETAIL, QR, CART, CHECKOUT, PRODUCTS, INVENTORY,
    ORDERS, NOTIFICATIONS, PROFILE
}

class MainActivity : ComponentActivity() {
    private val vm by viewModels<OylViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("oyl_demo", "Avisos OYL", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = Forest, background = Cream, surface = Color.White)
            ) {
                OylNavigation(vm)
            }
        }
    }
}

@Composable
private fun OylNavigation(vm: OylViewModel) {
    val session by vm.session.collectAsState()
    val notice by vm.notice.collectAsState()
    val products by vm.products.collectAsState()
    val cart by vm.cart.collectAsState()
    val orders by vm.orders.collectAsState()
    val cachedOrders by vm.cachedOrders.collectAsState()
    val avatar by vm.avatar.collectAsState()
    val notices by vm.notifications.collectAsState()
    val locations by vm.locations.collectAsState()
    val busy by vm.busy.collectAsState()
    var page by remember { mutableStateOf(Page.WELCOME) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var confirmedOrder by remember { mutableStateOf<Long?>(null) }
    val isAdmin = session?.role == "ADMIN"

    fun go(target: Page) {
        page = target
        if (target == Page.MAP) vm.loadLocations()
        if (target == Page.ORDERS) vm.reloadOrders()
        if (target == Page.CATALOG || target == Page.INVENTORY) vm.refreshProducts()
    }

    LaunchedEffect(session?.id) {
        if (session != null && page in listOf(Page.WELCOME, Page.LOGIN, Page.REGISTER)) page = Page.DASHBOARD
    }

    BackHandler(enabled = page != Page.WELCOME && page != Page.DASHBOARD) {
        page = when (page) {
            Page.DETAIL -> Page.CATALOG
            Page.REGISTER -> Page.LOGIN
            else -> if (session == null) Page.WELCOME else Page.DASHBOARD
        }
    }

    val showBottom = session != null && page !in listOf(Page.WELCOME, Page.REGISTER, Page.LOGIN)
    Scaffold(
        topBar = {
            Surface(color = Forest, shadowElevation = 3.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("OYL FRUTOS SECOS", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Naturalmente cerca de ti", color = Color(0xFFD6E8D4), style = MaterialTheme.typography.labelSmall)
                    }
                    if (session != null) TextButton(onClick = { go(Page.NOTIFICATIONS) }) {
                        Icon(Icons.Default.Notifications, "Notificaciones", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            if (showBottom) NavigationBar {
                val tabs = listOf(
                    Triple(Page.DASHBOARD, "Inicio", Icons.Default.Home),
                    Triple(Page.MAP, "Mapa", Icons.Default.Place),
                    Triple(Page.QR, "QR", Icons.Default.QrCodeScanner),
                    Triple(Page.ORDERS, "Pedidos", Icons.Default.ReceiptLong),
                    Triple(Page.PROFILE, "Perfil", Icons.Default.Person)
                )
                tabs.forEach { (target, name, icon) ->
                    NavigationBarItem(
                        selected = page == target,
                        onClick = { go(target) },
                        icon = { Icon(icon, contentDescription = name) },
                        label = { Text(name) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (notice.isNotBlank()) {
                Surface(color = Color(0xFFFFF3D8), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(notice, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = vm::dismissNotice) { Text("Cerrar") }
                    }
                }
            }
            when (page) {
                Page.WELCOME -> WelcomeScreen(onLogin = { go(Page.LOGIN) }, onCatalog = { go(Page.CATALOG) }, onMap = { go(Page.MAP) })
                Page.REGISTER -> RegisterScreen(busy = busy, register = vm::register) { go(Page.DASHBOARD) }
                Page.LOGIN -> LoginScreen(busy = busy, login = vm::login, register = { go(Page.REGISTER) }) { go(Page.DASHBOARD) }
                Page.DASHBOARD -> DashboardScreen(session, products, orders, cart,
                    open = ::go,
                    detail = { selectedProduct = it; go(Page.DETAIL) },
                    add = vm::addProduct)
                Page.MAP -> MapScreen(locations, vm::loadLocations)
                Page.CATALOG -> CatalogScreen(products, session,
                    detail = { selectedProduct = it; go(Page.DETAIL) },
                    add = vm::addProduct)
                Page.DETAIL -> ProductDetailScreen(selectedProduct, session,
                    back = { go(Page.CATALOG) },
                    add = vm::addProduct,
                    signIn = { go(Page.LOGIN) })
                Page.QR -> QrScreen(products, canManage = isAdmin,
                    found = { selectedProduct = it; go(Page.DETAIL) },
                    notFound = { vm.notice.value = it },
                    inventory = { product -> if (isAdmin) { selectedProduct = product; go(Page.INVENTORY) } })
                Page.CART -> CartScreen(cart, products, vm::adjustCart, checkout = { go(Page.CHECKOUT) })
                Page.CHECKOUT -> CheckoutScreen(cart, products, confirmedOrder, busy,
                    confirm = vm::confirmOrder,
                    after = { confirmedOrder = it },
                    orders = { confirmedOrder = null; go(Page.ORDERS) },
                    catalog = { confirmedOrder = null; go(Page.CATALOG) })
                Page.PRODUCTS -> AdminProductsScreen(products, vm::saveProduct, vm::refreshProducts)
                Page.INVENTORY -> InventoryScreen(products, vm::changeStock, selectedProduct) {
                    selectedProduct = null
                }
                Page.ORDERS -> OrdersScreen(orders, cachedOrders, isAdmin, vm::setStatus, vm::reloadOrders)
                Page.NOTIFICATIONS -> NotificationsScreen(notices, vm::markRead, vm::demoNotice)
                Page.PROFILE -> ProfileScreen(session, avatar, vm::saveAvatar, vm::updateProfile,
                    logout = { vm.logout { page = Page.WELCOME } },
                    notifications = { go(Page.NOTIFICATIONS) })
            }
        }
    }
}

@Composable
fun ActionButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}
