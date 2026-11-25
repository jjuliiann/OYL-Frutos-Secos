package cl.oyl.mobile.ui.navigation

/** Misma dirección para ir/volver entre pestañas: Perfil → Pedidos entra desde la izquierda. */
fun navigationDirection(from: Page, to: Page): Int = if (navigationPosition(to) >= navigationPosition(from)) 1 else -1
private fun navigationPosition(page: Page): Int = when (page) {
    Page.WELCOME -> 0
    Page.LOGIN -> 1
    Page.REGISTER -> 2
    Page.DASHBOARD -> 10
    Page.CATALOG -> 11
    Page.DETAIL -> 12
    Page.CART -> 13
    Page.CHECKOUT -> 14
    Page.PRODUCTS -> 15
    Page.INVENTORY -> 16
    Page.MAP -> 20
    Page.QR -> 30
    Page.ORDERS -> 40
    Page.PROFILE -> 50
    Page.NOTIFICATIONS -> 60
}
