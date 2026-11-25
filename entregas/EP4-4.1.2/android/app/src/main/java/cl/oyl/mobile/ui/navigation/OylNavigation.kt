package cl.oyl.mobile.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.oyl.mobile.BuildConfig
import cl.oyl.mobile.ui.bienvenida.GatewayDialog
import cl.oyl.mobile.model.productos.Product
import cl.oyl.mobile.ui.auth.LoginScreen
import cl.oyl.mobile.ui.auth.RegisterScreen
import cl.oyl.mobile.ui.bienvenida.WelcomeScreen
import cl.oyl.mobile.ui.carrito.CartScreen
import cl.oyl.mobile.ui.carrito.CheckoutScreen
import cl.oyl.mobile.ui.catalogo.CatalogScreen
import cl.oyl.mobile.ui.catalogo.ProductDetailScreen
import cl.oyl.mobile.ui.components.OylTextButton
import cl.oyl.mobile.ui.inicio.DashboardScreen
import cl.oyl.mobile.ui.inventario.InventoryScreen
import cl.oyl.mobile.ui.mapa.MapScreen
import cl.oyl.mobile.ui.notificaciones.NotificationsScreen
import cl.oyl.mobile.ui.pedidos.OrdersScreen
import cl.oyl.mobile.ui.perfil.ProfileScreen
import cl.oyl.mobile.ui.productos.AdminProductsScreen
import cl.oyl.mobile.ui.qr.QrScreen
import kotlinx.coroutines.CancellationException
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
import cl.oyl.mobile.model.qr.QrResolution

@Composable
fun OylNavigation(
    appVm: OylViewModel,
    authVm: AuthViewModel,
    productsVm: ProductsViewModel,
    inventoryVm: InventoryViewModel,
    cartVm: CartViewModel,
    checkoutVm: CheckoutViewModel,
    ordersVm: OrdersViewModel,
    profileVm: ProfileViewModel,
    mapVm: MapViewModel,
    qrVm: QrViewModel,
    notificationsVm: NotificationsViewModel,
    productLink: String? = null, linkConsumed: () -> Unit = {}
) {
    val session by authVm.session.collectAsState()
    val notice by appVm.notice.collectAsState()
    val products by productsVm.products.collectAsState()
    val catalogSync by productsVm.sync.collectAsState()
    val cart by cartVm.cart.collectAsState()
    val orders by ordersVm.orders.collectAsState()
    val cachedOrders by ordersVm.cachedOrders.collectAsState()
    val avatar by profileVm.avatar.collectAsState()
    val notices by notificationsVm.notifications.collectAsState()
    val locations by mapVm.locations.collectAsState()
    val busy by appVm.busy.collectAsState()
    var page by rememberSaveable { mutableStateOf(Page.WELCOME) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedProductId by rememberSaveable { mutableStateOf<Long?>(null) }
    var linkMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmedOrder by remember { mutableStateOf<Long?>(null) }
    val isAdmin = session?.role == "ADMIN"
    val isClient = session?.role == "CLIENT"

    fun go(target: Page) {
        // No ofrece rutas de compra al administrador ni de gestión al cliente/invitado.
        if (target in listOf(Page.CART, Page.CHECKOUT) && !isClient) return
        if (target in listOf(Page.PRODUCTS, Page.INVENTORY) && !isAdmin) return
        page = target
        if (target == Page.QR) appVm.dismissNotice()
        if (target == Page.MAP) mapVm.loadLocations()
        if (target == Page.ORDERS) ordersVm.reloadOrders()
        if (target in listOf(Page.CATALOG, Page.PRODUCTS, Page.INVENTORY)) productsVm.refreshProducts()
    }

    LaunchedEffect(productLink) {
        val link = productLink ?: return@LaunchedEffect
        appVm.dismissNotice()
        selectedProduct = null
        selectedProductId = null
        linkMessage = "Consultando producto…"
        page = Page.DETAIL
        try {
            when (val resolution = qrVm.resolve(link, external = true)) {
                is QrResolution.Found -> {
                    selectedProduct = resolution.product
                    selectedProductId = resolution.product.id
                    linkMessage = null
                    appVm.showNotice(resolution.message)
                }
                else -> linkMessage = resolution.message
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            linkMessage = "No se pudo consultar el producto. Vuelve a abrir el enlace para reintentar."
        }
        linkConsumed()
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
                    StoreHeader()
                    if (session != null) OylTextButton(onClick = { go(Page.NOTIFICATIONS) }) {
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
                        icon = {
                            val iconScale by animateFloatAsState(if (page == target) 1.16f else 1f,
                                tween(300, easing = FastOutSlowInEasing), label = "Icono de navegación")
                            Icon(icon, contentDescription = name, modifier = Modifier.graphicsLayer {
                                scaleX = iconScale; scaleY = iconScale
                            })
                        },
                        label = { Text(name) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            if (notice.isNotBlank()) {
                Surface(color = Color(0xFFFFF3D8), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(notice, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        OylTextButton(onClick = appVm::dismissNotice) { Text("Cerrar") }
                    }
                }
            }
            if (busy) androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth())
            AnimatedContent(targetState = page, modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds(),
                transitionSpec = {
                    val direction = navigationDirection(initialState, targetState)
                    ((slideInHorizontally(tween(520, easing = FastOutSlowInEasing)) { it * direction / 3 } +
                        fadeIn(tween(380, delayMillis = 80)) +
                        scaleIn(tween(520, easing = FastOutSlowInEasing), initialScale = .94f)) togetherWith
                        (slideOutHorizontally(tween(420, easing = FastOutSlowInEasing)) { -it * direction / 5 } +
                        fadeOut(tween(230)) + scaleOut(tween(420), targetScale = .98f)))
                        .using(SizeTransform(clip = false))
                },
                label = "Cambio de pantalla") { currentPage ->
            when (currentPage) {
                Page.WELCOME -> WelcomeScreen(onLogin = { go(Page.LOGIN) }, onCatalog = { go(Page.CATALOG) }, onMap = { go(Page.MAP) })
                Page.REGISTER -> RegisterScreen(busy = busy, register = authVm::register) { go(Page.DASHBOARD) }
                Page.LOGIN -> LoginScreen(busy = busy, login = authVm::login, register = { go(Page.REGISTER) }) { go(Page.DASHBOARD) }
                Page.DASHBOARD -> DashboardScreen(session, products, orders, cart,
                    open = ::go,
                    detail = { selectedProduct = it; selectedProductId = it.id; linkMessage = null; go(Page.DETAIL) },
                    add = cartVm::addProduct)
                Page.MAP -> MapScreen(locations, mapVm::loadLocations)
                Page.CATALOG -> CatalogScreen(products, session, catalogSync, productsVm::refreshProducts,
                    detail = { selectedProduct = it; selectedProductId = it.id; linkMessage = null; go(Page.DETAIL) },
                    add = cartVm::addProduct)
                Page.DETAIL -> ProductDetailScreen(products.firstOrNull { it.id == selectedProductId } ?: selectedProduct, session,
                    back = { go(Page.CATALOG) },
                    add = cartVm::addProduct,
                    signIn = { go(Page.LOGIN) },
                    manageProducts = { if (isAdmin) go(Page.PRODUCTS) },
                    manageInventory = { product -> if (isAdmin) { selectedProduct = product; go(Page.INVENTORY) } },
                    unavailableMessage = linkMessage)
                Page.QR -> QrScreen(qrVm, canManage = isAdmin,
                    found = { selectedProduct = it; selectedProductId = it.id; linkMessage = null; appVm.showNotice("Producto encontrado"); go(Page.DETAIL) },
                    inventory = { product -> if (isAdmin) { selectedProduct = product; go(Page.INVENTORY) } })
                Page.CART -> if (isClient) CartScreen(cart, products, cartVm::adjustCart, checkout = { go(Page.CHECKOUT) }) else Text("El carrito está disponible para cuentas de cliente.")
                Page.CHECKOUT -> if (isClient) CheckoutScreen(cart, products, confirmedOrder, busy,
                    locations = locations, session = session, completedOrder = orders.firstOrNull { it.id == confirmedOrder },
                    confirm = checkoutVm::confirmOrder,
                    after = { confirmedOrder = it; ordersVm.reloadOrders(); productsVm.refreshProducts() },
                    orders = { confirmedOrder = null; go(Page.ORDERS) },
                    catalog = { confirmedOrder = null; go(Page.CATALOG) }) else Text("La compra está disponible para cuentas de cliente.")
                Page.PRODUCTS -> if (isAdmin) AdminProductsScreen(products, productsVm::saveProduct, productsVm::deactivate, productsVm::refreshProducts) else Text("Acceso exclusivo para administrador.")
                Page.INVENTORY -> if (isAdmin) InventoryScreen(products, inventoryVm::changeStock, selectedProduct) {
                    selectedProduct = null
                } else Text("Acceso exclusivo para administrador.")
                Page.ORDERS -> OrdersScreen(orders, cachedOrders, isAdmin, ordersVm::setStatus, ordersVm::reloadOrders)
                Page.NOTIFICATIONS -> NotificationsScreen(notices, notificationsVm::markRead, notificationsVm::demoNotice)
                Page.PROFILE -> ProfileScreen(session, avatar, profileVm::saveAvatar, profileVm::updateProfile,
                    logout = { authVm.logout { page = Page.WELCOME } },
                    notifications = { go(Page.NOTIFICATIONS) })
            }
            }
        }
    }
}

@Composable
private fun StoreHeader() {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val isClassroom = BuildConfig.BUILD_TYPE == "aula" || BuildConfig.DEBUG
    // En clase se puede cambiar de red sin añadir un botón al menú de la tienda.
    val modifier = if (isClassroom) Modifier
        .pointerInput(Unit) { detectTapGestures(onLongPress = { showSettings = true }) }
        .semantics(mergeDescendants = true) {
            onLongClick(label = "Configurar servidor de aula") { showSettings = true; true }
        } else Modifier

    Column(modifier) {
        Text("OYL FRUTOS SECOS", color = Color.White, fontWeight = FontWeight.Bold)
        Text("Naturalmente cerca de ti", color = Color(0xFFD6E8D4), style = MaterialTheme.typography.labelSmall)
    }
    if (isClassroom && showSettings) GatewayDialog(onDismiss = { showSettings = false })
}
